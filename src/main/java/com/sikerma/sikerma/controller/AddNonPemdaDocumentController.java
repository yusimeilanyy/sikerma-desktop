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

    @FXML private ComboBox<String> cbJenisPerjanjian;
    @FXML private ComboBox<String> cbJenisDokumen;
    @FXML private VBox boxJenisDokumenLainnya;
    @FXML private TextField txtJenisDokumenLainnya;
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
        cbJenisPerjanjian.getItems().addAll("MoU (Memorandum of Understanding)", "PKS (Perjanjian Kerja Sama)");
        cbJenisPerjanjian.setValue("MoU (Memorandum of Understanding)");

        cbJenisDokumen.getItems().addAll(
                "Pilih jenis dokumen",
                "Nota Kesepahaman",
                "Perjanjian Kerjasama",
                "Surat Pernyataan",
                "Nota Kesepakatan",
                "Lainnya..."
        );
        cbJenisDokumen.setValue("Pilih jenis dokumen");

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

        cbPicBlsdm.getItems().addAll("Dr. Ahmad Santoso, M.Si", "Dra. Maria Wowor, M.Pd",
                "Ir. John Lengkong, M.T", "Drs. Sarah Tumangkeng, M.Si");
        cbPicBlsdm.setValue("Pilih PIC BLSDM Komdigi Manado");

        cbStatus.getItems().addAll(
                "Baru", "Dalam Proses", "Review PEMDA 1", "Review BPSDMP Kominfo",
                "Review BPSDMP 1", "Review PEMDA 2", "Review BPSDMP 2",
                "Persiapan TTD Para Pihak", "Selesai"
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

    // ✅✅✅ METHOD INI DI-UPDATE: Simpan PIC BLSDM ke database ✅✅✅
    @FXML
    private void handleSave() {
        if (txtTingkatKerjaSama.getText().trim().isEmpty()) {
            showAlert("Validasi", "Nama Instansi / Tingkat Kerja Sama harus diisi!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = selectedFile != null ? uploadFile(selectedFile) : "";

            // ✅ Ambil Jenis Dokumen
            String jenisDokumenValue = cbJenisDokumen.getValue();
            if ("Lainnya...".equals(jenisDokumenValue)) {
                jenisDokumenValue = txtJenisDokumenLainnya.getText().trim();
                if (jenisDokumenValue.isEmpty()) {
                    showAlert("Validasi", "Jenis dokumen harus diisi!", Alert.AlertType.WARNING);
                    return;
                }
            }

            // ✅ SINGKAT JENIS PERJANJIAN: MoU atau PKS saja
            String jenisPerjanjian = cbJenisPerjanjian.getValue();
            if (jenisPerjanjian != null) {
                if (jenisPerjanjian.contains("MoU")) {
                    jenisPerjanjian = "MoU";
                } else if (jenisPerjanjian.contains("PKS")) {
                    jenisPerjanjian = "PKS";
                }
            }

            // ✅ KATEGORI untuk filter tab
            String kategori = "Non-Pemerintah";

            // ✅ MITRA = Nama instansi yang diketik user
            String mitra = txtTingkatKerjaSama.getText().trim();

            // ✅ PIC BLSDM & PIC MITRA (BERBEDA)
            String picBlsdm = cbPicBlsdm.getValue();  // ✅ PIC BLSDM
            String picMitra = txtPicMitra.getText().trim();  // ✅ PIC MITRA
            String kontakPic = txtKontakMitra.getText().trim();  // ✅ KONTAK PIC MITRA

            // ✅ QUERY dengan kolom pic_blsdm dan kontak_pic
            String sql = "INSERT INTO documents (nomor_dokumen, jenis, mitra, kategori, jenis_dokumen_detail, " +
                    "tanggal_mulai, tanggal_berakhir, pic, kontak_pic, pic_blsdm, file_path, status, keterangan, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String nomorDokumen = txtNomorDokumenBalai.getText().trim();
                if (nomorDokumen.isEmpty()) nomorDokumen = txtNomorDokumenMitra.getText().trim();

                pstmt.setString(1, nomorDokumen);
                pstmt.setString(2, jenisPerjanjian);
                pstmt.setString(3, mitra);
                pstmt.setString(4, kategori);
                pstmt.setString(5, jenisDokumenValue);
                pstmt.setString(6, dpTanggalMulai.getValue() != null ? dpTanggalMulai.getValue().toString() : "");
                pstmt.setString(7, dpTanggalBerakhir.getValue() != null ? dpTanggalBerakhir.getValue().toString() : "");
                pstmt.setString(8, picMitra);        // ✅ PIC MITRA
                pstmt.setString(9, kontakPic);        // ✅ KONTAK PIC MITRA
                pstmt.setString(10, picBlsdm);        // ✅ PIC BLSDM (field baru)
                pstmt.setString(11, filePath);
                pstmt.setString(12, cbStatus.getValue());
                pstmt.setString(13, txtCatatan.getText().trim());
                pstmt.setInt(14, currentUserId);

                pstmt.executeUpdate();
                showAlert("Sukses!", "Dokumen berhasil disimpan.", Alert.AlertType.INFORMATION);
                ((Stage) txtNomorDokumenBalai.getScene().getWindow()).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menyimpan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String uploadFile(File file) {
        try {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Files.copy(file.toPath(), uploadPath.resolve(fileName));
            return uploadPath.resolve(fileName).toString();
        } catch (Exception e) { return ""; }
    }

    @FXML private void handleBack() { ((Stage) txtNomorDokumenBalai.getScene().getWindow()).close(); }

    @FXML private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Batal");
        alert.setContentText("Yakin ingin membatalkan?");
        alert.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) handleBack(); });
    }

    private void showAlert(String t, String m, Alert.AlertType type) {
        new Alert(type, m, ButtonType.OK).showAndWait();
    }
}