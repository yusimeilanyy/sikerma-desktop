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

    // Data Kabupaten/Kota per Provinsi
    private final Map<String, String[]> kabKotaData = new HashMap<>();

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        // Inisialisasi Data Kabupaten/Kota
        initKabKotaData();

        // Isi ComboBox
        cbJenisPerjanjian.getItems().addAll("MoU (Memorandum of Understanding)", "PKS (Perjanjian Kerja Sama)");
        cbJenisPerjanjian.setValue("MoU (Memorandum of Understanding)");

        cbTingkatKerjaSama.getItems().addAll("Pemerintah Provinsi", "Pemerintah Kabupaten/Kota");

        // ✅ JENIS DOKUMEN DENGAN OPSI BARU
        cbJenisDokumen.getItems().addAll(
                "Pilih jenis dokumen",
                "Nota Kesepahaman",
                "Perjanjian Kerjasama",
                "Surat Pernyataan",
                "Nota Kesepakatan",
                "Lainnya..."
        );
        cbJenisDokumen.setValue("Pilih jenis dokumen");

        // ✅ EVENT: Tampilkan TextField jika pilih "Lainnya..."
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

        // ✅ STATUS LENGKAP SESUAI GAMBAR
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

        // Provinsi
        cbProvinsi.getItems().addAll("Sulawesi Utara", "Gorontalo", "Sulawesi Tengah", "Maluku Utara");

        // Event Listener untuk Tingkat Kerja Sama
        cbTingkatKerjaSama.setOnAction(e -> handleTingkatChange());

        // Event Listener untuk Provinsi (update Kab/Kota)
        cbProvinsi.setOnAction(e -> handleProvinsiChange());
    }

    private void initKabKotaData() {
        // Sulawesi Utara
        kabKotaData.put("Sulawesi Utara", new String[]{
                "Kab. Bolaang Mongondow", "Kab. Minahasa", "Kab. Kepulauan Sangihe",
                "Kab. Kepulauan Talaud", "Kab. Minahasa Selatan", "Kab. Minahasa Utara",
                "Kab. Bolaang Mongondow Utara", "Kab. Kepulauan Siau Tagulandang Biaro",
                "Kab. Minahasa Tenggara", "Kab. Bolaang Mongondow Timur", "Kab. Bolaang Mongondow Selatan",
                "Kota Manado", "Kota Bitung", "Kota Tomohon", "Kota Kotamobagu"
        });

        // Gorontalo
        kabKotaData.put("Gorontalo", new String[]{
                "Kab. Gorontalo", "Kab. Boalemo", "Kab. Pohuwato", "Kab. Bone Bolango", "Kab. Gorontalo Utara",
                "Kota Gorontalo"
        });

        // Sulawesi Tengah
        kabKotaData.put("Sulawesi Tengah", new String[]{
                "Kab. Banggai", "Kab. Buol", "Kab. Donggala", "Kab. Morowali", "Kab. Parigi Moutong",
                "Kab. Poso", "Kab. Sigi", "Kab. Tojo Una-Una", "Kab. Toli-Toli", "Kab. Banggai Kepulauan",
                "Kab. Banggai Laut", "Kab. Morowali Utara",
                "Kota Palu"
        });

        // Maluku Utara
        kabKotaData.put("Maluku Utara", new String[]{
                "Kab. Halmahera Barat", "Kab. Halmahera Tengah", "Kab. Halmahera Utara",
                "Kab. Halmahera Selatan", "Kab. Kepulauan Sula", "Kab. Halmahera Timur",
                "Kab. Pulau Morotai", "Kab. Pulau Taliabu",
                "Kota Ternate", "Kota Tidore Kepulauan"
        });
    }

    private void handleTingkatChange() {
        String tingkat = cbTingkatKerjaSama.getValue();

        if ("Pemerintah Provinsi".equals(tingkat)) {
            // Tampilkan Provinsi, sembunyikan Kab/Kota
            boxProvinsi.setVisible(true);
            boxProvinsi.setManaged(true);
            boxKabKota.setVisible(false);
            boxKabKota.setManaged(false);
            cbKabKota.setValue(null);
        } else if ("Pemerintah Kabupaten/Kota".equals(tingkat)) {
            // Tampilkan Provinsi dan Kab/Kota
            boxProvinsi.setVisible(true);
            boxProvinsi.setManaged(true);
            boxKabKota.setVisible(true);
            boxKabKota.setManaged(true);
        } else {
            // Sembunyikan keduanya
            boxProvinsi.setVisible(false);
            boxProvinsi.setManaged(false);
            boxKabKota.setVisible(false);
            boxKabKota.setManaged(false);
        }
    }

    private void handleProvinsiChange() {
        String provinsi = cbProvinsi.getValue();
        if (provinsi != null && cbKabKota != null) {
            String[] kabKotaList = kabKotaData.get(provinsi);
            if (kabKotaList != null) {
                cbKabKota.getItems().clear();
                cbKabKota.getItems().addAll(kabKotaList);
            }
        }
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
        if (cbJenisPerjanjian.getValue() == null) {
            showAlert("Validasi", "Jenis perjanjian harus dipilih!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = selectedFile != null ? uploadFile(selectedFile) : "";

            // ✅ TENTUKAN JENIS DOKUMEN (dari combo atau textfield)
            String jenisDokumenValue = cbJenisDokumen.getValue();
            if ("Lainnya...".equals(jenisDokumenValue)) {
                jenisDokumenValue = txtJenisDokumenLainnya.getText().trim();
                if (jenisDokumenValue.isEmpty()) {
                    showAlert("Validasi", "Jenis dokumen harus diisi!", Alert.AlertType.WARNING);
                    return;
                }
            }

            String status = cbStatus.getValue();

            // Tentukan mitra berdasarkan tingkat
            String mitra = "";
            if ("Pemerintah Provinsi".equals(cbTingkatKerjaSama.getValue())) {
                mitra = "Provinsi " + cbProvinsi.getValue();
            } else if ("Pemerintah Kabupaten/Kota".equals(cbTingkatKerjaSama.getValue())) {
                mitra = cbKabKota.getValue() + ", " + cbProvinsi.getValue();
            }

            String sql = "INSERT INTO documents (nomor_dokumen, jenis, mitra, kategori, tanggal_mulai, " +
                    "tanggal_berakhir, pic, file_path, status, keterangan, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String nomorDokumen = txtNomorDokumenBalai.getText().trim();
                if (nomorDokumen.isEmpty()) nomorDokumen = txtNomorDokumenPemda.getText().trim();

                pstmt.setString(1, nomorDokumen);
                pstmt.setString(2, cbJenisPerjanjian.getValue());
                pstmt.setString(3, mitra);
                pstmt.setString(4, jenisDokumenValue); // ✅ Simpan jenis dokumen
                pstmt.setString(5, dpTanggalMulai.getValue() != null ? dpTanggalMulai.getValue().toString() : "");
                pstmt.setString(6, dpTanggalBerakhir.getValue() != null ? dpTanggalBerakhir.getValue().toString() : "");
                pstmt.setString(7, txtPicPemda.getText().trim());
                pstmt.setString(8, filePath);
                pstmt.setString(9, status); // ✅ Simpan status yang dipilih
                pstmt.setString(10, txtCatatan.getText().trim());
                pstmt.setInt(11, currentUserId);

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

    private String determineStatus(LocalDate end) {
        if (end == null) return "Aktif";
        return end.isBefore(LocalDate.now()) ? "Kadaluarsa" : "Aktif";
    }

    @FXML private void handleBack() { ((Stage) txtNomorDokumenBalai.getScene().getWindow()).close(); }
    @FXML private void handleCancel() { handleBack(); }

    private void showAlert(String t, String m, Alert.AlertType type) {
        new Alert(type, m, ButtonType.OK).showAndWait();
    }
}