package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;

public class DashboardController {

    // === VARIABEL LABEL ===
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblTotalMou;
    @FXML private Label lblTotalPks;
    @FXML private Label lblActive;
    @FXML private Label lblExpired;

    // === VARIABEL FILTER ===
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbJenis;
    @FXML private ComboBox<String> cbStatus;

    // === VARIABEL CHART ===
    @FXML private StackedBarChart<String, Number> chartComparison;
    @FXML private AreaChart<String, Number> chartTrend;
    @FXML private ComboBox<String> cbTahun;

    // === VARIABEL TABEL ===
    @FXML private TableView<Document> tableDocuments;
    @FXML private TableColumn<Document, Integer> colNo;
    @FXML private TableColumn<Document, String> colJenis;
    @FXML private TableColumn<Document, String> colNomor;
    @FXML private TableColumn<Document, String> colMitra;
    @FXML private TableColumn<Document, LocalDate> colTanggalMulai;
    @FXML private TableColumn<Document, LocalDate> colTanggal;
    @FXML private TableColumn<Document, String> colStatus;

    // ✅ VARIABEL PAGINATION
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnPage1;
    @FXML private Button btnPage2;
    @FXML private Button btnPage3;
    @FXML private Button btnPage4;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private int currentUserId;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int ITEMS_PER_PAGE = 10;

    // === METHOD UTAMA: Dipanggil setelah login sukses ===
    public void setUserData(int userId, String userName, String role) {
        this.currentUserId = userId;
        lblUserName.setText(userName);
        lblUserRole.setText(role.toUpperCase());

        initializeComboBox();
        initializeTable();
        initializeCharts();
        loadDashboardData();
    }

    private void initializeComboBox() {
        cbJenis.getItems().addAll("Semua Jenis", "MOU", "PKS");
        cbJenis.setValue("Semua Jenis");

        cbStatus.getItems().addAll("Semua Status", "Aktif", "Perlu Perhatian", "Kadaluarsa");
        cbStatus.setValue("Semua Status");
    }

    // ✅ INITIALIZE CHARTS
    private void initializeCharts() {
        int currentYear = LocalDate.now().getYear();
        cbTahun.getItems().addAll(
                String.valueOf(currentYear - 2),
                String.valueOf(currentYear - 1),
                String.valueOf(currentYear),
                String.valueOf(currentYear + 1)
        );
        cbTahun.setValue(String.valueOf(currentYear));

        cbTahun.setOnAction(e -> updateTrendChart());

        updateComparisonChart();
        updateTrendChart();
    }

