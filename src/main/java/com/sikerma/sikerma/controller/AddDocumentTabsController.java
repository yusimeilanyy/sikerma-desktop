package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.LocalDate;

public class AddDocumentTabsController {

    // Tab buttons
    @FXML private Button btnTabPemda;
    @FXML private Button btnTabNonPemda;
    @FXML private VBox boxPemda;
    @FXML private VBox boxNonPemda;

    // PEMDA Fields
    @FXML private ComboBox<String> cbJenisPerjanjianPemda;
    @FXML private ComboBox<String> cbTingkatKerjaSamaPemda;
    @FXML private Label lblProvinsiPemda;
    @FXML private VBox boxProvinsiPemda;
    @FXML private ComboBox<String> cbProvinsiPemda;
    @FXML private Label lblKabKotaPemda;
    @FXML private VBox boxKabKotaPemda;
    @FXML private ComboBox<String> cbKabKotaPemda;
    @FXML private ComboBox<String> cbJenisDokumenPemda;
    @FXML private Label lblJenisDokumenLainnyaPemda;
    @FXML private VBox boxJenisDokumenLainnyaPemda;
    @FXML private TextField txtJenisDokumenLainnyaPemda;
    @FXML private ComboBox<String> cbPICBlsdmPemda;
    @FXML private TextField txtNomorDokumenBalaiPemda;
    @FXML private TextField txtNomorDokumenPemda;
    @FXML private TextField txtPICPemda;
    @FXML private TextField txtKontakPICPemda;
    @FXML private ComboBox<String> cbStatusPemda;
    @FXML private DatePicker dpTanggalMulaiPemda;
    @FXML private DatePicker dpTanggalBerakhirPemda;
    @FXML private TextArea txtCatatanPemda;
    @FXML private Label lblFileNamePemda;

    // Non-PEMDA Fields
    @FXML private ComboBox<String> cbJenisPerjanjianNonPemda;
    @FXML private ComboBox<String> cbJenisDokumenNonPemda;
    @FXML private Label lblJenisDokumenLainnyaNonPemda;
    @FXML private VBox boxJenisDokumenLainnyaNonPemda;
    @FXML private TextField txtJenisDokumenLainnyaNonPemda;
    @FXML private TextField txtTingkatKerjaSamaNonPemda;
    @FXML private ComboBox<String> cbPicBlsdmNonPemda;
    @FXML private TextField txtNomorDokumenBalaiNonPemda;
    @FXML private TextField txtNomorDokumenMitra;
    @FXML private TextField txtPicMitra;
    @FXML private TextField txtKontakMitra;
    @FXML private ComboBox<String> cbStatusNonPemda;
    @FXML private DatePicker dpTanggalMulaiNonPemda;
    @FXML private DatePicker dpTanggalBerakhirNonPemda;
    @FXML private TextArea txtCatatanNonPemda;
    @FXML private Label lblFileNameNonPemda;

    private int currentUserId;
    private DashboardController dashboardController;
    private File selectedFilePemda;
    private File selectedFileNonPemda;
    private boolean isPemdaActive = true;

    // Data lists
    private ObservableList<String> kabSulut = FXCollections.observableArrayList(
            "Kab. Minahasa", "Kab. Minahasa Selatan", "Kab. Minahasa Utara",
            "Kab. Minahasa Tenggara", "Kota Manado", "Kota Bitung",
            "Kota Tomohon", "Kota Kotamobagu"
    );

    private ObservableList<String> kabGorontalo = FXCollections.observableArrayList(
            "Kab. Gorontalo", "Kab. Gorontalo Utara", "Kab. Bone Bolango",
            "Kab. Pohuwato", "Kab. Boalemo", "Kota Gorontalo"
    );

    private ObservableList<String> kabSulteng = FXCollections.observableArrayList(
            "Kab. Donggala", "Kab. Toli-Toli", "Kab. Buol",
            "Kab. Morowali", "Kab. Banggai", "Kab. Parigi Moutong",
            "Kab. Tojo Una-Una", "Kab. Sigi", "Kab. Banggai Kepulauan",
            "Kab. Banggai Laut", "Kab. Morowali Utara", "Kota Palu"
    );

