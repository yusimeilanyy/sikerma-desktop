package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AllDocumentsController {

    @FXML private Button btnPemda;
    @FXML private Button btnNonPemda;
    @FXML private Label lblSectionTitle;
    @FXML private Label lblSectionDesc;
    @FXML private Label lblTotalDocs;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TextField txtSearch;
    @FXML private TableView<Document> tableDocuments;
    @FXML private TableColumn<Document, String> colJenisPerjanjian;
    @FXML private TableColumn<Document, String> colTingkatKerjaSama;
    @FXML private TableColumn<Document, String> colJenisDokumen;
    @FXML private TableColumn<Document, String> colPicBpsdmp;
    @FXML private TableColumn<Document, String> colPicPemda;
    @FXML private TableColumn<Document, String> colTanggalMulai;
    @FXML private TableColumn<Document, String> colTanggalBerakhir;
    @FXML private TableColumn<Document, String> colStatus;
    @FXML private TableColumn<Document, String> colCatatan;
    @FXML private TableColumn<Document, Void> colDokumenFinal;
    @FXML private TableColumn<Document, Void> colAksi;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private int currentUserId;
    private String currentFilter = "Pemerintah Daerah";

    public void setUserData(int userId, String userName) {
        this.currentUserId = userId;
        initializeTable();
        initializeFilters();
        loadDocuments();
    }

    private void initializeTable() {
        colJenisPerjanjian.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colTingkatKerjaSama.setCellValueFactory(cellData -> {
            String mitra = cellData.getValue().getMitra();
            if (mitra != null && mitra.contains("Provinsi")) {
                return new javafx.beans.property.SimpleStringProperty("Pemerintah Provinsi");
            } else if (mitra != null && (mitra.contains("Kab.") || mitra.contains("Kota"))) {
                return new javafx.beans.property.SimpleStringProperty("Pemerintah Kabupaten/Kota");
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colJenisDokumen.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colPicBpsdmp.setCellValueFactory(new PropertyValueFactory<>("pic"));
        colPicPemda.setCellValueFactory(cellData -> {
            String pic = cellData.getValue().getPic();
            return new javafx.beans.property.SimpleStringProperty(pic != null ? pic : "-");
        });
        colTanggalMulai.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggalMulai();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-");
        });
        colTanggalBerakhir.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggalBerakhir();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-");
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCatatan.setCellValueFactory(cellData -> {
            String ket = cellData.getValue().getKeterangan();
            return new javafx.beans.property.SimpleStringProperty(ket != null && !ket.isEmpty() ? ket : "-");
        });

        colDokumenFinal.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Label icon = new Label("📄");
                    icon.setStyle("-fx-cursor: hand; -fx-font-size: 18px;");
                    icon.setOnMouseClicked(e -> {
                        Document doc = getTableView().getItems().get(getIndex());
                        showAlert("Info", "Download dokumen: " + doc.getNomorDokumen());
                    });
                    setGraphic(icon);
                }
            }
        });

        colAksi.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document doc = getTableView().getItems().get(getIndex());

                    Button btnEdit = new Button("✏️");
                    btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
                    btnEdit.setOnAction(e -> handleEdit(doc));

                    Button btnDelete = new Button("🗑️");
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
                    btnDelete.setOnAction(e -> handleDelete(doc));

                    setGraphic(new HBox(5, btnEdit, btnDelete));
                }
            }
        });
    }

    private void initializeFilters() {
        cbStatusFilter.getItems().addAll("Semua Status", "Baru", "Dalam Proses", "Review PEMDA 1",
                "Review BPSDMP Kominfo", "Review BPSDMP 1", "Review PEMDA 2",
                "Review BPSDMP 2", "Persiapan TTD Para Pihak", "Selesai");
        cbStatusFilter.setValue("Semua Status");
    }

    private void loadDocuments() {
        String sql = "SELECT * FROM documents WHERE is_deleted = 0";

        if ("Pemerintah Daerah".equals(currentFilter)) {
            sql += " AND kategori = 'Pemerintah Daerah'";
        } else {
            sql += " AND kategori = 'Non-Pemerintah'";
        }

        sql += " ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            documentList.clear();
            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getInt("id"));
                doc.setNomorDokumen(rs.getString("nomor_dokumen"));
                doc.setJenis(rs.getString("jenis"));
                doc.setMitra(rs.getString("mitra"));
                doc.setKategori(rs.getString("kategori"));
                if (rs.getDate("tanggal_mulai") != null)
                    doc.setTanggalMulai(rs.getDate("tanggal_mulai").toLocalDate());
                if (rs.getDate("tanggal_berakhir") != null)
                    doc.setTanggalBerakhir(rs.getDate("tanggal_berakhir").toLocalDate());
                doc.setStatus(rs.getString("status"));
                doc.setPic(rs.getString("pic"));
                doc.setKeterangan(rs.getString("keterangan"));
                doc.setFilePath(rs.getString("file_path"));
                documentList.add(doc);
            }

            lblTotalDocs.setText("(" + documentList.size() + ")");
            tableDocuments.setItems(documentList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    @FXML
    private void showPemdaDocuments() {
        currentFilter = "Pemerintah Daerah";
        btnPemda.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        btnNonPemda.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        lblSectionTitle.setText("Instansi Pemerintah Daerah");
        lblSectionDesc.setText("Pengelolaan dokumen kerja sama dengan Pemerintah Daerah beserta perangkat daerah");
        loadDocuments();
    }

    @FXML
    private void showNonPemdaDocuments() {
        currentFilter = "Non-Pemerintah Daerah";
        btnNonPemda.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        btnPemda.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        lblSectionTitle.setText("Instansi Non-Pemerintah Daerah");
        lblSectionDesc.setText("Pengelolaan dokumen kerja sama dengan Instansi Non-Pemerintah Daerah");
        loadDocuments();
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().toLowerCase();
        ObservableList<Document> filtered = FXCollections.observableArrayList();

        for (Document doc : documentList) {
            if (doc.getNomorDokumen().toLowerCase().contains(keyword) ||
                    doc.getMitra().toLowerCase().contains(keyword) ||
                    doc.getJenis().toLowerCase().contains(keyword)) {
                filtered.add(doc);
            }
        }
        tableDocuments.setItems(filtered);
        lblTotalDocs.setText("(" + filtered.size() + ")");
    }

    @FXML
    private void handleAddDocument() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            DashboardController controller = loader.getController();
            controller.setUserData(currentUserId, "User", "USER");

            stage.show();
            ((Stage) lblSectionTitle.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ METHOD BARU: Kembali ke Dashboard
    @FXML
    private void handleBack() {
        Stage stage = (Stage) ((Node) btnPemda).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleEdit(Document doc) {
        try {
            String fxmlFile = "Pemerintah Daerah".equals(currentFilter) ?
                    "/fxml/edit_pemda_document.fxml" : "/fxml/edit_non_pemda_document.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 800, 700));
            stage.setTitle("Edit Dokumen - " + doc.getNomorDokumen());

            stage.showAndWait();
            loadDocuments();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form edit: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(Document doc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setContentText("Yakin ingin menghapus dokumen: " + doc.getNomorDokumen() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.connect()) {
                    String sql = "UPDATE documents SET is_deleted = 1 WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, doc.getId());
                    pstmt.executeUpdate();

                    showAlert("Sukses", "Dokumen berhasil dihapus.");
                    loadDocuments();
                } catch (Exception e) {
                    showAlert("Error", "Gagal menghapus: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}