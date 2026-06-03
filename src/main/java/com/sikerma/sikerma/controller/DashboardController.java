package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional; // ✅ Import untuk ChoiceDialog

public class DashboardController {

    // === VARIABEL LABEL ===
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTotalMou;
    @FXML private Label lblTotalPks;
    @FXML private Label lblActive;
    @FXML private Label lblExpired;

    // === VARIABEL FILTER ===
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbJenis;
    @FXML private ComboBox<String> cbStatus;

    // === VARIABEL TABEL ===
    @FXML private TableView<Document> tableDocuments;
    @FXML private TableColumn<Document, String> colNomor;
    @FXML private TableColumn<Document, String> colJenis;
    @FXML private TableColumn<Document, String> colMitra;
    @FXML private TableColumn<Document, LocalDate> colTanggal;
    @FXML private TableColumn<Document, String> colStatus;
    @FXML private TableColumn<Document, Void> colAksi;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private int currentUserId;

    // === METHOD UTAMA: Dipanggil setelah login sukses ===
    public void setUserData(int userId, String userName, String role) {
        this.currentUserId = userId;
        lblUserName.setText(userName);
        lblUserRole.setText(role.toUpperCase());

        initializeComboBox();
        initializeTable();
        loadDashboardData();
    }

    private void initializeComboBox() {
        cbJenis.getItems().addAll("Semua Jenis", "MOU", "PKS");
        cbJenis.setValue("Semua Jenis");

        cbStatus.getItems().addAll("Semua Status", "Aktif", "Perlu Perhatian", "Kadaluarsa");
        cbStatus.setValue("Semua Status");
    }

    private void initializeTable() {
        colNomor.setCellValueFactory(new PropertyValueFactory<>("nomorDokumen"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colMitra.setCellValueFactory(new PropertyValueFactory<>("mitra"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalBerakhir"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Kolom Aksi (Edit & Hapus)
        colAksi.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button btnEdit = new Button("Edit");
                    btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand;");

                    Button btnDelete = new Button("Hapus");
                    btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");

                    setGraphic(new HBox(5, btnEdit, btnDelete));
                }
            }
        });
    }

    private void loadDashboardData() {
        String sql = "SELECT * FROM documents ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            documentList.clear();
            int totalMou = 0, totalPks = 0, active = 0, expired = 0;
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getInt("id"));
                doc.setNomorDokumen(rs.getString("nomor_dokumen"));
                doc.setJenis(rs.getString("jenis"));
                doc.setMitra(rs.getString("mitra"));
                if (rs.getDate("tanggal_berakhir") != null)
                    doc.setTanggalBerakhir(rs.getDate("tanggal_berakhir").toLocalDate());
                doc.setStatus(rs.getString("status"));
                documentList.add(doc);

                if ("MOU".equals(doc.getJenis())) totalMou++;
                if ("PKS".equals(doc.getJenis())) totalPks++;

                if (doc.getTanggalBerakhir() != null && doc.getTanggalBerakhir().isBefore(today)) {
                    expired++;
                } else {
                    active++;
                }
            }

            // Update Statistik Cards
            lblTotalMou.setText(String.valueOf(totalMou));
            lblTotalPks.setText(String.valueOf(totalPks));
            lblActive.setText(String.valueOf(active));
            lblExpired.setText(String.valueOf(expired));

            tableDocuments.setItems(documentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === ✅ METHOD BARU: Handle Tambah Dokumen dengan Popup Pilihan ===
    @FXML
    private void handleAddDocument() {
        // 1. Tampilkan Popup Pilihan Jenis Dokumen
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Pemerintah Daerah",
                "Pemerintah Daerah", "Non-Pemerintah Daerah");
        dialog.setTitle("Pilih Jenis Dokumen");
        dialog.setHeaderText("Silakan pilih jenis mitra dokumen:");
        dialog.setContentText("Jenis Mitra:");

        Optional<String> result = dialog.showAndWait();

        // 2. Buka Form Sesuai Pilihan User
        if (result.isPresent()) {
            try {
                String jenis = result.get();

                // Tentukan file FXML berdasarkan pilihan
                String fxmlFile;
                if (jenis.equals("Pemerintah Daerah")) {
                    fxmlFile = "/fxml/add_pemda_document.fxml"; // Form Pemda
                } else {
                    fxmlFile = "/fxml/add_non_pemda_document.fxml"; // Form Non-Pemda
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load(), 800, 700));
                stage.setTitle("Tambah Dokumen - " + jenis);

                // Kirim currentUserId ke Controller yang sesuai
                Object controller = loader.getController();
                if (controller instanceof AddPemdaDocumentController) {
                    ((AddPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                } else if (controller instanceof AddNonPemdaDocumentController) {
                    ((AddNonPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                }

                stage.showAndWait(); // Tunggu form ditutup
                loadDashboardData(); // Refresh data tabel

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka form: " + e.getMessage());
            }
        }
    }

    // === EVENT HANDLER LAINNYA ===
    @FXML private void handleDashboard() { loadDashboardData(); }
    @FXML private void handleViewDocuments() { loadDashboardData(); }

    @FXML private void handleRenewal() {
        showAlert("Info", "Fitur perpanjangan akan dibuat...");
    }

    @FXML private void handleNotifications() {
        showAlert("Info", "Fitur notifikasi akan dibuat...");
    }

    @FXML private void handleUsers() {
        showAlert("Info", "Manajemen User (Admin Only)");
    }

    @FXML private void handleSearch() {
        String keyword = txtSearch.getText().toLowerCase();
        ObservableList<Document> filtered = FXCollections.observableArrayList();

        for (Document doc : documentList) {
            if (doc.getNomorDokumen().toLowerCase().contains(keyword) ||
                    doc.getMitra().toLowerCase().contains(keyword)) {
                filtered.add(doc);
            }
        }
        tableDocuments.setItems(filtered);
    }

    @FXML private void handleLogout() {
        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}