    private ObservableList<String> kabMalut = FXCollections.observableArrayList(
            "Kab. Halmahera Barat", "Kab. Halmahera Tengah", "Kab. Halmahera Utara",
            "Kab. Halmahera Selatan", "Kab. Kepulauan Sula", "Kab. Halmahera Timur",
            "Kab. Pulau Morotai", "Kab. Pulau Taliabu", "Kota Ternate", "Kota Tidore Kepulauan"
    );

    @FXML
    public void initialize() {
        initializeComboBoxes();
        setupEventHandlers();
        showPemdaTab();
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    private void initializeComboBoxes() {
        // PEMDA ComboBoxes
        cbJenisPerjanjianPemda.getItems().addAll(
                "MoU (Memorandum of Understanding)",
                "PKS (Perjanjian Kerja Sama)"
        );

        cbTingkatKerjaSamaPemda.getItems().addAll(
                "Pemerintah Provinsi",
                "Pemerintah Kabupaten/Kota"
        );

        cbProvinsiPemda.getItems().addAll(
                "Sulawesi Utara",
                "Gorontalo",
                "Sulawesi Tengah",
                "Maluku Utara"
        );

        cbJenisDokumenPemda.getItems().addAll(
                "Nota Kesepahaman",
                "Nota Kesepakatan",
                "Surat Pernyataan",
                "Perjanjian Kerjasama",
                "Lainnya..."
        );

        cbPICBlsdmPemda.getItems().addAll(
                "Pilih PIC",
                "Dra. Maria Wowor, M.Pd",
                "Drs. Sarah Tumangkeng, M.Si"
        );
        cbPICBlsdmPemda.setValue("Pilih PIC");

        cbStatusPemda.getItems().addAll(
                "Baru", "Dalam Proses", "Review BPSDMP 1", "Review BPSDMP Kominfo",
                "Review BPSDMP 2", "Review PEMDA 1", "Review PEMDA 2",
                "Persiapan TTD Para Pihak", "Selesai"
        );
        cbStatusPemda.setValue("Baru");

        // Non-PEMDA ComboBoxes
        cbJenisPerjanjianNonPemda.getItems().addAll(
                "MoU (Memorandum of Understanding)",
                "PKS (Perjanjian Kerja Sama)"
        );
        cbJenisPerjanjianNonPemda.setValue("MoU (Memorandum of Understanding)");

        cbJenisDokumenNonPemda.getItems().addAll(
                "Pilih jenis dokumen",
                "Nota Kesepahaman",
                "Perjanjian Kerjasama",
                "Surat Pernyataan",
                "Nota Kesepakatan",
                "Lainnya..."
        );
        cbJenisDokumenNonPemda.setValue("Pilih jenis dokumen");

        cbPicBlsdmNonPemda.getItems().addAll(
                "Dr. Ahmad Santoso, M.Si",
                "Dra. Maria Wowor, M.Pd",
                "Ir. John Lengkong, M.T",
                "Drs. Sarah Tumangkeng, M.Si"
        );
        cbPicBlsdmNonPemda.setValue("Pilih PIC BLSDM Komdigi Manado");

        cbStatusNonPemda.getItems().addAll(
                "Baru", "Dalam Proses", "Review PEMDA 1", "Review BPSDMP Kominfo",
                "Review BPSDMP 1", "Review PEMDA 2", "Review BPSDMP 2",
                "Persiapan TTD Para Pihak", "Selesai"
        );
        cbStatusNonPemda.setValue("Baru");
    }

    private void setupEventHandlers() {
        // PEMDA Event Handlers
        cbTingkatKerjaSamaPemda.setOnAction(e -> {
            String selected = cbTingkatKerjaSamaPemda.getValue();
            hideConditionalFieldsPemda();
            cbProvinsiPemda.setValue(null);
            cbKabKotaPemda.setValue(null);
            cbKabKotaPemda.getItems().clear();

            if (selected != null) {
                if ("Pemerintah Provinsi".equals(selected)) {
                    lblProvinsiPemda.setVisible(true);
                    lblProvinsiPemda.setManaged(true);
                    boxProvinsiPemda.setVisible(true);
                    boxProvinsiPemda.setManaged(true);
                } else if ("Pemerintah Kabupaten/Kota".equals(selected)) {
                    lblProvinsiPemda.setVisible(true);
                    lblProvinsiPemda.setManaged(true);
                    boxProvinsiPemda.setVisible(true);
                    boxProvinsiPemda.setManaged(true);
                    lblKabKotaPemda.setVisible(true);
                    lblKabKotaPemda.setManaged(true);
                    boxKabKotaPemda.setVisible(true);
                    boxKabKotaPemda.setManaged(true);
                }
            }
        });

        cbProvinsiPemda.setOnAction(e -> {
            String provinsi = cbProvinsiPemda.getValue();
            if (provinsi != null && boxKabKotaPemda.isVisible()) {
                cbKabKotaPemda.getItems().clear();
                switch (provinsi) {
                    case "Sulawesi Utara": cbKabKotaPemda.setItems(kabSulut); break;
                    case "Gorontalo": cbKabKotaPemda.setItems(kabGorontalo); break;
                    case "Sulawesi Tengah": cbKabKotaPemda.setItems(kabSulteng); break;
                    case "Maluku Utara": cbKabKotaPemda.setItems(kabMalut); break;
                }
            }
        });

        cbJenisDokumenPemda.setOnAction(e -> {
            String selected = cbJenisDokumenPemda.getValue();
            if ("Lainnya...".equals(selected)) {
                lblJenisDokumenLainnyaPemda.setVisible(true);
                lblJenisDokumenLainnyaPemda.setManaged(true);
                boxJenisDokumenLainnyaPemda.setVisible(true);
                boxJenisDokumenLainnyaPemda.setManaged(true);
                txtJenisDokumenLainnyaPemda.requestFocus();
            } else {
                lblJenisDokumenLainnyaPemda.setVisible(false);
                lblJenisDokumenLainnyaPemda.setManaged(false);
                boxJenisDokumenLainnyaPemda.setVisible(false);
                boxJenisDokumenLainnyaPemda.setManaged(false);
                txtJenisDokumenLainnyaPemda.setText("");
            }
        });

        // Non-PEMDA Event Handlers
        cbJenisDokumenNonPemda.setOnAction(e -> {
            String selected = cbJenisDokumenNonPemda.getValue();
            if ("Lainnya...".equals(selected)) {
                lblJenisDokumenLainnyaNonPemda.setVisible(true);
                lblJenisDokumenLainnyaNonPemda.setManaged(true);
                boxJenisDokumenLainnyaNonPemda.setVisible(true);
                boxJenisDokumenLainnyaNonPemda.setManaged(true);
                txtJenisDokumenLainnyaNonPemda.requestFocus();
            } else {
                lblJenisDokumenLainnyaNonPemda.setVisible(false);
                lblJenisDokumenLainnyaNonPemda.setManaged(false);
                boxJenisDokumenLainnyaNonPemda.setVisible(false);
                boxJenisDokumenLainnyaNonPemda.setManaged(false);
                txtJenisDokumenLainnyaNonPemda.setText("");
            }
        });
    }

    private void hideConditionalFieldsPemda() {
        lblProvinsiPemda.setVisible(false);
        lblProvinsiPemda.setManaged(false);
        boxProvinsiPemda.setVisible(false);
        boxProvinsiPemda.setManaged(false);

        lblKabKotaPemda.setVisible(false);
        lblKabKotaPemda.setManaged(false);
        boxKabKotaPemda.setVisible(false);
        boxKabKotaPemda.setManaged(false);
    }

    @FXML
    private void showPemdaTab() {
        isPemdaActive = true;
        boxPemda.setVisible(true);
        boxPemda.setManaged(true);
        boxNonPemda.setVisible(false);
        boxNonPemda.setManaged(false);
        btnTabPemda.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 13px;");
        btnTabNonPemda.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 13px;");
    }

    @FXML
    private void showNonPemdaTab() {
        isPemdaActive = false;
        boxPemda.setVisible(false);
        boxPemda.setManaged(false);
        boxNonPemda.setVisible(true);
        boxNonPemda.setManaged(true);
        btnTabNonPemda.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 13px;");
        btnTabPemda.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand; -fx-font-size: 13px;");
    }

    @FXML
    private void handleChooseFilePemda() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Dokumen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Files", "*.doc", "*.docx")
        );
        selectedFilePemda = fileChooser.showOpenDialog(null);
        if (selectedFilePemda != null) {
            lblFileNamePemda.setText(selectedFilePemda.getName());
        }
    }

    @FXML
    private void handleChooseFileNonPemda() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Dokumen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Files", "*.doc", "*.docx")
        );
        selectedFileNonPemda = fileChooser.showOpenDialog(null);
        if (selectedFileNonPemda != null) {
            lblFileNameNonPemda.setText(selectedFileNonPemda.getName());
        }
    }

    @FXML
    private void handleSave() {
        if (isPemdaActive) {
            savePemdaDocument();
        } else {
            saveNonPemdaDocument();
        }
    }

    private void savePemdaDocument() {
        if (!validatePemdaForm()) return;

        try {
            String mitra = "";
            if ("Pemerintah Provinsi".equals(cbTingkatKerjaSamaPemda.getValue())) {
                mitra = "Pemerintah Provinsi " + cbProvinsiPemda.getValue();
            } else if ("Pemerintah Kabupaten/Kota".equals(cbTingkatKerjaSamaPemda.getValue())) {
                mitra = cbKabKotaPemda.getValue();
            }

            String jenisDokumen = cbJenisDokumenPemda.getValue();
            if ("Lainnya...".equals(jenisDokumen)) {
                jenisDokumen = txtJenisDokumenLainnyaPemda.getText().trim();
            }

            String filePath = "";
            if (selectedFilePemda != null) {
                filePath = uploadFile(selectedFilePemda);
            }

            Connection conn = DatabaseConfig.connect();
            String sql = "INSERT INTO documents (jenis, mitra, kategori, jenis_dokumen_detail, " +
                    "pic_blsdm, nomor_dokumen, tanggal_mulai, tanggal_berakhir, status, " +
                    "pic, kontak_pic, keterangan, file_path, created_at) " +
                    "VALUES (?, ?, 'Pemerintah Daerah', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cbJenisPerjanjianPemda.getValue());
            pstmt.setString(2, mitra);
            pstmt.setString(3, jenisDokumen);
            pstmt.setString(4, cbPICBlsdmPemda.getValue());
            pstmt.setString(5, txtNomorDokumenBalaiPemda.getText());

            LocalDate tglMulai = dpTanggalMulaiPemda.getValue();
            pstmt.setDate(6, tglMulai != null ? java.sql.Date.valueOf(tglMulai) : null);

            LocalDate tglBerakhir = dpTanggalBerakhirPemda.getValue();
            pstmt.setDate(7, tglBerakhir != null ? java.sql.Date.valueOf(tglBerakhir) : null);

            pstmt.setString(8, cbStatusPemda.getValue());
            pstmt.setString(9, txtPICPemda.getText());
            pstmt.setString(10, txtKontakPICPemda.getText());

            String catatan = txtCatatanPemda.getText().trim();
            pstmt.setString(11, catatan.isEmpty() ? null : catatan);
            pstmt.setString(12, filePath);

            pstmt.executeUpdate();
            conn.close();

            showAlert("Sukses", "Dokumen Pemerintah Daerah berhasil disimpan!");
            if (dashboardController != null) {
                dashboardController.handleDashboard();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menyimpan dokumen: " + e.getMessage());
        }
    }

    private void saveNonPemdaDocument() {
        if (txtTingkatKerjaSamaNonPemda.getText().trim().isEmpty()) {
            showAlert("Validasi", "Nama Instansi / Tingkat Kerja Sama harus diisi!");
            return;
        }

        try {
            String filePath = "";
            if (selectedFileNonPemda != null) {
                filePath = uploadFile(selectedFileNonPemda);
            }

            String jenisDokumenValue = cbJenisDokumenNonPemda.getValue();
            if ("Lainnya...".equals(jenisDokumenValue)) {
                jenisDokumenValue = txtJenisDokumenLainnyaNonPemda.getText().trim();
                if (jenisDokumenValue.isEmpty()) {
                    showAlert("Validasi", "Jenis dokumen harus diisi!");
                    return;
                }
            }

            String jenisPerjanjian = cbJenisPerjanjianNonPemda.getValue();
            if (jenisPerjanjian != null) {
                if (jenisPerjanjian.contains("MoU")) jenisPerjanjian = "MoU";
                else if (jenisPerjanjian.contains("PKS")) jenisPerjanjian = "PKS";
            }

            String mitra = txtTingkatKerjaSamaNonPemda.getText().trim();
            String picBlsdm = cbPicBlsdmNonPemda.getValue();
            String picMitra = txtPicMitra.getText().trim();
            String kontakPic = txtKontakMitra.getText().trim();
            String catatan = txtCatatanNonPemda.getText().trim();

            Connection conn = DatabaseConfig.connect();
            String sql = "INSERT INTO documents (nomor_dokumen, jenis, mitra, kategori, jenis_dokumen_detail, " +
                    "tanggal_mulai, tanggal_berakhir, pic, kontak_pic, pic_blsdm, file_path, status, keterangan, created_by) " +
                    "VALUES (?, ?, ?, 'Non-Pemerintah', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            String nomorDokumen = txtNomorDokumenBalaiNonPemda.getText().trim();
            if (nomorDokumen.isEmpty()) nomorDokumen = txtNomorDokumenMitra.getText().trim();

            pstmt.setString(1, nomorDokumen);
            pstmt.setString(2, jenisPerjanjian);
            pstmt.setString(3, mitra);
            pstmt.setString(4, jenisDokumenValue);
            pstmt.setString(5, dpTanggalMulaiNonPemda.getValue() != null ? dpTanggalMulaiNonPemda.getValue().toString() : "");
            pstmt.setString(6, dpTanggalBerakhirNonPemda.getValue() != null ? dpTanggalBerakhirNonPemda.getValue().toString() : "");
            pstmt.setString(7, picMitra);
            pstmt.setString(8, kontakPic);
            pstmt.setString(9, picBlsdm);
            pstmt.setString(10, filePath);
            pstmt.setString(11, cbStatusNonPemda.getValue());
            pstmt.setString(12, catatan.isEmpty() ? null : catatan);
            pstmt.setInt(13, currentUserId);

            pstmt.executeUpdate();
            conn.close();

            showAlert("Sukses", "Dokumen Non-Pemerintah berhasil disimpan!");
            if (dashboardController != null) {
                dashboardController.handleDashboard();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menyimpan: " + e.getMessage());
        }
    }

    private boolean validatePemdaForm() {
        if (cbJenisPerjanjianPemda.getValue() == null || cbJenisPerjanjianPemda.getValue().isEmpty()) {
            showAlert("Error", "Jenis Perjanjian harus dipilih!");
            return false;
        }
        if (cbTingkatKerjaSamaPemda.getValue() == null || cbTingkatKerjaSamaPemda.getValue().isEmpty()) {
            showAlert("Error", "Tingkat Kerja Sama harus dipilih!");
            return false;
        }
        if (cbProvinsiPemda.getValue() == null || cbProvinsiPemda.getValue().isEmpty()) {
            showAlert("Error", "Provinsi harus dipilih!");
            return false;
        }
        if ("Pemerintah Kabupaten/Kota".equals(cbTingkatKerjaSamaPemda.getValue())) {
            if (cbKabKotaPemda.getValue() == null || cbKabKotaPemda.getValue().isEmpty()) {
                showAlert("Error", "Kabupaten/Kota harus dipilih!");
                return false;
            }
        }
        if (cbJenisDokumenPemda.getValue() == null || cbJenisDokumenPemda.getValue().isEmpty()) {
            showAlert("Error", "Jenis Dokumen harus dipilih!");
            return false;
        }
        if ("Lainnya...".equals(cbJenisDokumenPemda.getValue())) {
            if (txtJenisDokumenLainnyaPemda.getText().trim().isEmpty()) {
                showAlert("Error", "Jenis Dokumen Lainnya harus diisi!");
                return false;
            }
        }
        if (txtNomorDokumenBalaiPemda.getText().isEmpty()) {
            showAlert("Error", "Nomor Dokumen Balai harus diisi!");
            return false;
        }
        if (txtPICPemda.getText().isEmpty()) {
            showAlert("Error", "PIC Pemerintah Daerah harus diisi!");
            return false;
        }
        if (dpTanggalMulaiPemda.getValue() == null) {
            showAlert("Error", "Tanggal Mulai harus dipilih!");
            return false;
        }
        return true;
    }

    private String uploadFile(File file) {
        try {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Files.copy(file.toPath(), uploadPath.resolve(fileName));
            return uploadPath.resolve(fileName).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @FXML
    private void handleCancel() {
        if (dashboardController != null) {
            dashboardController.handleDashboard();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}