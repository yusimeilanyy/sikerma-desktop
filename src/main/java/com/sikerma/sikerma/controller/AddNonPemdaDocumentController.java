package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class AddNonPemdaDocumentController {

    // === FIELDS FXML ===
    @FXML private ComboBox<String> cbJenisPerjanjian;

    // ✅ Jenis Dokumen (ComboBox + TextField untuk "Lainnya...")
    @FXML private ComboBox<String> cbJenisDokumen;
    @FXML private VBox boxJenisDokumenLainnya;
    @FXML private TextField txtJenisDokumenLainnya;

    // ✅ Tingkat Kerja Sama (INPUT TEXT - BISA DIKETIK)
    @FXML private TextField txtTingkatKerjaSama;

    @FXML private ComboBox<String> cbPicBlsdm;
    @FXML private TextField txtNomorDokumenBalai;
    @FXML private TextField txtNomorDokumenMitra;
    @FXML private TextField txtPicMitra;
    @FXML private TextField txtKontakMitra;
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
        // 1. Jenis Perjanjian
        cbJenisPerjanjian.getItems().addAll("MoU (Memorandum of Understanding)", "PKS (Perjanjian Kerja Sama)");
        cbJenisPerjanjian.setValue("MoU (Memorandum of Understanding)");

        // ✅ 2. Jenis Dokumen (Dropdown + Opsi "Lainnya...")
        cbJenisDokumen.getItems().addAll(
                "Pilih jenis dokumen",
                "Nota Kesepahaman",
                "Perjanjian Kerjasama",
                "Surat Pernyataan",
                "Nota Kesepakatan",
                "Lainnya..."
        );
        cbJenisDokumen.setValue("Pilih jenis dokumen");

        // ✅ Event: Tampilkan TextField jika pilih "Lainnya..."
        cbJenisDokumen.setOnAction(e -> {
            String selected = cbJenisDokumen.getValue();
            if ("Lainnya...".equals(selected)) {
                boxJenisDokumenLainnya.setVisible(true);
                boxJenisDokumenLainnya.setManaged(true);
                txtJenisDokumenLainnya.requestFocus();
            } else {
                boxJenisDokumenLainnya.setVisible(false);
                boxJenisDokumenLainnya.setManaged(false);
                txtJenisDokumenLainnya.setText("");
            }
        });

        // 3. PIC BLSDM (Contoh Data)
        cbPicBlsdm.getItems().addAll("Dr. Ahmad Santoso", "Dra. Maria Wowor", "Ir. John Lengkong");
        cbPicBlsdm.setValue("Pilih PIC BLSDM Komdigi Manado");

        // ✅ 4. Status Lengkap Sesuai Gambar
        cbStatus.getItems().addAll(
                "Baru",
                "Dalam Proses",
                "Review PEMDA 1",
                "Review BPSDMP Kominfo",
                "Review BPSDMP 1",
                "Review PEMDA 2",
                "Review BPSDMP 2",
                "Persiapan TTD Para Pihak",
                "Selesai"
        );
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
        // Validasi: Tingkat Kerja Sama wajib diisi
        if (txtTingkatKerjaSama.getText().trim().isEmpty()) {
            showAlert("Validasi", "Tingkat Kerja Sama / Nama Instansi harus diisi!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = "";
            if (selectedFile != null) {
                filePath = uploadFile(selectedFile);
            }

            // ✅ Tentukan nilai Jenis Dokumen (dari combo atau textfield)
            String jenisDokumenValue = cbJenisDokumen.getValue();
            if ("Lainnya...".equals(jenisDokumenValue)) {
                jenisDokumenValue = txtJenisDokumenLainnya.getText().trim();
                if (jenisDokumenValue.isEmpty()) {
                    showAlert("Validasi", "Jenis dokumen harus diisi!", Alert.AlertType.WARNING);
                    return;
                }
            }

            // Simpan ke Database
            String sql = "INSERT INTO documents (nomor_dokumen, jenis, mitra, kategori, tanggal_mulai, " +
                    "tanggal_berakhir, pic, file_path, status, keterangan, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String nomorDokumen = txtNomorDokumenBalai.getText().trim();
                if (nomorDokumen.isEmpty()) nomorDokumen = txtNomorDokumenMitra.getText().trim();

                pstmt.setString(1, nomorDokumen);
                pstmt.setString(2, cbJenisPerjanjian.getValue());

                // Mitra = Tingkat Kerja Sama (input text)
                pstmt.setString(3, txtTingkatKerjaSama.getText().trim());

                // ✅ Kategori = Jenis Dokumen (bisa dari dropdown atau input manual)
                pstmt.setString(4, jenisDokumenValue);

                pstmt.setString(5, dpTanggalMulai.getValue() != null ? dpTanggalMulai.getValue().toString() : "");
                pstmt.setString(6, dpTanggalBerakhir.getValue() != null ? dpTanggalBerakhir.getValue().toString() : "");
                pstmt.setString(7, txtPicMitra.getText().trim());
                pstmt.setString(8, filePath);

                // Simpan Status yang dipilih
                pstmt.setString(9, cbStatus.getValue());

                pstmt.setString(10, txtCatatan.getText().trim());
                pstmt.setInt(11, currentUserId);

                pstmt.executeUpdate();

                showAlert("Sukses!", "Dokumen berhasil disimpan.", Alert.AlertType.INFORMATION);

                Stage stage = (Stage) txtNomorDokumenBalai.getScene().getWindow();
                stage.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menyimpan dokumen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String uploadFile(File file) {
        try {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.toPath(), targetPath);

            return targetPath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
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
        alert.setContentText("Yakin ingin membatalkan?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) handleBack();
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}