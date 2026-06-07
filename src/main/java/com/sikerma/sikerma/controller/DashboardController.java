package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;

public class DashboardController {

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTotalMou;
    @FXML private Label lblTotalPks;
    @FXML private Label lblActive;
    @FXML private Label lblExpired;
    @FXML private Label lblMendesak;
    @FXML private Label lblPeringatan;
    @FXML private Label lblTotalAlert;
    @FXML private Label lblMouActive;
    @FXML private Label lblPksActive;
    @FXML private Label lblActiveDetail;
    @FXML private Label lblExpiredDetail;
    @FXML private Label lblTotalDocs;
    @FXML private Label lblPageInfo;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbJenis;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<String> cbPerPage;

    @FXML private TableView<Document> tableDocuments;
    @FXML private TableColumn<Document, Integer> colNo;
    @FXML private TableColumn<Document, String> colJenis;
    @FXML private TableColumn<Document, String> colNomor;
    @FXML private TableColumn<Document, String> colMitra;
    @FXML private TableColumn<Document, LocalDate> colTanggalMulai;
    @FXML private TableColumn<Document, LocalDate> colTanggal;
    @FXML private TableColumn<Document, String> colStatus;

    @FXML private Button btnDashboard;
    @FXML private Button btnTambah;
    @FXML private Button btnSemuaDokumen;
    @FXML private Button btnPerpanjangan;
    @FXML private Button btnNotifikasi;
    @FXML private Button btnPage1;
    @FXML private Button btnPage2;
    @FXML private Button btnPage3;
    @FXML private Button btnPage4;
    @FXML private Button btnLogout;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private int currentUserId;
    private int currentPage = 1;
    private int totalPages = 1;
    private int itemsPerPage = 5;
    private BorderPane mainLayout;

    public void setUserData(int userId, String userName, String role) {
        this.currentUserId = userId;
        lblUserName.setText(userName);
        lblUserRole.setText(role.toUpperCase());

        initializeComboBox();
        initializeTable();
        loadDashboardData();
    }

    public void setMainLayout(BorderPane layout) {
        this.mainLayout = layout;
    }

    private void initializeComboBox() {
        cbJenis.getItems().addAll("Semua Jenis", "MoU", "PKS");
        cbJenis.setValue("Semua Jenis");

        cbStatus.getItems().addAll("Semua Status", "Baru", "Dalam Proses", "Review BPSDMP 1",
                "Review BPSDMP Kominfo", "Review BPSDMP 2", "Review PEMDA 1",
                "Review PEMDA 2", "Persiapan TTD Para Pihak", "Selesai",
                "Aktif", "Perlu Perhatian", "Kadaluarsa");
        cbStatus.setValue("Semua Status");

        cbPerPage.getItems().addAll("5 / halaman", "10 / halaman", "25 / halaman");
        cbPerPage.setValue("5 / halaman");
        cbPerPage.setOnAction(e -> {
            String selected = cbPerPage.getValue();
            if (selected.equals("5 / halaman")) itemsPerPage = 5;
            else if (selected.equals("10 / halaman")) itemsPerPage = 10;
            else if (selected.equals("25 / halaman")) itemsPerPage = 25;
            currentPage = 1;
            loadDashboardData();
        });
    }