    // ✅ UPDATE COMPARISON CHART (MoU vs PKS) - DENGAN STYLING WARNA
    private void updateComparisonChart() {
        String sql = "SELECT jenis, status, COUNT(*) as jumlah FROM documents " +
                "WHERE is_deleted = 0 GROUP BY jenis, status";

        int mouAktif = 0, mouKadaluarsa = 0;
        int pksAktif = 0, pksKadaluarsa = 0;

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String jenis = rs.getString("jenis");
                String status = rs.getString("status");
                int jumlah = rs.getInt("jumlah");

                if (jenis != null && jenis.contains("MoU")) {
                    if ("Aktif".equals(status)) mouAktif = jumlah;
                    else if ("Kadaluarsa".equals(status) || "Perlu Perhatian".equals(status)) mouKadaluarsa = jumlah;
                } else if (jenis != null && jenis.contains("PKS")) {
                    if ("Aktif".equals(status)) pksAktif = jumlah;
                    else if ("Kadaluarsa".equals(status) || "Perlu Perhatian".equals(status)) pksKadaluarsa = jumlah;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create series - AKTIF dulu (bawah), lalu KADALUARSA (atas) untuk stacked
        XYChart.Series<String, Number> seriesAktif = new XYChart.Series<>();
        seriesAktif.setName("Aktif");
        seriesAktif.getData().add(new XYChart.Data<>("MoU", mouAktif));
        seriesAktif.getData().add(new XYChart.Data<>("PKS", pksAktif));

        XYChart.Series<String, Number> seriesKadaluarsa = new XYChart.Series<>();
        seriesKadaluarsa.setName("Kadaluarsa");
        seriesKadaluarsa.getData().add(new XYChart.Data<>("MoU", mouKadaluarsa));
        seriesKadaluarsa.getData().add(new XYChart.Data<>("PKS", pksKadaluarsa));

        // Clear and add data
        chartComparison.getData().clear();
        chartComparison.getData().addAll(seriesAktif, seriesKadaluarsa);

        // ✅ STYLE WARNA: Teal untuk Aktif, Orange untuk Kadaluarsa
        chartComparison.lookupAll(".default-color0.chart-bar").forEach(node ->
                node.setStyle("-fx-bar-fill: #14b8a6;"));
        chartComparison.lookupAll(".default-color1.chart-bar").forEach(node ->
                node.setStyle("-fx-bar-fill: #f97316;"));

        // ✅ Adjust Y-axis range agar proporsional
        NumberAxis yAxis = (NumberAxis) chartComparison.getYAxis();
        int maxVal = Math.max(mouAktif + mouKadaluarsa, pksAktif + pksKadaluarsa);
        yAxis.setUpperBound(maxVal + 2);
        yAxis.setTickUnit(Math.max(1, Math.ceil((maxVal + 2) / 5.0)));
    }

    // ✅ UPDATE TREND CHART (Bulanan) - DENGAN STYLING WARNA & DOTS
    private void updateTrendChart() {
        String tahun = cbTahun.getValue();
        if (tahun == null) tahun = String.valueOf(LocalDate.now().getYear());

        String sql = "SELECT jenis, MONTH(tanggal_mulai) as bulan, COUNT(*) as jumlah " +
                "FROM documents WHERE is_deleted = 0 AND YEAR(tanggal_mulai) = ? " +
                "GROUP BY jenis, MONTH(tanggal_mulai) ORDER BY bulan";

        int[] mouPerBulan = new int[12];
        int[] pksPerBulan = new int[12];

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tahun);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String jenis = rs.getString("jenis");
                int bulan = rs.getInt("bulan");
                int jumlah = rs.getInt("jumlah");

                if (jenis != null && jenis.contains("MoU")) {
                    mouPerBulan[bulan - 1] = jumlah;
                } else if (jenis != null && jenis.contains("PKS")) {
                    pksPerBulan[bulan - 1] = jumlah;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create series
        XYChart.Series<String, Number> seriesMoU = new XYChart.Series<>();
        seriesMoU.setName("MoU Baru");
        String[] bulanNames = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

        for (int i = 0; i < 12; i++) {
            seriesMoU.getData().add(new XYChart.Data<>(bulanNames[i], mouPerBulan[i]));
        }

        XYChart.Series<String, Number> seriesPKS = new XYChart.Series<>();
        seriesPKS.setName("PKS Baru");
        for (int i = 0; i < 12; i++) {
            seriesPKS.getData().add(new XYChart.Data<>(bulanNames[i], pksPerBulan[i]));
        }

        // Clear and add data
        chartTrend.getData().clear();
        chartTrend.getData().addAll(seriesMoU, seriesPKS);

        // ✅ STYLE WARNA AREA CHART: Teal untuk MoU, Orange untuk PKS
        // MoU - Line, Fill, dan Dots
        chartTrend.lookupAll(".default-color0.chart-series-area-line").forEach(node ->
                node.setStyle("-fx-stroke: #14b8a6; -fx-stroke-width: 3px;"));
        chartTrend.lookupAll(".default-color0.chart-series-area-fill").forEach(node ->
                node.setStyle("-fx-fill: #14b8a640;")); // Transparan 25%
        chartTrend.lookupAll(".default-color0.chart-line-symbol").forEach(node ->
                node.setStyle("-fx-background-color: #14b8a6, white; -fx-background-radius: 5; -fx-padding: 4;"));

        // PKS - Line, Fill, dan Dots
        chartTrend.lookupAll(".default-color1.chart-series-area-line").forEach(node ->
                node.setStyle("-fx-stroke: #f97316; -fx-stroke-width: 3px;"));
        chartTrend.lookupAll(".default-color1.chart-series-area-fill").forEach(node ->
                node.setStyle("-fx-fill: #f9731640;")); // Transparan 25%
        chartTrend.lookupAll(".default-color1.chart-line-symbol").forEach(node ->
                node.setStyle("-fx-background-color: #f97316, white; -fx-background-radius: 5; -fx-padding: 4;"));

        // ✅ Adjust Y-axis range
        NumberAxis yAxis = (NumberAxis) chartTrend.getYAxis();
        int maxMoU = java.util.Arrays.stream(mouPerBulan).max().orElse(0);
        int maxPKS = java.util.Arrays.stream(pksPerBulan).max().orElse(0);
        int maxVal = Math.max(maxMoU, maxPKS);
        yAxis.setUpperBound(Math.max(6, maxVal + 2));
        yAxis.setTickUnit(Math.max(1, Math.ceil((maxVal + 2) / 5.0)));
    }

    private void initializeTable() {
        colNo.setResizable(false);
        colJenis.setResizable(false);
        colNomor.setResizable(false);
        colMitra.setResizable(false);
        colTanggalMulai.setResizable(false);
        colTanggal.setResizable(false);
        colStatus.setResizable(false);

        // Kolom No dengan nomor urut otomatis
        colNo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #64748b;");
                }
            }
        });

        // Kolom Nomor Dokumen
        colNomor.setCellValueFactory(new PropertyValueFactory<>("nomorDokumen"));
        colNomor.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569;");
                }
            }
        });

        // ✅ Kolom Jenis dengan Badge Berwarna - Posisi Tengah
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colJenis.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String displayText = item;
                    if (item.contains("(")) {
                        displayText = item.substring(0, item.indexOf("(")).trim();
                    }
                    if (displayText.contains(" ")) {
                        displayText = displayText.split(" ")[0];
                    }

                    Label badge = new Label(displayText);
                    badge.setStyle(getJenisStyle(displayText));
                    badge.setPadding(new Insets(6, 12, 6, 12));
                    badge.setStyle(badge.getStyle() +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 12px; " +
                            "-fx-alignment: CENTER;");
                    badge.setAlignment(Pos.CENTER);

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Kolom Mitra
        colMitra.setCellValueFactory(new PropertyValueFactory<>("mitra"));
        colMitra.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569;");
                }
            }
        });

        // Kolom Tanggal Mulai dengan Format Indonesia
        colTanggalMulai.setCellValueFactory(new PropertyValueFactory<>("tanggalMulai"));
        colTanggalMulai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTanggalIndonesia(item));
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569;");
                }
            }
        });

        // Kolom Tanggal Berakhir dengan Format Indonesia
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalBerakhir"));
        colTanggal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTanggalIndonesia(item));
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #475569;");
                }
            }
        });

        // ✅ Kolom Status dengan Badge Berwarna - Posisi Tengah
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(getStatusStyle(item));
                    badge.setPadding(new Insets(6, 15, 6, 15));
                    badge.setStyle(badge.getStyle() +
                            "-fx-background-radius: 20; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 12px; " +
                            "-fx-alignment: CENTER;");
                    badge.setAlignment(Pos.CENTER);

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    // ✅ Helper: Style untuk Badge Jenis
    private String getJenisStyle(String jenis) {
        if (jenis == null) return "-fx-background-color: #cbd5e1;";
        if (jenis.contains("PKS")) return "-fx-background-color: #fdba74;";
        if (jenis.contains("MoU")) return "-fx-background-color: #99f6e4;";
        return "-fx-background-color: #cbd5e1;";
    }

    // ✅ Helper: Style untuk Badge Status (LENGKAP)
    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #94a3b8;";

        String s = status.toLowerCase();
        if (s.equals("baru")) return "-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca;";
        if (s.equals("dalam proses")) return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;";
        if (s.equals("review bpsdmp kominfo")) return "-fx-background-color: #ddd6fe; -fx-text-fill: #7c3aed;";
        if (s.equals("review pemda 1") || s.equals("review pemda 2"))
            return "-fx-background-color: #fce7f3; -fx-text-fill: #db2777;";
        if (s.equals("review bpsdmp 1") || s.equals("review bpsdmp 2"))
            return "-fx-background-color: #ccfbf1; -fx-text-fill: #0d9488;";
        if (s.equals("persiapan ttd para pihak"))
            return "-fx-background-color: #fef9c3; -fx-text-fill: #a16207;";
        if (s.equals("selesai")) return "-fx-background-color: #bbf7d0; -fx-text-fill: #15803d;";
        if (s.equals("aktif")) return "-fx-background-color: #86efac; -fx-text-fill: #166534;";
        if (s.equals("perlu perhatian")) return "-fx-background-color: #fdba74; -fx-text-fill: #c2410c;";
        if (s.equals("kadaluarsa")) return "-fx-background-color: #fca5a5; -fx-text-fill: #b91c1c;";

        return "-fx-background-color: #94a3b8;";
    }

    // ✅ Helper: Format Tanggal Indonesia (13 Maret 2026)
    private String formatTanggalIndonesia(LocalDate date) {
        if (date == null) return "-";
        String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        return date.getDayOfMonth() + " " + bulan[date.getMonthValue() - 1] + " " + date.getYear();
    }

    // ✅ METHOD INI DI-UPDATE: Support Pagination dengan LIMIT/OFFSET
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

        totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int offset = (currentPage - 1) * ITEMS_PER_PAGE;

        String sql = "SELECT * FROM documents WHERE is_deleted = 0 ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ITEMS_PER_PAGE);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            documentList.clear();
            int totalMou = 0, totalPks = 0, active = 0, expired = 0;
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
            }

            lblTotalMou.setText(String.valueOf(totalMou));
            lblTotalPks.setText(String.valueOf(totalPks));
            lblActive.setText(String.valueOf(active));
            lblExpired.setText(String.valueOf(expired));

            tableDocuments.setItems(documentList);
            updatePaginationControls();

            // ✅ Refresh charts
            updateComparisonChart();
            updateTrendChart();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ METHOD PAGINATION
    private void updatePaginationControls() {
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);

        String disabledStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #cbd5e1; -fx-cursor: default; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;";
        String enabledStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;";

        btnPrev.setStyle(btnPrev.isDisabled() ? disabledStyle : enabledStyle);
        btnNext.setStyle(btnNext.isDisabled() ? disabledStyle : enabledStyle);

        updatePageButtons();
    }

    private void updatePageButtons() {
        Button[] pageButtons = {btnPage1, btnPage2, btnPage3, btnPage4};
        String activeStyle = "-fx-background-color: #14b8a6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 20; -fx-font-weight: bold;";
        String inactiveStyle = "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 20; -fx-font-weight: bold;";

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

    // === ✅ METHOD: Handle Tambah Dokumen - UPDATED (Modal Rapi + Scroll) ===
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
                Parent root = loader.load();

                // ✅ Bungkus form dalam ScrollPane agar bisa di-scroll jika konten panjang
                ScrollPane scrollPane = new ScrollPane(root);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");

                // ✅ Buat Scene dengan ukuran yang pas (tidak terlalu besar)
                Scene scene = new Scene(scrollPane, 700, 600);

                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Tambah Dokumen - " + jenis);
                stage.setResizable(true); // Bisa di-resize jika perlu
                stage.sizeToScene(); // Auto size ke konten

                // ✅ Set modality agar user fokus ke form (dashboard di background)
                stage.initModality(Modality.APPLICATION_MODAL);

                Object controller = loader.getController();
                if (controller instanceof AddPemdaDocumentController) {
                    ((AddPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                } else if (controller instanceof AddNonPemdaDocumentController) {
                    ((AddNonPemdaDocumentController) controller).setCurrentUserId(currentUserId);
                }

                stage.showAndWait();
                loadDashboardData();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka form: " + e.getMessage());
            }
        }
    }

    // === METHOD: Handle Edit Dokumen ===
    @FXML
    private void handleEdit(Document doc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_document.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 800, 650));
            stage.setTitle("Edit Dokumen - " + doc.getNomorDokumen());

            EditDocumentController controller = loader.getController();
            controller.setDocumentData(doc);
            controller.setCurrentUserId(currentUserId);

            stage.showAndWait();
            loadDashboardData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form edit: " + e.getMessage());
        }
    }

    // === METHOD: Handle Hapus Dokumen (Soft Delete) ===
    @FXML
    private void handleDelete(Document doc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Dokumen");
        confirm.setContentText("Yakin ingin menghapus dokumen:\n\"" + doc.getNomorDokumen() + "\"?\n\nData akan ditandai sebagai terhapus (soft delete).");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.connect()) {
                    String sql = "UPDATE documents SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, doc.getId());
                    pstmt.executeUpdate();

                    showAlert("Sukses", "Dokumen berhasil dihapus.");
                    loadDashboardData();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Gagal menghapus: " + e.getMessage());
                }
            }
        });
    }

    // === EVENT HANDLER LAINNYA ===
    @FXML private void handleDashboard() { loadDashboardData(); }

    // ✅✅✅ METHOD INI DI-UPDATE: Window Modal yang MENUTUPI Dashboard Sepenuhnya ✅✅✅
    @FXML
    private void handleViewDocuments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/all_documents.fxml"));
            Parent root = loader.load();

            // ✅ Buat Scene dengan ukuran yang pas
            Scene scene = new Scene(root, 1100, 700);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Manajemen Dokumen - SIKERMA");

            // ✅ PENTING: Urutan yang benar untuk modal window
            Stage ownerStage = (Stage) ((javafx.scene.Node) lblUserName).getScene().getWindow();
            stage.initOwner(ownerStage);                              // 1. Set owner dulu
            stage.initModality(Modality.APPLICATION_MODAL);           // 2. Set modality
            stage.centerOnScreen();                                   // 3. Center di layar

            AllDocumentsController controller = loader.getController();
            controller.setUserData(currentUserId, lblUserName.getText());

            stage.showAndWait();  // ✅ Block sampai window ditutup
            loadDashboardData();  // ✅ Refresh dashboard setelah tutup

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka halaman dokumen: " + e.getMessage());
        }
    }

    @FXML private void handleRenewal() {
        showAlert("Info", "Fitur perpanjangan akan dibuat...");
    }

    @FXML private void handleNotifications() {
        showAlert("Info", "Fitur notifikasi akan dibuat...");
    }

    @FXML private void handleUsers() {
        showAlert("Info", "Manajemen User (Admin Only)");
    }

    @FXML private void handleSearch() {
        String keyword = txtSearch.getText().toLowerCase();
        ObservableList<Document> filtered = FXCollections.observableArrayList();

        for (Document doc : documentList) {
            if (doc.getNomorDokumen().toLowerCase().contains(keyword) ||
                    doc.getMitra().toLowerCase().contains(keyword)) {
                filtered.add(doc);
            }
        }
        tableDocuments.setItems(filtered);
    }

    @FXML private void handleLogout() {
        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}