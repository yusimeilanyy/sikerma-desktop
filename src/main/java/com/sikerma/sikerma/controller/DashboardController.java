package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;

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

    // === VARIABEL TABEL (DI-UPDATE) ===
    @FXML private TableView<Document> tableDocuments;
    @FXML private TableColumn<Document, Integer> colNo;              // ✅ TAMBAH: Kolom No
    @FXML private TableColumn<Document, String> colJenis;
    @FXML private TableColumn<Document, String> colNomor;
    @FXML private TableColumn<Document, String> colMitra;
    @FXML private TableColumn<Document, LocalDate> colTanggalMulai;  // ✅ TAMBAH: Tanggal Mulai
    @FXML private TableColumn<Document, LocalDate> colTanggal;
    @FXML private TableColumn<Document, String> colStatus;
    // ❌ HAPUS: colAksi (tidak ada tombol Edit/Hapus di dashboard)

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

    // ✅ METHOD INI DI-UPDATE: Tampilan Modern dengan Badge & Tanpa Tombol Aksi
    private void initializeTable() {
        // Kolom No dengan nomor urut otomatis
        colNo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #64748b;");
                }
            }
        });

        // Kolom Nomor Dokumen
        colNomor.setCellValueFactory(new PropertyValueFactory<>("nomorDokumen"));

        // ✅ Kolom Jenis dengan Badge Berwarna
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colJenis.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(getJenisStyle(item));
                    badge.setPadding(new Insets(6, 12, 6, 12));
                    badge.setStyle(badge.getStyle() +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 12px; " +
                            "-fx-alignment: CENTER;");
                    setGraphic(badge);
                }
            }
        });

        // Kolom Mitra
        colMitra.setCellValueFactory(new PropertyValueFactory<>("mitra"));

        // ✅ Kolom Tanggal Mulai dengan Format Indonesia
        colTanggalMulai.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        colTanggalMulai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTanggalIndonesia(item));
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569;");
                }
            }
        });

        // Kolom Tanggal Berakhir dengan Format Indonesia
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalBerakhir"));
        colTanggal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTanggalIndonesia(item));
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569;");
                }
            }
        });

        // ✅ Kolom Status dengan Badge Berwarna Lengkap
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(getStatusStyle(item));
                    badge.setPadding(new Insets(6, 15, 6, 15));
                    badge.setStyle(badge.getStyle() +
                            "-fx-background-radius: 20; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 12px; " +
                            "-fx-alignment: CENTER;");
                    setGraphic(badge);
                }
            }
        });
    }

    // ✅ Helper: Style untuk Badge Jenis
    private String getJenisStyle(String jenis) {
        if (jenis == null) return "-fx-background-color: #cbd5e1;";
        if (jenis.contains("PKS")) return "-fx-background-color: #fdba74;";
        if (jenis.contains("MoU")) return "-fx-background-color: #99f6e4;";
        return "-fx-background-color: #cbd5e1;";
    }

    // ✅ Helper: Style untuk Badge Status (LENGKAP)
    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #94a3b8;";

        String s = status.toLowerCase();
        if (s.equals("baru")) return "-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca;";
        if (s.equals("dalam proses")) return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;";
        if (s.equals("review bpsdmp kominfo")) return "-fx-background-color: #ddd6fe; -fx-text-fill: #7c3aed;";
        if (s.equals("review pemda 1") || s.equals("review pemda 2"))
            return "-fx-background-color: #fce7f3; -fx-text-fill: #db2777;";
        if (s.equals("review bpsdmp 1") || s.equals("review bpsdmp 2"))
            return "-fx-background-color: #ccfbf1; -fx-text-fill: #0d9488;";
        if (s.equals("persiapan ttd para pihak"))
            return "-fx-background-color: #fef9c3; -fx-text-fill: #a16207;";
        if (s.equals("selesai")) return "-fx-background-color: #bbf7d0; -fx-text-fill: #15803d;";
        if (s.equals("aktif")) return "-fx-background-color: #86efac; -fx-text-fill: #166534;";
        if (s.equals("perlu perhatian")) return "-fx-background-color: #fdba74; -fx-text-fill: #c2410c;";
        if (s.equals("kadaluarsa")) return "-fx-background-color: #fca5a5; -fx-text-fill: #b91c1c;";

        return "-fx-background-color: #94a3b8;";
    }

    // ✅ Helper: Format Tanggal Indonesia (13 Maret 2026)
    private String formatTanggalIndonesia(LocalDate date) {
        if (date == null) return "-";
        String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        return date.getDayOfMonth() + " " + bulan[date.getMonthValue() - 1] + " " + date.getYear();
    }

    // ✅ METHOD INI DI-UPDATE: Ambil tanggal_mulai dari database
    private void loadDashboardData() {
        String sql = "SELECT * FROM documents WHERE is_deleted = 0 ORDER BY created_at DESC";

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

                // ✅ Ambil tanggal_mulai
                if (rs.getDate("tanggal_mulai") != null)
                    doc.setTanggalMulai(rs.getDate("tanggal_mulai").toLocalDate());
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

            lblTotalMou.setText(String.valueOf(totalMou));
            lblTotalPks.setText(String.valueOf(totalPks));
            lblActive.setText(String.valueOf(active));
            lblExpired.setText(String.valueOf(expired));

            tableDocuments.setItems(documentList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === METHOD: Handle Tambah Dokumen ===
    @FXML
    private void handleAddDocument() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Pemerintah Daerah",
                "Pemerintah Daerah", "Non-Pemerintah Daerah");
        dialog.setTitle("Pilih Jenis Dokumen");
        dialog.setHeaderText("Silakan pilih jenis mitra dokumen:");
        dialog.setContentText("Jenis Mitra:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                String jenis = result.get();
                String fxmlFile = jenis.equals("Pemerintah Daerah") ?
                        "/fxml/add_pemda_document.fxml" : "/fxml/add_non_pemda_document.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load(), 800, 700));
                stage.setTitle("Tambah Dokumen - " + jenis);

                Object controller = loader.getController();
                if (controller instanceof AddPemdaDocumentController) {
                    ((AddPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                } else if (controller instanceof AddNonPemdaDocumentController) {
                    ((AddNonPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                }

                stage.showAndWait();
                loadDashboardData();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka form: " + e.getMessage());
            }
        }
    }

    // === METHOD: Handle Edit Dokumen ===
    @FXML
    private void handleEdit(Document doc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_document.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 800, 650));
            stage.setTitle("Edit Dokumen - " + doc.getNomorDokumen());

            EditDocumentController controller = loader.getController();
            controller.setDocumentData(doc);
            controller.setCurrentUserId(currentUserId);

            stage.showAndWait();
            loadDashboardData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form edit: " + e.getMessage());
        }
    }

    // === METHOD: Handle Hapus Dokumen (Soft Delete) ===
    @FXML
    private void handleDelete(Document doc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Dokumen");
        confirm.setContentText("Yakin ingin menghapus dokumen:\n\"" + doc.getNomorDokumen() + "\"?\n\nData akan ditandai sebagai terhapus (soft delete).");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.connect()) {
                    String sql = "UPDATE documents SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, doc.getId());
                    pstmt.executeUpdate();

                    showAlert("Sukses", "Dokumen berhasil dihapus.");
                    loadDashboardData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Gagal menghapus: " + e.getMessage());
                }
            }
        });
    }

    // === EVENT HANDLER LAINNYA ===
    @FXML private void handleDashboard() { loadDashboardData(); }

    @FXML private void handleViewDocuments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/all_documents.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Manajemen Dokumen - SIKERMA");

            AllDocumentsController controller = loader.getController();
            controller.setUserData(currentUserId, lblUserName.getText());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka halaman dokumen: " + e.getMessage());
        }
    }

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