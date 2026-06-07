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
import java.util.HashMap;
import java.util.Map;

public class AddPemdaDocumentController {

    @FXML private ComboBox<String> cbJenisPerjanjian;
    @FXML private ComboBox<String> cbTingkatKerjaSama;
    @FXML private VBox boxProvinsi;
    @FXML private ComboBox<String> cbProvinsi;
    @FXML private VBox boxKabKota;
    @FXML private ComboBox<String> cbKabKota;
    @FXML private ComboBox<String> cbJenisDokumen;
    @FXML private VBox boxJenisDokumenLainnya;
    @FXML private TextField txtJenisDokumenLainnya;
    @FXML private ComboBox<String> cbPicBlsdm;
    @FXML private TextField txtNomorDokumenBalai;
    @FXML private TextField txtNomorDokumenPemda;
    @FXML private TextField txtPicPemda;
    @FXML private TextField txtKontakPemda;
    @FXML private TextField txtPemilik;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpTanggalMulai;
    @FXML private DatePicker dpTanggalBerakhir;
    @FXML private TextArea txtCatatan;
    @FXML private Label lblFileName;

    private File selectedFile;
    private int currentUserId;
    private final Map<String, String[]> kabKotaData = new HashMap<>();

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        initKabKotaData();

        cbJenisPerjanjian.getItems().addAll("MoU (Memorandum of Understanding)", "PKS (Perjanjian Kerja Sama)");
        cbJenisPerjanjian.setValue("MoU (Memorandum of Understanding)");

        cbTingkatKerjaSama.getItems().addAll("Pemerintah Provinsi", "Pemerintah Kabupaten/Kota");

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

        cbStatus.getItems().addAll(
                "Baru", "Dalam Proses", "Review PEMDA 1", "Review BPSDMP Kominfo",
                "Review BPSDMP 1", "Review PEMDA 2", "Review BPSDMP 2",
                "Persiapan TTD Para Pihak", "Selesai"
        );
        cbStatus.setValue("Baru");

        cbProvinsi.getItems().addAll("Sulawesi Utara", "Gorontalo", "Sulawesi Tengah", "Maluku Utara");