    private void initializeTable() {
        colNo.setResizable(false);
        colJenis.setResizable(false);
        colNomor.setResizable(false);
        colMitra.setResizable(false);
        colTanggalMulai.setResizable(false);
        colTanggal.setResizable(false);
        colStatus.setResizable(false);

        colNo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else {
                    setText(String.valueOf(getIndex() + 1 + (currentPage - 1) * itemsPerPage));
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 12 5;");
                }
            }
        });

        colNomor.setCellValueFactory(new PropertyValueFactory<>("nomorDokumen"));
        colNomor.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569; -fx-padding: 12 5;");
                }
            }
        });

        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colJenis.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String displayText = item;
                    if (item.contains("(")) displayText = item.substring(0, item.indexOf("(")).trim();
                    if (displayText.contains(" ")) displayText = displayText.split(" ")[0];

                    Label badge = new Label(displayText);
                    badge.setStyle(getJenisStyle(displayText) + "-fx-padding: 6 10; -fx-background-radius: 6; -fx-font-weight: bold;");
                    badge.setAlignment(Pos.CENTER);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colMitra.setCellValueFactory(new PropertyValueFactory<>("mitra"));
        colMitra.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String formattedMitra = item;
                    if (!item.toLowerCase().startsWith("pemerintah") &&
                            (item.contains("Provinsi") || item.contains("Kab.") || item.contains("Kabupaten") || item.contains("Kota"))) {
                        formattedMitra = "Pemerintah " + item;
                    }
                    formattedMitra = formattedMitra.replace("Kab.", "Kabupaten");
                    formattedMitra = formattedMitra.replace("Kab ", "Kabupaten ");

                    Label lbl = new Label(formattedMitra);
                    lbl.setWrapText(true);
                    lbl.setTextAlignment(TextAlignment.CENTER);
                    lbl.setAlignment(Pos.CENTER);
                    lbl.setStyle("-fx-text-fill: #475569; -fx-padding: 5 0;");

                    VBox box = new VBox(lbl);
                    box.setAlignment(Pos.CENTER);

                    setGraphic(box);
                }
            }
        });

        colTanggalMulai.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        colTanggalMulai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(formatTanggalIndonesia(item));
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569; -fx-padding: 12 5;");
                }
            }
        });

        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalBerakhir"));
        colTanggal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(formatTanggalIndonesia(item));
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569; -fx-padding: 12 5;");
                }
            }
        });

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(getStatusStyle(item) +
                            "-fx-padding: 8 10; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 11px;");
                    badge.setWrapText(true);
                    badge.setTextAlignment(TextAlignment.CENTER);
                    badge.setAlignment(Pos.CENTER);

                    VBox box = new VBox(badge);
                    box.setAlignment(Pos.CENTER);
                    box.setMaxWidth(130);

                    setGraphic(box);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tableDocuments.setFixedCellSize(75);
        tableDocuments.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
    }

    private String getJenisStyle(String jenis) {
        if (jenis == null) return "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
        String j = jenis.toLowerCase();
        if (j.contains("pks")) return "-fx-background-color: #ffedd5; -fx-text-fill: #c2410c;";
        if (j.contains("mou")) return "-fx-background-color: #d1fae5; -fx-text-fill: #047857;";
        return "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
    }

    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";

        String s = status.toLowerCase();
        if (s.equals("baru")) return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
        if (s.equals("dalam proses")) return "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
        if (s.contains("review bpsdmp kominfo")) return "-fx-background-color: #f3e8ff; -fx-text-fill: #7c3aed;";
        if (s.contains("review pemda")) return "-fx-background-color: #fce7f3; -fx-text-fill: #db2777;";
        if (s.contains("review bpsdmp")) return "-fx-background-color: #d1fae5; -fx-text-fill: #047857;";
        if (s.contains("persiapan")) return "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
        if (s.equals("selesai")) return "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
        if (s.equals("aktif")) return "-fx-background-color: #d1fae5; -fx-text-fill: #047857;";
        if (s.equals("perlu perhatian")) return "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
        if (s.equals("kadaluarsa")) return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;";

        return "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
    }

    private String formatTanggalIndonesia(LocalDate date) {
        if (date == null) return "-";
        String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        return date.getDayOfMonth() + " " + bulan[date.getMonthValue() - 1] + " " + date.getYear();
    }

    private void loadDashboardData() {
        String countSql = "SELECT COUNT(*) FROM documents WHERE is_deleted = 0";
        int totalItems = 0;

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement countStmt = conn.prepareStatement(countSql);
             ResultSet rs = countStmt.executeQuery()) {
            if (rs.next()) {
                totalItems = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int offset = (currentPage - 1) * itemsPerPage;

        String sql = "SELECT * FROM documents WHERE is_deleted = 0 ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemsPerPage);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            documentList.clear();
            int totalMou = 0, totalPks = 0, active = 0, expired = 0, mendesak = 0, peringatan = 0;
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getInt("id"));
                doc.setNomorDokumen(rs.getString("nomor_dokumen"));
                doc.setJenis(rs.getString("jenis"));
                doc.setMitra(rs.getString("mitra"));

                if (rs.getDate("tanggal_mulai") != null)
                    doc.setTanggalMulai(rs.getDate("tanggal_mulai").toLocalDate());
                if (rs.getDate("tanggal_berakhir") != null)
                    doc.setTanggalBerakhir(rs.getDate("tanggal_berakhir").toLocalDate());

                doc.setStatus(rs.getString("status"));
                documentList.add(doc);

                if ("MOU".equals(doc.getJenis())) totalMou++;
                if ("PKS".equals(doc.getJenis())) totalPks++;

                if (doc.getTanggalBerakhir() != null && doc.getTanggalBerakhir().isBefore(today)) {
                    expired++;
                } else {
                    active++;
                }

                String status = doc.getStatus();
                if (status != null) {
                    if (status.equals("Perlu Perhatian") || status.equals("Kadaluarsa")) {
                        mendesak++;
                    } else if (status.contains("Review")) {
                        peringatan++;
                    }
                }
            }

            lblTotalMou.setText(String.valueOf(totalMou));
            lblTotalPks.setText(String.valueOf(totalPks));
            lblActive.setText(String.valueOf(active));
            lblExpired.setText(String.valueOf(expired));
            lblMouActive.setText(getMouActiveCount() + " aktif");
            lblPksActive.setText(getPksActiveCount() + " aktif");
            lblActiveDetail.setText(active + " aktif dari " + totalItems + " dokumen");
            lblExpiredDetail.setText(expired + " kadaluarsa dari " + totalItems + " dokumen");

            lblMendesak.setText(String.valueOf(mendesak));
            lblPeringatan.setText(String.valueOf(peringatan));
            lblTotalAlert.setText(String.valueOf(mendesak + peringatan));

            lblTotalDocs.setText("Total : " + totalItems);
            int start = offset + 1;
            int end = Math.min(offset + itemsPerPage, totalItems);
            lblPageInfo.setText("Menampilkan " + start + " - " + end + " dari " + totalItems + " dokumen");

            tableDocuments.setItems(documentList);
            updatePaginationButtons();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getMouActiveCount() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Document doc : documentList) {
            if ("MOU".equals(doc.getJenis()) && doc.getTanggalBerakhir() != null && !doc.getTanggalBerakhir().isBefore(today)) {
                count++;
            }
        }
        return count;
    }

    private int getPksActiveCount() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Document doc : documentList) {
            if ("PKS".equals(doc.getJenis()) && doc.getTanggalBerakhir() != null && !doc.getTanggalBerakhir().isBefore(today)) {
                count++;
            }
        }
        return count;
    }

    private void updatePaginationButtons() {
        Button[] pageButtons = {btnPage1, btnPage2, btnPage3, btnPage4};
        String activeStyle = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;";

        for (int i = 0; i < pageButtons.length; i++) {
            int pageNum = i + 1;
            if (pageNum <= totalPages) {
                pageButtons[i].setVisible(true);
                pageButtons[i].setManaged(true);
                if (pageNum == currentPage) {
                    pageButtons[i].setStyle(activeStyle);
                } else {
                    pageButtons[i].setStyle(inactiveStyle);
                }
            } else {
                pageButtons[i].setVisible(false);
                pageButtons[i].setManaged(false);
            }
        }
    }

    // ✅ PERBAIKAN: Jangan load ulang FXML, cukup refresh data
    @FXML
    public void handleDashboard() {
        currentPage = 1;
        loadDashboardData();
        updateMenuStyle(btnDashboard);
    }

    @FXML
    private void handleAddDocument() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Pemerintah Daerah",
                "Pemerintah Daerah", "Non-Pemerintah Daerah");
        dialog.setTitle("Pilih Jenis Dokumen");
        dialog.setHeaderText("Silakan pilih jenis mitra dokumen:");
        dialog.setContentText("Jenis Mitra:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                String jenis = result.get();
                String fxmlFile = jenis.equals("Pemerintah Daerah") ?
                        "/fxml/add_pemda_document.fxml" : "/fxml/add_non_pemda_document.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent formRoot = loader.load();

                Object controller = loader.getController();
                if (controller instanceof AddPemdaDocumentController) {
                    ((AddPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                    ((AddPemdaDocumentController) controller).setDashboardController(this);
                } else if (controller instanceof AddNonPemdaDocumentController) {
                    ((AddNonPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                    ((AddNonPemdaDocumentController) controller).setDashboardController(this);
                }

                if (mainLayout != null) {
                    mainLayout.setCenter(formRoot);
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka form: " + e.getMessage());
            }
        }
        updateMenuStyle(btnTambah);
    }

    @FXML
    private void handleViewDocuments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/all_documents.fxml"));
            Parent root = loader.load();

            AllDocumentsController controller = loader.getController();
            controller.setUserData(currentUserId, lblUserName.getText());
            controller.setDashboardController(this);

            if (mainLayout != null) {
                mainLayout.setCenter(root);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka halaman dokumen: " + e.getMessage());
        }
        updateMenuStyle(btnSemuaDokumen);
    }

    @FXML private void handleRenewal() {
        showAlert("Info", "Fitur perpanjangan akan dibuat...");
        updateMenuStyle(btnPerpanjangan);
    }

    @FXML private void handleNotifications() {
        showAlert("Info", "Fitur notifikasi akan dibuat...");
        updateMenuStyle(btnNotifikasi);
    }

    @FXML private void handleUsers() {
        showAlert("Info", "Manajemen User (Admin Only)");
    }

    @FXML private void handleSearch() {
        String keyword = txtSearch.getText().toLowerCase();
        String jenisFilter = cbJenis.getValue();
        String statusFilter = cbStatus.getValue();

        ObservableList<Document> filtered = FXCollections.observableArrayList();

        for (Document doc : documentList) {
            boolean matchKeyword = doc.getNomorDokumen().toLowerCase().contains(keyword) ||
                    doc.getMitra().toLowerCase().contains(keyword);
            boolean matchJenis = jenisFilter.equals("Semua Jenis") || doc.getJenis().equals(jenisFilter);
            boolean matchStatus = statusFilter.equals("Semua Status") || doc.getStatus().equals(statusFilter);

            if (matchKeyword && matchJenis && matchStatus) {
                filtered.add(doc);
            }
        }
        tableDocuments.setItems(filtered);
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadDashboardData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadDashboardData();
        }
    }

    @FXML
    private void handlePageClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        int clickedPage = Integer.parseInt(clickedButton.getText());
        if (clickedPage >= 1 && clickedPage <= totalPages) {
            currentPage = clickedPage;
            loadDashboardData();
        }
    }

    @FXML private void handleLogout() {
        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.close();
    }

    private void updateMenuStyle(Button activeButton) {
        String activeStyle = "-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #475569; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

        Button[] buttons = {btnDashboard, btnTambah, btnSemuaDokumen, btnPerpanjangan, btnNotifikasi};
        for (Button btn : buttons) {
            if (btn == activeButton) {
                btn.setStyle(activeStyle);
            } else {
                btn.setStyle(inactiveStyle);
            }
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}