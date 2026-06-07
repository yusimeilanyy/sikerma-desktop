package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class AddPemdaDocumentController {

    @FXML private ComboBox<String> cbJenisPerjanjian;
    @FXML private ComboBox<String> cbTingkatKerjaSama;
    @FXML private ComboBox<String> cbJenisDokumen;
    @FXML private ComboBox<String> cbPICBlsdm;
    @FXML private TextField txtNomorDokumenBalai;
    @FXML private TextField txtNomorDokumenPemda;
    @FXML private TextField txtPICPemda;
    @FXML private TextField txtKontakPIC;
    @FXML private TextField txtPemilik;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpTanggalMulai;
    @FXML private DatePicker dpTanggalBerakhir;
    @FXML private TextArea txtCatatan;

    // Field conditional
    @FXML private Label lblProvinsi;
    @FXML private VBox boxProvinsi;
    @FXML private ComboBox<String> cbProvinsi;
    @FXML private Label lblKabKota;
    @FXML private VBox boxKabKota;
    @FXML private ComboBox<String> cbKabKota;
    @FXML private Label lblJenisDokumenLainnya;
    @FXML private VBox boxJenisDokumenLainnya;
    @FXML private TextField txtJenisDokumenLainnya;

    private int currentUserId;
    private File selectedFile;
    private DashboardController dashboardController;

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

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        initializeComboBoxes();
        setupEventHandlers();
    }

    private void initializeComboBoxes() {
        cbJenisPerjanjian.getItems().addAll(
                "MoU (Memorandum of Understanding)",
                "PKS (Perjanjian Kerja Sama)"
        );

        cbTingkatKerjaSama.getItems().addAll(
                "Pemerintah Provinsi",
                "Pemerintah Kabupaten/Kota"
        );

        cbProvinsi.getItems().addAll(
                "Sulawesi Utara",
                "Gorontalo",
                "Sulawesi Tengah",
                "Maluku Utara"
        );

        cbJenisDokumen.getItems().addAll(
                "Nota Kesepahaman",
                "Nota Kesepakatan",
                "Surat Pernyataan",
                "Perjanjian Kerjasama",
                "Lainnya..."
        );

        cbPICBlsdm.getItems().addAll(
                "Pilih PIC",
                "Dra. Maria Wowor, M.Pd",
                "Drs. Sarah Tumangkeng, M.Si"
        );
        cbPICBlsdm.setValue("Pilih PIC");

        cbStatus.getItems().addAll(
                "Baru",
                "Dalam Proses",
                "Review BPSDMP 1",
                "Review BPSDMP Kominfo",
                "Review BPSDMP 2",
                "Review PEMDA 1",
                "Review PEMDA 2",
                "Persiapan TTD Para Pihak",
                "Selesai"
        );
        cbStatus.setValue("Baru");
    }

    private void setupEventHandlers() {
        // Sembunyikan semua field conditional di awal
        hideConditionalFields();

        // Handle Tingkat Kerja Sama
        cbTingkatKerjaSama.setOnAction(e -> {
            String selected = cbTingkatKerjaSama.getValue();

            // Reset dulu
            hideConditionalFields();
            cbProvinsi.setValue(null);
            cbKabKota.setValue(null);
            cbKabKota.getItems().clear();

            if (selected != null) {
                if ("Pemerintah Provinsi".equals(selected)) {
                    // Tampilkan hanya Provinsi
                    lblProvinsi.setVisible(true);
                    lblProvinsi.setManaged(true);
                    boxProvinsi.setVisible(true);
                    boxProvinsi.setManaged(true);
                } else if ("Pemerintah Kabupaten/Kota".equals(selected)) {
                    // Tampilkan Provinsi DAN Kabupaten/Kota
                    lblProvinsi.setVisible(true);
                    lblProvinsi.setManaged(true);
                    boxProvinsi.setVisible(true);
                    boxProvinsi.setManaged(true);
                    lblKabKota.setVisible(true);
                    lblKabKota.setManaged(true);
                    boxKabKota.setVisible(true);
                    boxKabKota.setManaged(true);
                }
            }
        });

        // Handle Provinsi - update Kabupaten/Kota
        cbProvinsi.setOnAction(e -> {
            String provinsi = cbProvinsi.getValue();
            if (provinsi != null && boxKabKota.isVisible()) {
                cbKabKota.getItems().clear();
                switch (provinsi) {
                    case "Sulawesi Utara":
                        cbKabKota.setItems(kabSulut);
                        break;
                    case "Gorontalo":
                        cbKabKota.setItems(kabGorontalo);
                        break;
                    case "Sulawesi Tengah":
                        cbKabKota.setItems(kabSulteng);
                        break;
                    case "Maluku Utara":
                        cbKabKota.setItems(kabMalut);
                        break;
                }
            }
        });

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
        // Sembunyikan Provinsi
        lblProvinsi.setVisible(false);
        lblProvinsi.setManaged(false);
        boxProvinsi.setVisible(false);
        boxProvinsi.setManaged(false);

        // Sembunyikan Kabupaten/Kota
        lblKabKota.setVisible(false);
        lblKabKota.setManaged(false);
        boxKabKota.setVisible(false);
        boxKabKota.setManaged(false);

        // Sembunyikan Jenis Dokumen Lainnya
        lblJenisDokumenLainnya.setVisible(false);
        lblJenisDokumenLainnya.setManaged(false);
        boxJenisDokumenLainnya.setVisible(false);
        boxJenisDokumenLainnya.setManaged(false);
    }

    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Dokumen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedFile = file;
            showAlert("Info", "File dipilih: " + file.getName());
        }
    }

    @FXML
    private void handleBatal() {
        if (dashboardController != null) {
            dashboardController.handleDashboard();
        }
    }

    @FXML
    private void handleSimpanDokumen() {
        if (validateForm()) {
            try {
                String mitra = "";
                if ("Pemerintah Provinsi".equals(cbTingkatKerjaSama.getValue())) {
                    mitra = "Pemerintah Provinsi " + cbProvinsi.getValue();
                } else if ("Pemerintah Kabupaten/Kota".equals(cbTingkatKerjaSama.getValue())) {
                    mitra = cbKabKota.getValue();
                }

                String jenisDokumen = cbJenisDokumen.getValue();
                if ("Lainnya...".equals(jenisDokumen)) {
                    jenisDokumen = txtJenisDokumenLainnya.getText().trim();
                }

                String sql = "INSERT INTO documents (jenis, mitra, kategori, jenis_dokumen_detail, " +
                        "pic_blsdm, nomor_dokumen, tanggal_mulai, tanggal_berakhir, status, " +
                        "pic, kontak_pic, keterangan, created_at) " +
                        "VALUES (?, ?, 'Pemerintah Daerah', ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

                Connection conn = DatabaseConfig.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, cbJenisPerjanjian.getValue());
                pstmt.setString(2, mitra);
                pstmt.setString(3, jenisDokumen);
                pstmt.setString(4, cbPICBlsdm.getValue());
                pstmt.setString(5, txtNomorDokumenBalai.getText());

                LocalDate tglMulai = dpTanggalMulai.getValue();
                pstmt.setDate(6, tglMulai != null ? java.sql.Date.valueOf(tglMulai) : null);

                LocalDate tglBerakhir = dpTanggalBerakhir.getValue();
                pstmt.setDate(7, tglBerakhir != null ? java.sql.Date.valueOf(tglBerakhir) : null);

                pstmt.setString(8, cbStatus.getValue());
                pstmt.setString(9, txtPICPemda.getText());
                pstmt.setString(10, txtKontakPIC.getText());
                pstmt.setString(11, txtPemilik.getText());

                pstmt.executeUpdate();
                conn.close();

                showAlert("Sukses", "Dokumen berhasil disimpan!");
                if (dashboardController != null) {
                    dashboardController.handleDashboard();
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal menyimpan dokumen: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        if (cbJenisPerjanjian.getValue() == null || cbJenisPerjanjian.getValue().isEmpty()) {
            showAlert("Error", "Jenis Perjanjian harus dipilih!");
            return false;
        }
        if (cbTingkatKerjaSama.getValue() == null || cbTingkatKerjaSama.getValue().isEmpty()) {
            showAlert("Error", "Tingkat Kerja Sama harus dipilih!");
            return false;
        }
        if (cbProvinsi.getValue() == null || cbProvinsi.getValue().isEmpty()) {
            showAlert("Error", "Provinsi harus dipilih!");
            return false;
        }
        if ("Pemerintah Kabupaten/Kota".equals(cbTingkatKerjaSama.getValue())) {
            if (cbKabKota.getValue() == null || cbKabKota.getValue().isEmpty()) {
                showAlert("Error", "Kabupaten/Kota harus dipilih!");
                return false;
            }
        }
        if (cbJenisDokumen.getValue() == null || cbJenisDokumen.getValue().isEmpty()) {
            showAlert("Error", "Jenis Dokumen harus dipilih!");
            return false;
        }
        if ("Lainnya...".equals(cbJenisDokumen.getValue())) {
            if (txtJenisDokumenLainnya.getText().trim().isEmpty()) {
                showAlert("Error", "Jenis Dokumen Lainnya harus diisi!");
                return false;
            }
        }
        if (txtNomorDokumenBalai.getText().isEmpty()) {
            showAlert("Error", "Nomor Dokumen Balai harus diisi!");
            return false;
        }
        if (txtPICPemda.getText().isEmpty()) {
            showAlert("Error", "PIC Pemerintah Daerah harus diisi!");
            return false;
        }
        if (dpTanggalMulai.getValue() == null) {
            showAlert("Error", "Tanggal Mulai harus dipilih!");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}