        cbTingkatKerjaSama.setOnAction(e -> handleTingkatChange());
        cbProvinsi.setOnAction(e -> handleProvinsiChange());
    }

    private void initKabKotaData() {
        kabKotaData.put("Sulawesi Utara", new String[]{
                "Kabupaten Bolaang Mongondow", "Kabupaten Minahasa", "Kabupaten Kepulauan Sangihe",
                "Kabupaten Kepulauan Talaud", "Kabupaten Minahasa Selatan", "Kabupaten Minahasa Utara",
                "Kabupaten Bolaang Mongondow Utara", "Kabupaten Kepulauan Siau Tagulandang Biaro",
                "Kabupaten Minahasa Tenggara", "Kabupaten Bolaang Mongondow Timur", "Kabupaten Bolaang Mongondow Selatan",
                "Kota Manado", "Kota Bitung", "Kota Tomohon", "Kota Kotamobagu"
        });
        kabKotaData.put("Gorontalo", new String[]{
                "Kabupaten Gorontalo", "Kabupaten Boalemo", "Kabupaten Pohuwato", "Kabupaten Bone Bolango", "Kabupaten Gorontalo Utara",
                "Kota Gorontalo"
        });
        kabKotaData.put("Sulawesi Tengah", new String[]{
                "Kabupaten Banggai", "Kabupaten Buol", "Kabupaten Donggala", "Kabupaten Morowali", "Kabupaten Parigi Moutong",
                "Kabupaten Poso", "Kabupaten Sigi", "Kabupaten Tojo Una-Una", "Kabupaten Toli-Toli", "Kabupaten Banggai Kepulauan",
                "Kabupaten Banggai Laut", "Kabupaten Morowali Utara", "Kota Palu"
        });
        kabKotaData.put("Maluku Utara", new String[]{
                "Kabupaten Halmahera Barat", "Kabupaten Halmahera Tengah", "Kabupaten Halmahera Utara",
                "Kabupaten Halmahera Selatan", "Kabupaten Kepulauan Sula", "Kabupaten Halmahera Timur",
                "Kabupaten Pulau Morotai", "Kabupaten Pulau Taliabu",
                "Kota Ternate", "Kota Tidore Kepulauan"
        });
    }

    private void handleTingkatChange() {
        String tingkat = cbTingkatKerjaSama.getValue();
        if ("Pemerintah Provinsi".equals(tingkat)) {
            boxProvinsi.setVisible(true); boxProvinsi.setManaged(true);
            boxKabKota.setVisible(false); boxKabKota.setManaged(false);
            cbKabKota.setValue(null);
        } else if ("Pemerintah Kabupaten/Kota".equals(tingkat)) {
            boxProvinsi.setVisible(true); boxProvinsi.setManaged(true);
            boxKabKota.setVisible(true); boxKabKota.setManaged(true);
        } else {
            boxProvinsi.setVisible(false); boxProvinsi.setManaged(false);
            boxKabKota.setVisible(false); boxKabKota.setManaged(false);
        }
    }

    private void handleProvinsiChange() {
        String provinsi = cbProvinsi.getValue();
        if (provinsi != null && cbKabKota != null) {
            String[] list = kabKotaData.get(provinsi);
            if (list != null) {
                cbKabKota.getItems().clear();
                cbKabKota.getItems().addAll(list);
            }
        }
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih File Dokumen");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Files", "*.doc", "*.docx")
        );
        selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null) lblFileName.setText(selectedFile.getName());
    }

    // ✅✅✅ METHOD INI DI-UPDATE: Simpan KONTAK PIC ✅✅✅
    @FXML
    private void handleSave() {
        if (cbJenisPerjanjian.getValue() == null) {
            showAlert("Validasi", "Jenis perjanjian harus dipilih!", Alert.AlertType.WARNING);
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

            // ✅ FORMAT MITRA LENGKAP sesuai pilihan user
            String mitra = "";
            String tingkat = cbTingkatKerjaSama.getValue();
            if ("Pemerintah Provinsi".equals(tingkat)) {
                mitra = "Pemerintah Provinsi " + cbProvinsi.getValue();
            } else if ("Pemerintah Kabupaten/Kota".equals(tingkat)) {
                mitra = cbKabKota.getValue();
            }

            // ✅ KATEGORI untuk filter tab
            String kategori = "Pemerintah Daerah";

            // ✅ PIC & KONTAK
            String picBlsdm = cbPicBlsdm.getValue();
            String picPemda = txtPicPemda.getText().trim();
            String kontakPic = txtKontakPemda.getText().trim(); // ✅ KONTAK PIC

            // ✅ QUERY dengan kolom kontak_pic
            String sql = "INSERT INTO documents (nomor_dokumen, jenis, mitra, kategori, jenis_dokumen_detail, " +
                    "tanggal_mulai, tanggal_berakhir, pic, kontak_pic, pic_blsdm, file_path, status, keterangan, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String nomorDokumen = txtNomorDokumenBalai.getText().trim();
                if (nomorDokumen.isEmpty()) nomorDokumen = txtNomorDokumenPemda.getText().trim();

                pstmt.setString(1, nomorDokumen);
                pstmt.setString(2, jenisPerjanjian);
                pstmt.setString(3, mitra);
                pstmt.setString(4, kategori);
                pstmt.setString(5, jenisDokumenValue);
                pstmt.setString(6, dpTanggalMulai.getValue() != null ? dpTanggalMulai.getValue().toString() : "");
                pstmt.setString(7, dpTanggalBerakhir.getValue() != null ? dpTanggalBerakhir.getValue().toString() : "");
                pstmt.setString(8, picPemda);        // PIC PEMDA
                pstmt.setString(9, kontakPic);        // ✅ KONTAK PIC
                pstmt.setString(10, picBlsdm);        // PIC BLSdm
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
    @FXML private void handleCancel() { handleBack(); }

    private void showAlert(String t, String m, Alert.AlertType type) {
        new Alert(type, m, ButtonType.OK).showAndWait();
    }
}