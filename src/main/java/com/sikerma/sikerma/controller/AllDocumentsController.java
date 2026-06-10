package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private ObservableList<Document> filteredList = FXCollections.observableArrayList();
    private int currentUserId;
    private String currentFilter = "Pemerintah Daerah";
    private String currentUserRole;
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void setUserData(int userId, String userName, String role) {
        this.currentUserId = userId;
        this.currentUserRole = role;
        initializeTable();
        initializeFilters();
        loadDocuments();
    }

    private void initializeTable() {
        colJenisPerjanjian.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colJenisPerjanjian.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String displayText = item;
                    if (item.contains("(")) {
                        displayText = item.substring(0, item.indexOf("(")).trim();
                    }
                    if (displayText.contains(" ")) {
                        displayText = displayText.split(" ")[0];
                    }

                    setText(displayText);
                    setWrapText(true);
                    setTextAlignment(TextAlignment.CENTER);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 8 5; -fx-font-weight: bold;");
                }
            }
        });
        colJenisPerjanjian.setPrefWidth(100);
        colJenisPerjanjian.setMaxWidth(100);
        colJenisPerjanjian.setMinWidth(100);

        colTingkatKerjaSama.setCellValueFactory(cellData -> {
            Document doc = cellData.getValue();
            if (doc == null) return new javafx.beans.property.SimpleStringProperty("-");
            String mitra = doc.getMitra();
            String kategori = doc.getKategori();

            if (mitra != null && !mitra.isEmpty()) {
                if ("Pemerintah Daerah".equals(kategori)) {
                    if (!mitra.toLowerCase().startsWith("pemerintah")) {
                        mitra = "Pemerintah " + mitra;
                    }
                    mitra = mitra.replace("Kab.", "Kabupaten");
                    mitra = mitra.replace("Kab ", "Kabupaten ");
                }
            }
            return new javafx.beans.property.SimpleStringProperty(mitra != null ? mitra : "-");
        });
        colTingkatKerjaSama.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setTextAlignment(TextAlignment.CENTER);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 8 5;");
                }
            }
        });
        colTingkatKerjaSama.setPrefWidth(180);
        colTingkatKerjaSama.setMaxWidth(180);
        colTingkatKerjaSama.setMinWidth(180);

        colJenisDokumen.setCellValueFactory(new PropertyValueFactory<>("jenisDokumenDetail"));
        colJenisDokumen.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setTextAlignment(TextAlignment.CENTER);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 8 5;");
                }
            }
        });
        colJenisDokumen.setPrefWidth(130);
        colJenisDokumen.setMaxWidth(130);
        colJenisDokumen.setMinWidth(130);

        colPicBpsdmp.setCellValueFactory(cellData -> {
            Document doc = cellData.getValue();
            if (doc == null) return new javafx.beans.property.SimpleStringProperty("-");
            String picBlsdm = doc.getPicBlsdm();
            return new javafx.beans.property.SimpleStringProperty(
                    (picBlsdm != null && !picBlsdm.isEmpty()) ? picBlsdm : "-"
            );
        });
        colPicBpsdmp.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setTextAlignment(TextAlignment.CENTER);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 8 5;");
                }
            }
        });
        colPicBpsdmp.setPrefWidth(120);
        colPicBpsdmp.setMaxWidth(120);
        colPicBpsdmp.setMinWidth(120);

        colPicPemda.setCellValueFactory(cellData -> {
            Document doc = cellData.getValue();
            if (doc == null) return new javafx.beans.property.SimpleStringProperty("-");
            return new javafx.beans.property.SimpleStringProperty(doc.getPic() != null ? doc.getPic() : "-");
        });
        colPicPemda.setCellFactory(column -> new TableCell<Document, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Document doc = getTableView().getItems().get(getTableRow().getIndex());
                    if (doc == null) {
                        setGraphic(null);
                        return;
                    }

                    VBox vbox = new VBox(3);
                    vbox.setAlignment(Pos.CENTER);
                    vbox.setStyle("-fx-padding: 8 0;");

                    Label lblNama = new Label(doc.getPic() != null ? doc.getPic() : "-");
                    lblNama.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e3a8a;");
                    lblNama.setAlignment(Pos.CENTER);
                    lblNama.setTextAlignment(TextAlignment.CENTER);
                    lblNama.setWrapText(true);
                    lblNama.setMaxWidth(110);

                    String kontak = doc.getKontakPic() != null ? doc.getKontakPic() : "-";
                    Label lblKontak = new Label(kontak);
                    lblKontak.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                    lblKontak.setAlignment(Pos.CENTER);
                    lblKontak.setTextAlignment(TextAlignment.CENTER);
                    lblKontak.setWrapText(true);
                    lblKontak.setMaxWidth(110);

                    vbox.getChildren().addAll(lblNama, lblKontak);
                    setGraphic(vbox);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colPicPemda.setPrefWidth(130);
        colPicPemda.setMaxWidth(130);
        colPicPemda.setMinWidth(130);

        colTanggalMulai.setCellValueFactory(cellData -> {
            Document doc = cellData.getValue();
            if (doc == null) return new javafx.beans.property.SimpleStringProperty("-");
            LocalDate date = doc.getTanggalMulai();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-"
            );
        });
        colTanggalMulai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextAlignment(TextAlignment.CENTER);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 8 5;");
                }
            }
        });
        colTanggalMulai.setPrefWidth(110);
        colTanggalMulai.setMaxWidth(110);
        colTanggalMulai.setMinWidth(110);

        colTanggalBerakhir.setCellValueFactory(cellData -> {
            Document doc = cellData.getValue();
            if (doc == null) return new javafx.beans.property.SimpleStringProperty("-");
            LocalDate date = doc.getTanggalBerakhir();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-"
            );
        });
        colTanggalBerakhir.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextAlignment(TextAlignment.CENTER);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 8 5;");
                }
            }
        });
        colTanggalBerakhir.setPrefWidth(110);
        colTanggalBerakhir.setMaxWidth(110);
        colTanggalBerakhir.setMinWidth(110);

        colStatus.setCellValueFactory(cellData -> {
            Document doc = cellData.getValue();
            if (doc == null) return new javafx.beans.property.SimpleStringProperty("-");
            String status = doc.getStatus();
            return new javafx.beans.property.SimpleStringProperty(status != null ? status : "-");
        });
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);

                    String bgColor = "#f1f5f9";
                    String textColor = "#64748b";

                    String s = item.toLowerCase();
                    if (s.equals("baru")) { bgColor = "#dbeafe"; textColor = "#1e40af"; }
                    else if (s.equals("dalam proses")) { bgColor = "#fef9c3"; textColor = "#854d0e"; }
                    else if (s.contains("review bpsdmp")) { bgColor = "#ffedd5"; textColor = "#9a3412"; }
                    else if (s.contains("review pemda")) { bgColor = "#f3e8ff"; textColor = "#6b21a8"; }
                    else if (s.contains("persiapan")) { bgColor = "#e0e7ff"; textColor = "#3730a3"; }
                    else if (s.equals("selesai")) { bgColor = "#dcfce7"; textColor = "#166534"; }
                    else if (s.equals("aktif")) { bgColor = "#d1fae5"; textColor = "#065f46"; }
                    else if (s.equals("kadaluarsa")) { bgColor = "#fee2e2"; textColor = "#991b1b"; }

                    badge.setStyle("-fx-background-color: " + bgColor + "; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-background-radius: 10; " +
                            "-fx-padding: 6 10; " +
                            "-fx-alignment: CENTER;");

                    badge.setAlignment(Pos.CENTER);
                    badge.setTextAlignment(TextAlignment.CENTER);
                    badge.setWrapText(true);
                    badge.setMaxWidth(130);

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStatus.setPrefWidth(150);
        colStatus.setMaxWidth(150);
        colStatus.setMinWidth(150);

        colCatatan.setCellValueFactory(cellData -> {
            Document doc = cellData.getValue();
            if (doc == null) return new javafx.beans.property.SimpleStringProperty("-");
            String ket = doc.getKeterangan();
            if (ket == null || ket.trim().isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
            return new javafx.beans.property.SimpleStringProperty(ket.trim());
        });
        colCatatan.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setTextAlignment(TextAlignment.CENTER);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 8 5;");
                }
            }
        });
        colCatatan.setPrefWidth(100);
        colCatatan.setMaxWidth(100);
        colCatatan.setMinWidth(100);

        colDokumenFinal.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document doc = getTableView().getItems().get(getIndex());
                    if (doc == null || doc.getFilePath() == null || doc.getFilePath().isEmpty()) {
                        setGraphic(null);
                        return;
                    }

                    Label icon = new Label("📄");
                    icon.setStyle("-fx-cursor: hand; -fx-font-size: 20px;");
                    icon.setTooltip(new Tooltip("Lihat Dokumen"));
                    icon.setOnMouseClicked(e -> handleViewDocument(doc));
                    setGraphic(icon);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colDokumenFinal.setPrefWidth(80);
        colDokumenFinal.setMaxWidth(80);
        colDokumenFinal.setMinWidth(80);

        colAksi.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document doc = getTableView().getItems().get(getIndex());
                    if (doc == null) {
                        setGraphic(null);
                        return;
                    }

                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER);

                    boolean isAdmin = currentUserRole != null &&
                            (currentUserRole.equalsIgnoreCase("admin") ||
                                    currentUserRole.equalsIgnoreCase("administrator"));

                    if (isAdmin) {
                        Button btnEdit = new Button("✏️");
                        btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                                "-fx-cursor: hand; -fx-font-size: 14px; -fx-background-radius: 5; " +
                                "-fx-padding: 5 10;");
                        btnEdit.setTooltip(new Tooltip("Edit"));
                        btnEdit.setOnAction(e -> handleEdit(doc));

                        Button btnDelete = new Button("🗑️");
                        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                                "-fx-cursor: hand; -fx-font-size: 14px; -fx-background-radius: 5; " +
                                "-fx-padding: 5 10;");
                        btnDelete.setTooltip(new Tooltip("Hapus"));
                        btnDelete.setOnAction(e -> handleDelete(doc));

                        hbox.getChildren().addAll(btnEdit, btnDelete);
                    }

                    setGraphic(hbox);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colAksi.setPrefWidth(120);
        colAksi.setMaxWidth(120);
        colAksi.setMinWidth(120);

        boolean isAdmin = currentUserRole != null &&
                (currentUserRole.equalsIgnoreCase("admin") ||
                        currentUserRole.equalsIgnoreCase("administrator"));

        if (!isAdmin) {
            colAksi.setVisible(false);
        }

        tableDocuments.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Document item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(empty ? "" : "-fx-min-height: 80px; -fx-pref-height: 80px;");
            }
        });
    }

    private void initializeFilters() {
        cbStatusFilter.getItems().addAll("Semua Status", "Baru", "Dalam Proses", "Review PEMDA 1",
                "Review BPSDMP Kominfo", "Review BPSDMP 1", "Review PEMDA 2",
                "Review BPSDMP 2", "Persiapan TTD Para Pihak", "Selesai");
        cbStatusFilter.setValue("Semua Status");

        cbStatusFilter.setOnAction(e -> applyFilters());
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
                doc.setJenisDokumenDetail(rs.getString("jenis_dokumen_detail"));
                if (rs.getDate("tanggal_mulai") != null)
                    doc.setTanggalMulai(rs.getDate("tanggal_mulai").toLocalDate());
                if (rs.getDate("tanggal_berakhir") != null)
                    doc.setTanggalBerakhir(rs.getDate("tanggal_berakhir").toLocalDate());
                doc.setStatus(rs.getString("status"));
                doc.setPic(rs.getString("pic"));
                doc.setKontakPic(rs.getString("kontak_pic"));
                doc.setPicBlsdm(rs.getString("pic_blsdm"));
                doc.setKeterangan(rs.getString("keterangan"));
                doc.setFilePath(rs.getString("file_path"));

                doc.setNomorDokumenPemda(rs.getString("nomor_dokumen_pemda"));
                doc.setNomorDokumenMitra(rs.getString("nomor_dokumen_mitra"));
                doc.setPemilik(rs.getString("pemilik"));

                documentList.add(doc);
            }

            filteredList = FXCollections.observableArrayList(documentList);
            lblTotalDocs.setText("(" + documentList.size() + ")");
            tableDocuments.setItems(filteredList);
            applyFilters();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String selectedStatus = cbStatusFilter.getValue();
        String keyword = txtSearch.getText().toLowerCase();

        ObservableList<Document> filtered = FXCollections.observableArrayList();

        for (Document doc : documentList) {
            boolean matchStatus = "Semua Status".equals(selectedStatus) ||
                    (doc.getStatus() != null && doc.getStatus().equals(selectedStatus));

            boolean matchSearch = keyword.isEmpty() ||
                    (doc.getNomorDokumen() != null && doc.getNomorDokumen().toLowerCase().contains(keyword)) ||
                    (doc.getMitra() != null && doc.getMitra().toLowerCase().contains(keyword)) ||
                    (doc.getJenis() != null && doc.getJenis().toLowerCase().contains(keyword));

            if (matchStatus && matchSearch) {
                filtered.add(doc);
            }
        }

        tableDocuments.setItems(filtered);
        lblTotalDocs.setText("(" + filtered.size() + ")");
    }

    private void handleViewDocument(Document doc) {
        if (doc.getFilePath() == null || doc.getFilePath().isEmpty()) {
            showAlert("Info", "Dokumen belum diupload.");
            return;
        }

        File file = new File(doc.getFilePath());
        if (!file.exists()) {
            showAlert("Error", "File dokumen tidak ditemukan: " + doc.getFilePath());
            return;
        }

        try {
            java.awt.Desktop.getDesktop().open(file);
        } catch (IOException e) {
            showAlert("Error", "Gagal membuka dokumen: " + e.getMessage());
        }
    }

    @FXML
    private void showPemdaDocuments() {
        currentFilter = "Pemerintah Daerah";
        btnPemda.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        btnNonPemda.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        lblSectionTitle.setText("Instansi Pemerintah Daerah");
        lblSectionDesc.setText("Pengelolaan dokumen kerja sama dengan Pemerintah Daerah beserta perangkat daerah");
        colPicPemda.setText("PIC PEMDA");
        loadDocuments();
    }

    @FXML
    private void showNonPemdaDocuments() {
        currentFilter = "Non-Pemerintah Daerah";
        btnNonPemda.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        btnPemda.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 30; -fx-background-radius: 8;");
        lblSectionTitle.setText("Instansi Non-Pemerintah Daerah");
        lblSectionDesc.setText("Pengelolaan dokumen kerja sama dengan Instansi Non-Pemerintah Daerah");
        colPicPemda.setText("PIC MITRA");
        loadDocuments();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleAddDocument() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            DashboardController controller = loader.getController();
            controller.setUserData(currentUserId, "User", currentUserRole != null ? currentUserRole : "USER");

            stage.show();
            ((Stage) lblSectionTitle.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        if (dashboardController != null) {
            dashboardController.handleDashboard();
        } else {
            Stage stage = (Stage) ((Node) btnPemda).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleEdit(Document doc) {
        try {
            String fxmlFile = "Pemerintah Daerah".equals(doc.getKategori()) ?
                    "/fxml/add_pemda_document.fxml" : "/fxml/add_non_pemda_document.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent formRoot = loader.load();

            Object controller = loader.getController();
            if ("Pemerintah Daerah".equals(doc.getKategori())) {
                if (controller instanceof AddPemdaDocumentController) {
                    ((AddPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                    ((AddPemdaDocumentController) controller).setDashboardController(dashboardController);
                    ((AddPemdaDocumentController) controller).setDocumentData(doc);
                }
            } else {
                if (controller instanceof AddNonPemdaDocumentController) {
                    ((AddNonPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                    ((AddNonPemdaDocumentController) controller).setDashboardController(dashboardController);
                    ((AddNonPemdaDocumentController) controller).setDocumentData(doc);
                }
            }

            if (dashboardController != null && dashboardController.getMainLayout() != null) {
                dashboardController.getMainLayout().setCenter(formRoot);
            } else {
                Stage stage = new Stage();
                stage.setTitle("Edit Dokumen - " + doc.getNomorDokumen());
                stage.setScene(new Scene(formRoot, 1200, 750));
                stage.showAndWait();
                loadDocuments();
            }

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