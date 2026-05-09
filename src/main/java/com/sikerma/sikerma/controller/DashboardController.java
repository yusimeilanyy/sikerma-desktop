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

public class DashboardController {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTotalMou;      // ✅ DITAMBAHKAN
    @FXML private Label lblTotalPks;      // ✅ DITAMBAHKAN
    @FXML private Label lblActive;
    @FXML private Label lblExpired;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbJenis;
    @FXML private ComboBox<String> cbStatus;

    @FXML private TableView<Document> tableDocuments;
    @FXML private TableColumn<Document, String> colNomor;
    @FXML private TableColumn<Document, String> colJenis;
    @FXML private TableColumn<Document, String> colMitra;
    @FXML private TableColumn<Document, LocalDate> colTanggal;
    @FXML private TableColumn<Document, String> colStatus;
    @FXML private TableColumn<Document, Void> colAksi;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private int currentUserId;

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

            // ✅ BAGIAN INI YANG DIPERBAIKI (tambah 2 baris pertama):
            lblTotalMou.setText(String.valueOf(totalMou));   // ✅ TAMBAH INI
            lblTotalPks.setText(String.valueOf(totalPks));   // ✅ TAMBAH INI
            lblActive.setText(String.valueOf(active));
            lblExpired.setText(String.valueOf(expired));

            tableDocuments.setItems(documentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddDocument() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_document.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);

            Stage stage = new Stage();
            stage.setTitle("Tambah Dokumen - SIKERMA");
            stage.setScene(scene);

            AddDocumentController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);

            stage.showAndWait();
            loadDashboardData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form: " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard() { loadDashboardData(); }

    @FXML
    private void handleViewDocuments() { loadDashboardData(); }

    @FXML
    private void handleRenewal() {
        showAlert("Info", "Fitur perpanjangan akan dibuat...");
    }

    @FXML
    private void handleNotifications() {
        showAlert("Info", "Fitur notifikasi akan dibuat...");
    }

    @FXML
    private void handleUsers() {
        showAlert("Info", "Manajemen User (Admin Only)");
    }

    @FXML
    private void handleSearch() {
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

    @FXML
    private void handleLogout() {
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