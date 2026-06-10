package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import com.sikerma.sikerma.model.RenewalHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TindakLanjutController {

    @FXML private Label lblTotalDokumen;
    @FXML private Label lblPageInfo;

    @FXML private TextField txtSearch;
    @FXML private TableView<Document> tableDokumen;
    @FXML private TableColumn<Document, Integer> colNo;
    @FXML private TableColumn<Document, String> colJenis;
    @FXML private TableColumn<Document, String> colNomorDokumen;
    @FXML private TableColumn<Document, String> colMitra;
    @FXML private TableColumn<Document, LocalDate> colTanggalBerakhir;
    @FXML private TableColumn<Document, String> colStatus;
    @FXML private TableColumn<Document, Void> colAksi;

    @FXML private Button btnDashboard;
    @FXML private Button btnTambah;
    @FXML private Button btnSemuaDokumen;
    @FXML private Button btnTindakLanjut;
    @FXML private Button btnUser;
    @FXML private Button btnLogout;
    @FXML private Button btnPrevPage;
    @FXML private Button btnPage1;
    @FXML private Button btnPage2;
    @FXML private Button btnPage3;
    @FXML private Button btnNextPage;

    @FXML private VBox detailPanel;
    @FXML private Button btnCloseDetail;
    @FXML private Label lblDetailJenis;
    @FXML private Label lblDetailNomor;
    @FXML private Label lblDetailMitra;
    @FXML private Label lblDetailTanggalMulai;
    @FXML private Label lblDetailTanggalBerakhir;
    @FXML private Label lblDetailStatus;
    @FXML private DatePicker dpTanggalBaru;
    @FXML private TextArea txtCatatan;
    @FXML private Button btnPerpanjang;
    @FXML private ScrollPane scrollRiwayat;
    @FXML private VBox riwayatContainer;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private ObservableList<Document> allExpiredDocuments = FXCollections.observableArrayList();
    private int currentUserId;
    private String currentUserName;
    private String currentUserRole;
    private Document selectedDocument;
    private BorderPane mainLayout;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    public void setUserData(int userId, String userName, String role) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.currentUserRole = role;
        initializeTable();
        loadExpiredDocuments();

        // TAMBAHAN: Listener untuk fitur pencarian
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDocuments(newValue);
        });
    }

    public void setMainLayout(BorderPane layout) {
        this.mainLayout = layout;
    }

    // TAMBAHAN: Method untuk fitur pencarian
    private void searchDocuments(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            updateTableDisplay();
            return;
        }

        String search = searchText.toLowerCase();
        ObservableList<Document> filteredList = FXCollections.observableArrayList();

        for (Document doc : allExpiredDocuments) {
            boolean match = false;

            if (doc.getNomorDokumen() != null &&
                    doc.getNomorDokumen().toLowerCase().contains(search)) {
                match = true;
            }

            if (doc.getMitra() != null &&
                    doc.getMitra().toLowerCase().contains(search)) {
                match = true;
            }

            if (doc.getJenis() != null &&
                    doc.getJenis().toLowerCase().contains(search)) {
                match = true;
            }

            if (match) {
                filteredList.add(doc);
            }
        }

        documentList.clear();
        documentList.addAll(filteredList);
        tableDokumen.setItems(documentList);

        if (filteredList.isEmpty()) {
            lblPageInfo.setText("Tidak ada dokumen yang ditemukan");
        } else {
            lblPageInfo.setText("Menampilkan " + filteredList.size() + " dari " + allExpiredDocuments.size() + " dokumen");
        }
    }

    private void initializeTable() {
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
                    String style = displayText.toLowerCase().contains("pks")
                            ? "-fx-background-color: #ffedd5; -fx-text-fill: #c2410c; -fx-padding: 6 10; -fx-background-radius: 6; -fx-font-weight: bold;"
                            : "-fx-background-color: #d1fae5; -fx-text-fill: #047857; -fx-padding: 6 10; -fx-background-radius: 6; -fx-font-weight: bold;";
                    badge.setStyle(style);
                    badge.setAlignment(Pos.CENTER);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colNomorDokumen.setCellValueFactory(new PropertyValueFactory<>("nomorDokumen"));
        colNomorDokumen.setCellFactory(column -> new TableCell<>() {
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
                    lbl.setStyle("-fx-text-fill: #475569; -fx-padding: 2 5; -fx-font-size: 12px;");
                    lbl.setPrefWidth(170);

                    VBox box = new VBox(lbl);
                    box.setAlignment(Pos.CENTER);
                    box.setSpacing(2);

                    setGraphic(box);
                    setText(null);
                }
            }
        });

        colTanggalBerakhir.setCellValueFactory(new PropertyValueFactory<>("tanggalBerakhir"));
        colTanggalBerakhir.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(4);
                    Label tanggalLabel = new Label(formatTanggalIndonesia(item));
                    tanggalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

                    Document doc = getTableView().getItems().get(getIndex());
                    if (doc.getTanggalBerakhir() != null) {
                        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(doc.getTanggalBerakhir(), LocalDate.now());
                        if (daysOverdue > 0) {
                            Label overdueLabel = new Label("(" + daysOverdue + " hari lewat)");
                            overdueLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");
                            box.getChildren().addAll(tanggalLabel, overdueLabel);
                        } else {
                            box.getChildren().add(tanggalLabel);
                        }
                    }
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                    setText(null);
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
                    badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 8 10; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 11px;");
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

        // MODIFIKASI: Kolom Aksi dengan role checking
        colAksi.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    VBox buttonBox = new VBox(8);
                    buttonBox.setAlignment(Pos.CENTER);

                    // TAMBAHAN: Cek apakah user adalah Admin
                    boolean isAdmin = currentUserRole != null &&
                            (currentUserRole.equalsIgnoreCase("admin") ||
                                    currentUserRole.equalsIgnoreCase("administrator"));

                    if (isAdmin) {
                        // Admin bisa Perpanjang dan lihat Riwayat
                        Button btnPerpanjang = new Button("🔄 Perpanjang");
                        btnPerpanjang.setStyle("-fx-background-color: #14b8a6; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
                        btnPerpanjang.setOnAction(e -> handlePerpanjangFromTable(getTableView().getItems().get(getIndex())));

                        Button btnRiwayat = new Button("🕐 Riwayat");
                        btnRiwayat.setStyle("-fx-background-color: #06b6d4; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
                        btnRiwayat.setOnAction(e -> handleRiwayatFromTable(getTableView().getItems().get(getIndex())));

                        buttonBox.getChildren().addAll(btnPerpanjang, btnRiwayat);
                    } else {
                        // Staff/PIC hanya bisa lihat Riwayat
                        Button btnRiwayat = new Button("🕐 Riwayat");
                        btnRiwayat.setStyle("-fx-background-color: #06b6d4; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
                        btnRiwayat.setOnAction(e -> handleRiwayatFromTable(getTableView().getItems().get(getIndex())));

                        buttonBox.getChildren().add(btnRiwayat);
                    }

                    setGraphic(buttonBox);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tableDokumen.setFixedCellSize(90);
        tableDokumen.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
    }

    private void loadExpiredDocuments() {
        allExpiredDocuments.clear();
        String sql = "SELECT * FROM documents WHERE is_deleted = 0 AND tanggal_berakhir < CURDATE() ORDER BY tanggal_berakhir ASC";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

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

                doc.setStatus("Kadaluarsa");
                allExpiredDocuments.add(doc);
            }

            totalPages = (int) Math.ceil((double) allExpiredDocuments.size() / itemsPerPage);
            if (totalPages == 0) totalPages = 1;
            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            updateTableDisplay();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat dokumen: " + e.getMessage());
        }
    }

    private void updateTableDisplay() {
        documentList.clear();
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allExpiredDocuments.size());

        for (int i = start; i < end; i++) {
            documentList.add(allExpiredDocuments.get(i));
        }

        tableDokumen.setItems(documentList);

        int startDisplay = start + 1;
        int endDisplay = end;
        lblPageInfo.setText("Menampilkan " + startDisplay + " - " + endDisplay + " dari " + allExpiredDocuments.size() + " dokumen");

        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        Button[] pageButtons = {btnPage1, btnPage2, btnPage3};
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

    private void handlePerpanjangFromTable(Document doc) {
        selectedDocument = doc;
        navigateToPerpanjangPage(doc);
    }

    private void navigateToPerpanjangPage(Document doc) {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/perpanjang_dokumen.fxml"));
                Parent root = loader.load();
                PerpanjangDokumenController controller = loader.getController();
                controller.setDocumentData(doc);
                controller.setMainLayout(mainLayout);
                controller.setUserData(currentUserId, currentUserName, currentUserRole);
                mainLayout.setCenter(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka halaman perpanjangan: " + e.getMessage());
            }
        }
    }

    private void handleRiwayatFromTable(Document doc) {
        selectedDocument = doc;
        navigateToRiwayatPage(doc);
    }

    private void navigateToRiwayatPage(Document doc) {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/riwayat_perpanjangan.fxml"));
                Parent root = loader.load();
                RiwayatPerpanjanganController controller = loader.getController();
                controller.setDocumentData(doc);
                controller.setMainLayout(mainLayout);
                controller.setUserData(currentUserId, currentUserName, currentUserRole);
                mainLayout.setCenter(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka halaman riwayat: " + e.getMessage());
            }
        }
    }

    private void showDetailPanel(Document doc) {
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);

        lblDetailJenis.setText(doc.getJenis());
        lblDetailNomor.setText(doc.getNomorDokumen());
        lblDetailMitra.setText(doc.getMitra());

        if (doc.getTanggalMulai() != null)
            lblDetailTanggalMulai.setText(formatTanggalIndonesia(doc.getTanggalMulai()));
        else
            lblDetailTanggalMulai.setText("-");

        if (doc.getTanggalBerakhir() != null) {
            lblDetailTanggalBerakhir.setText(formatTanggalIndonesia(doc.getTanggalBerakhir()));
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(doc.getTanggalBerakhir(), LocalDate.now());
            if (daysOverdue > 0) {
                lblDetailTanggalBerakhir.setText(lblDetailTanggalBerakhir.getText() + " (" + daysOverdue + " hari lewat)");
            }
        } else {
            lblDetailTanggalBerakhir.setText("-");
        }

        lblDetailStatus.setText(doc.getStatus());
        loadRiwayatPerpanjangan(doc);
    }

    @FXML
    private void closeDetailPanel() {
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
        selectedDocument = null;
    }

    private void loadRiwayatPerpanjangan(Document doc) {
        riwayatContainer.getChildren().clear();
        List<RenewalHistory> histories = getRiwayatPerpanjangan(doc.getId());

        if (histories.isEmpty()) {
            Label noHistory = new Label("Belum ada riwayat perpanjangan");
            noHistory.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 12px;");
            noHistory.setAlignment(Pos.CENTER);
            noHistory.setMaxWidth(Double.MAX_VALUE);
            riwayatContainer.getChildren().add(noHistory);
        } else {
            for (RenewalHistory history : histories) {
                riwayatContainer.getChildren().add(createHistoryItem(history));
            }
        }
    }

    private VBox createHistoryItem(RenewalHistory history) {
        VBox item = new VBox(8);
        item.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        Label lblTanggal = new Label(formatTanggalIndonesia(history.getCreatedAt().toLocalDateTime().toLocalDate()));
        lblTanggal.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");

        Label lblBerakhirLama = new Label("Berakhir Lama: " + formatTanggalIndonesia(history.getOldEndDate()));
        lblBerakhirLama.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");

        Label lblBerakhirBaru = new Label("Berakhir Baru: " + formatTanggalIndonesia(history.getNewEndDate()));
        lblBerakhirBaru.setStyle("-fx-text-fill: #14b8a6; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label lblStatus = new Label("Status: " + history.getPreviousStatus() + " → " + history.getNewStatus());
        lblStatus.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        item.getChildren().addAll(lblTanggal, lblBerakhirLama, lblBerakhirBaru, lblStatus);

        if (history.getNotes() != null && !history.getNotes().isEmpty()) {
            Label lblNotes = new Label("📝 " + history.getNotes());
            lblNotes.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-style: italic;");
            lblNotes.setWrapText(true);
            item.getChildren().add(lblNotes);
        }

        return item;
    }

    @FXML
    private void handlePerpanjang() {
        if (selectedDocument == null) {
            showAlert("Error", "Silakan pilih dokumen terlebih dahulu!");
            return;
        }

        LocalDate tanggalBaru = dpTanggalBaru.getValue();
        if (tanggalBaru == null) {
            showAlert("Error", "Silakan pilih tanggal berakhir baru!");
            return;
        }

        if (tanggalBaru.isBefore(LocalDate.now())) {
            showAlert("Error", "Tanggal baru harus lebih besar dari hari ini!");
            return;
        }

        savePerpanjangan(selectedDocument, tanggalBaru, txtCatatan.getText());
    }

    private void savePerpanjangan(Document doc, LocalDate tanggalBaru, String catatan) {
        String sql = "UPDATE documents SET tanggal_berakhir = ?, status = 'Baru', updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(tanggalBaru));
            pstmt.setInt(2, doc.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                insertRiwayatPerpanjangan(doc, tanggalBaru, catatan);
                showAlert("Sukses", "Dokumen berhasil diperpanjang hingga " + formatTanggalIndonesia(tanggalBaru));
                loadExpiredDocuments();
                closeDetailPanel();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memperpanjang dokumen: " + e.getMessage());
        }
    }

    private void insertRiwayatPerpanjangan(Document doc, LocalDate tanggalBaru, String catatan) {
        ensureRiwayatTableExists();

        String sql = "INSERT INTO document_renewal_history (document_id, old_end_date, " +
                "new_end_date, previous_status, new_status, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doc.getId());
            pstmt.setDate(2, Date.valueOf(doc.getTanggalBerakhir()));
            pstmt.setDate(3, Date.valueOf(tanggalBaru));
            pstmt.setString(4, doc.getStatus());
            pstmt.setString(5, "Baru");
            pstmt.setString(6, catatan != null && !catatan.isEmpty() ? catatan : "Perpanjangan dokumen");

            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureRiwayatTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS document_renewal_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "document_id INT NOT NULL, " +
                "old_end_date DATE NOT NULL, " +
                "new_end_date DATE NOT NULL, " +
                "previous_status VARCHAR(50), " +
                "new_status VARCHAR(50), " +
                "notes TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<RenewalHistory> getRiwayatPerpanjangan(int documentId) {
        List<RenewalHistory> histories = new ArrayList<>();
        ensureRiwayatTableExists();

        String sql = "SELECT * FROM document_renewal_history WHERE document_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, documentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                RenewalHistory history = new RenewalHistory();
                history.setId(rs.getInt("id"));
                history.setDocumentId(rs.getInt("document_id"));
                history.setOldEndDate(rs.getDate("old_end_date").toLocalDate());
                history.setNewEndDate(rs.getDate("new_end_date").toLocalDate());
                history.setPreviousStatus(rs.getString("previous_status"));
                history.setNewStatus(rs.getString("new_status"));
                history.setNotes(rs.getString("notes"));
                history.setCreatedAt(rs.getTimestamp("created_at"));
                histories.add(history);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return histories;
    }

    private String formatTanggalIndonesia(LocalDate date) {
        if (date == null) return "-";
        String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        return date.getDayOfMonth() + " " + bulan[date.getMonthValue() - 1] + " " + date.getYear();
    }

    @FXML
    private void handleDashboard() {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();
                DashboardController controller = loader.getController();
                controller.setUserData(currentUserId, currentUserName, currentUserRole);
                controller.setMainLayout(mainLayout);
                mainLayout.setCenter(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka dashboard: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddDocument() {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_document_tabs.fxml"));
                Parent formRoot = loader.load();
                AddDocumentTabsController controller = loader.getController();
                controller.setCurrentUserId(currentUserId);
                mainLayout.setCenter(formRoot);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka form: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewDocuments() {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/all_documents.fxml"));
                Parent root = loader.load();
                AllDocumentsController controller = loader.getController();
                controller.setUserData(currentUserId, currentUserName, currentUserRole);
                mainLayout.setCenter(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka halaman dokumen: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleTindakLanjut() {
        loadExpiredDocuments();
    }

    @FXML
    private void handleUsers() {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_management.fxml"));
                Parent root = loader.load();
                UserManagementController controller = loader.getController();
                controller.setCurrentUserId(currentUserId);
                mainLayout.setCenter(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka halaman user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTableDisplay();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updateTableDisplay();
        }
    }

    @FXML
    private void handlePageClick(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        int clickedPage = Integer.parseInt(clickedButton.getText());
        if (clickedPage >= 1 && clickedPage <= totalPages) {
            currentPage = clickedPage;
            updateTableDisplay();
        }
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) detailPanel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}