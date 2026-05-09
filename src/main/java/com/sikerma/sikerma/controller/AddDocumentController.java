package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class AddDocumentController {

    @FXML private ComboBox<String> cbJenisPerjanjian;
    @FXML private ComboBox<String> cbTingkatKerjaSama;
    @FXML private ComboBox<String> cbJenisDokumen;
    @FXML private ComboBox<String> cbPICBLSMDM;
    @FXML private TextField txtNomorDokumenBalai;
    @FXML private TextField txtNomorDokumenPemda;
    @FXML private TextField txtPICPemda;
    @FXML private TextField txtKontakPICPemda;
    @FXML private TextField txtPemilik;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpTanggalMulai;
    @FXML private DatePicker dpTanggalBerakhir;
    @FXML private TextArea txtCatatan;
    @FXML private Label lblFileName;

    private File selectedFile;
    private int currentUserId;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        // Initialize ComboBox
        cbJenisPerjanjian.getItems().addAll(
                "MoU (Memorandum of Understanding)",
                "PKS (Perjanjian Kerja Sama)"
        );
        cbJenisPerjanjian.setValue("MoU (Memorandum of Understanding)");

        cbTingkatKerjaSama.getItems().addAll(
                "Pusat",
                "Provinsi",
                "Kabupaten/Kota"
        );

        cbJenisDokumen.getItems().addAll(
                "Dokumen Utama",
                "Dokumen Pendukung",
                "Lampiran"
        );

        cbPICBLSMDM.getItems().addAll(
                "Dr. Ahmad Santoso, M.Si",
                "Dra. Maria Wowor, M.Pd",
                "Ir. John Lengkong, M.T",
                "Drs. Sarah Tumangkeng, M.Si"
        );

        cbStatus.getItems().addAll("Baru", "Proses", "Selesai");
        cbStatus.setValue("Baru");
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Dokumen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Files", "*.doc", "*.docx")
        );

        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            lblFileName.setText(selectedFile.getName());
        }
    }

    @FXML
    private void handleSave() {
        // Validasi input
        if (!validateInput()) {
            return;
        }

        try {
            // Upload file jika ada
            String filePath = "";
            if (selectedFile != null) {
                filePath = uploadFile(selectedFile);
            }

            // Tentukan status otomatis
            String status = determineStatus(dpTanggalBerakhir.getValue());

            // Simpan ke database
            String sql = "INSERT INTO documents (nomor_dokumen, jenis, mitra, kategori, tanggal_mulai, " +
                    "tanggal_berakhir, pic, file_path, status, keterangan, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Gunakan nomor dokumen balai atau pemda
                String nomorDokumen = txtNomorDokumenBalai.getText().trim();
                if (nomorDokumen.isEmpty()) {
                    nomorDokumen = txtNomorDokumenPemda.getText().trim();
                }

                pstmt.setString(1, nomorDokumen);
                pstmt.setString(2, cbJenisPerjanjian.getValue());
                pstmt.setString(3, txtPemilik.getText().trim());
                pstmt.setString(4, cbTingkatKerjaSama.getValue());
                pstmt.setString(5, dpTanggalMulai.getValue().toString());
                pstmt.setString(6, dpTanggalBerakhir.getValue().toString());
                pstmt.setString(7, txtPICPemda.getText().trim());
                pstmt.setString(8, filePath);
                pstmt.setString(9, status);
                pstmt.setString(10, txtCatatan.getText().trim());
                pstmt.setInt(11, currentUserId);

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    showAlert("Sukses!", "Dokumen berhasil disimpan.", Alert.AlertType.INFORMATION);

                    // Tutup window dan kembali ke dashboard
                    Stage stage = (Stage) txtNomorDokumenBalai.getScene().getWindow();
                    stage.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menyimpan dokumen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateInput() {
        if (cbJenisPerjanjian.getValue() == null || cbJenisPerjanjian.getValue().isEmpty()) {
            showAlert("Validasi", "Jenis perjanjian harus dipilih!", Alert.AlertType.WARNING);
            return false;
        }

        if (txtNomorDokumenBalai.getText().trim().isEmpty() && txtNomorDokumenPemda.getText().trim().isEmpty()) {
            showAlert("Validasi", "Nomor dokumen (Balai atau Pemerintah Daerah) harus diisi!", Alert.AlertType.WARNING);
            return false;
        }

        if (txtPemilik.getText().trim().isEmpty()) {
            showAlert("Validasi", "Nama pemilik harus diisi!", Alert.AlertType.WARNING);
            txtPemilik.requestFocus();
            return false;
        }

        if (dpTanggalMulai.getValue() == null) {
            showAlert("Validasi", "Tanggal mulai kerja sama harus dipilih!", Alert.AlertType.WARNING);
            dpTanggalMulai.requestFocus();
            return false;
        }

        if (dpTanggalBerakhir.getValue() == null) {
            showAlert("Validasi", "Tanggal berakhir kerja sama harus dipilih!", Alert.AlertType.WARNING);
            dpTanggalBerakhir.requestFocus();
            return false;
        }

        // Cek tanggal tidak terbalik
        if (dpTanggalBerakhir.getValue().isBefore(dpTanggalMulai.getValue())) {
            showAlert("Validasi", "Tanggal berakhir tidak boleh sebelum tanggal mulai!", Alert.AlertType.WARNING);
            dpTanggalBerakhir.requestFocus();
            return false;
        }

        return true;
    }

    private String uploadFile(File file) {
        try {
            // Buat folder uploads jika belum ada
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Path targetPath = uploadPath.resolve(fileName);

            // Copy file
            Files.copy(file.toPath(), targetPath);

            return targetPath.toString();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error Upload", "Gagal upload file: " + e.getMessage(), Alert.AlertType.ERROR);
            return "";
        }
    }

    private String determineStatus(LocalDate tanggalBerakhir) {
        if (tanggalBerakhir == null) return "Aktif";

        LocalDate today = LocalDate.now();
        long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, tanggalBerakhir);

        if (daysUntilExpiry < 0) {
            return "Kadaluarsa";
        } else if (daysUntilExpiry <= 14) {
            return "Perlu Perhatian";
        } else {
            return "Aktif";
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) txtNomorDokumenBalai.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Batal");
        alert.setContentText("Yakin ingin membatalkan? Data yang sudah diisi akan hilang.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                handleBack();
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}