package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;

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

    // Field untuk conditional
    @FXML private Label lblJenisDokumenLainnya;

    private File selectedFile;
    private int currentUserId;
    private DashboardController dashboardController;
    // ✅ TAMBAHAN: Field untuk mode edit
    private Document editingDocument = null;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    // ✅ TAMBAHAN: Method untuk set data dokumen saat edit
    public void setDocumentData(Document doc) {
        this.editingDocument = doc;

        // Isi form dengan data dokumen
        if (doc.getJenis() != null) {
            if (doc.getJenis().contains("MoU")) {
                cbJenisPerjanjian.setValue("MoU (Memorandum of Understanding)");
            } else if (doc.getJenis().contains("PKS")) {
                cbJenisPerjanjian.setValue("PKS (Perjanjian Kerja Sama)");
            }
        }

        if (doc.getJenisDokumenDetail() != null) {
            if (cbJenisDokumen.getItems().contains(doc.getJenisDokumenDetail())) {
                cbJenisDokumen.setValue(doc.getJenisDokumenDetail());
            } else {
                cbJenisDokumen.setValue("Lainnya...");
                boxJenisDokumenLainnya.setVisible(true);
                boxJenisDokumenLainnya.setManaged(true);
                lblJenisDokumenLainnya.setVisible(true);
                lblJenisDokumenLainnya.setManaged(true);
                txtJenisDokumenLainnya.setText(doc.getJenisDokumenDetail());
            }
        }

        if (doc.getMitra() != null) txtTingkatKerjaSama.setText(doc.getMitra());
        if (doc.getPicBlsdm() != null) cbPicBlsdm.setValue(doc.getPicBlsdm());
        if (doc.getNomorDokumen() != null) txtNomorDokumenBalai.setText(doc.getNomorDokumen());
        if (doc.getPic() != null) txtPicMitra.setText(doc.getPic());
        if (doc.getKontakPic() != null) txtKontakMitra.setText(doc.getKontakPic());
        if (doc.getTanggalMulai() != null) dpTanggalMulai.setValue(doc.getTanggalMulai());
        if (doc.getTanggalBerakhir() != null) dpTanggalBerakhir.setValue(doc.getTanggalBerakhir());
        if (doc.getKeterangan() != null && !doc.getKeterangan().isEmpty()) {
            txtCatatan.setText(doc.getKeterangan());
        }
        if (doc.getFilePath() != null && !doc.getFilePath().isEmpty()) {
            lblFileName.setText(new File(doc.getFilePath()).getName());
        }
        if (doc.getStatus() != null) cbStatus.setValue(doc.getStatus());
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

        cbPicBlsdm.getItems().addAll("Dr. Ahmad Santoso, M.Si", "Dra. Maria Wowor, M.Pd",
                "Ir. John Lengkong, M.T", "Drs. Sarah Tumangkeng, M.Si");
        cbPicBlsdm.setValue("Pilih PIC BLSDM Komdigi Manado");

        cbStatus.getItems().addAll(
                "Baru", "Dalam Proses", "Review PEMDA 1", "Review BPSDMP Kominfo",
                "Review BPSDMP 1", "Review PEMDA 2", "Review BPSDMP 2",
                "Persiapan TTD Para Pihak", "Selesai"
        );
        cbStatus.setValue("Baru");

        // Setup event handler
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Hide conditional fields di awal
        hideConditionalFields();

        // Handle Jenis Dokumen - show/hide text field
        cbJenisDokumen.setOnAction(e -> {
            String selected = cbJenisDokumen.getValue();
            if ("Lainnya...".equals(selected)) {
                lblJenisDokumenLainnya.setVisible(true);
                lblJenisDokumenLainnya.setManaged(true);
                boxJenisDokumenLainnya.setVisible(true);
                boxJenisDokumenLainnya.setManaged(true);
                txtJenisDokumenLainnya.requestFocus();
            } else {
                lblJenisDokumenLainnya.setVisible(false);
                lblJenisDokumenLainnya.setManaged(false);
                boxJenisDokumenLainnya.setVisible(false);
                boxJenisDokumenLainnya.setManaged(false);
                txtJenisDokumenLainnya.setText("");
            }
        });
    }

    private void hideConditionalFields() {
        lblJenisDokumenLainnya.setVisible(false);
        lblJenisDokumenLainnya.setManaged(false);
        boxJenisDokumenLainnya.setVisible(false);
        boxJenisDokumenLainnya.setManaged(false);
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
        if (txtTingkatKerjaSama.getText().trim().isEmpty()) {
            showAlert("Validasi", "Nama Instansi / Tingkat Kerja Sama harus diisi!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = selectedFile != null ? uploadFile(selectedFile) :
                    (editingDocument != null ? editingDocument.getFilePath() : "");

            String jenisDokumenValue = cbJenisDokumen.getValue();
            if ("Lainnya...".equals(jenisDokumenValue)) {
                jenisDokumenValue = txtJenisDokumenLainnya.getText().trim();
                if (jenisDokumenValue.isEmpty()) {
                    showAlert("Validasi", "Jenis dokumen harus diisi!", Alert.AlertType.WARNING);
                    return;
                }
            }

            String jenisPerjanjian = cbJenisPerjanjian.getValue();
            if (jenisPerjanjian != null) {
                if (jenisPerjanjian.contains("MoU")) {
                    jenisPerjanjian = "MoU";
                } else if (jenisPerjanjian.contains("PKS")) {
                    jenisPerjanjian = "PKS";
                }
            }

            String mitra = txtTingkatKerjaSama.getText().trim();
            String picBlsdm = cbPicBlsdm.getValue();
            String picMitra = txtPicMitra.getText().trim();
            String kontakPic = txtKontakMitra.getText().trim();
            String catatan = txtCatatan.getText().trim();

            try (Connection conn = DatabaseConfig.connect()) {

                // ✅ FIX: Cek mode edit atau tambah
                if (editingDocument != null) {
                    // MODE EDIT - UPDATE
                    String sql = "UPDATE documents SET nomor_dokumen = ?, jenis = ?, mitra = ?, " +
                            "jenis_dokumen_detail = ?, tanggal_mulai = ?, tanggal_berakhir = ?, " +
                            "pic = ?, kontak_pic = ?, pic_blsdm = ?, file_path = ?, status = ?, " +
                            "keterangan = ? WHERE id = ?";

                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    String nomorDokumen = txtNomorDokumenBalai.getText().trim();
                    if (nomorDokumen.isEmpty()) nomorDokumen = txtNomorDokumenMitra.getText().trim();

                    pstmt.setString(1, nomorDokumen);
                    pstmt.setString(2, jenisPerjanjian);
                    pstmt.setString(3, mitra);
                    pstmt.setString(4, jenisDokumenValue);
                    pstmt.setString(5, dpTanggalMulai.getValue() != null ? dpTanggalMulai.getValue().toString() : "");
                    pstmt.setString(6, dpTanggalBerakhir.getValue() != null ? dpTanggalBerakhir.getValue().toString() : "");
                    pstmt.setString(7, picMitra);
                    pstmt.setString(8, kontakPic);
                    pstmt.setString(9, picBlsdm);
                    pstmt.setString(10, filePath);
                    pstmt.setString(11, cbStatus.getValue());
                    pstmt.setString(12, catatan.isEmpty() ? null : catatan);
                    pstmt.setInt(13, editingDocument.getId());

                    pstmt.executeUpdate();
                    showAlert("Sukses!", "Dokumen berhasil diupdate.", Alert.AlertType.INFORMATION);

                } else {
                    // MODE TAMBAH - INSERT
                    String sql = "INSERT INTO documents (nomor_dokumen, jenis, mitra, kategori, jenis_dokumen_detail, " +
                            "tanggal_mulai, tanggal_berakhir, pic, kontak_pic, pic_blsdm, file_path, status, keterangan, created_by) " +
                            "VALUES (?, ?, ?, 'Non-Pemerintah', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    String nomorDokumen = txtNomorDokumenBalai.getText().trim();
                    if (nomorDokumen.isEmpty()) nomorDokumen = txtNomorDokumenMitra.getText().trim();

                    pstmt.setString(1, nomorDokumen);
                    pstmt.setString(2, jenisPerjanjian);
                    pstmt.setString(3, mitra);
                    pstmt.setString(4, jenisDokumenValue);
                    pstmt.setString(5, dpTanggalMulai.getValue() != null ? dpTanggalMulai.getValue().toString() : "");
                    pstmt.setString(6, dpTanggalBerakhir.getValue() != null ? dpTanggalBerakhir.getValue().toString() : "");
                    pstmt.setString(7, picMitra);
                    pstmt.setString(8, kontakPic);
                    pstmt.setString(9, picBlsdm);
                    pstmt.setString(10, filePath);
                    pstmt.setString(11, cbStatus.getValue());
                    pstmt.setString(12, catatan.isEmpty() ? null : catatan);
                    pstmt.setInt(13, currentUserId);

                    pstmt.executeUpdate();
                    showAlert("Sukses!", "Dokumen berhasil disimpan.", Alert.AlertType.INFORMATION);
                }

                if (dashboardController != null) {
                    dashboardController.handleDashboard();
                }
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

    @FXML
    private void handleCancel() {
        if (dashboardController != null) {
            dashboardController.handleDashboard();
        }
    }

    private void showAlert(String t, String m, Alert.AlertType type) {
        new Alert(type, m, ButtonType.OK).showAndWait();
    }
}