package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import com.sikerma.sikerma.model.RenewalHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    @FXML private ComboBox<String> cbTahun;

    @FXML private TableView<Document> tableDocuments;
    @FXML private TableColumn<Document, Integer> colNo;
    @FXML private TableColumn<Document, String> colJenis;
    @FXML private TableColumn<Document, String> colNomor;
    @FXML private TableColumn<Document, String> colMitra;
    @FXML private TableColumn<Document, LocalDate> colTanggalMulai;
    @FXML private TableColumn<Document, LocalDate> colTanggal;
    @FXML private TableColumn<Document, String> colStatus;
    @FXML private TableColumn<Document, Void> colAksi;

    @FXML private Button btnDashboard;
    @FXML private Button btnTambah;
    @FXML private Button btnSemuaDokumen;
    @FXML private Button btnPerpanjangan;
    @FXML private Button btnNotifikasi;
    @FXML private Button btnUser;
    @FXML private Button btnPage1;
    @FXML private Button btnPage2;
    @FXML private Button btnPage3;
    @FXML private Button btnPage4;
    @FXML private Button btnLogout;

    @FXML private BarChart<String, Number> barChartMoUPks;
    @FXML private LineChart<String, Number> lineChartTren;

    @FXML private VBox cardMou;
    @FXML private VBox cardPks;
    @FXML private VBox cardAktif;
    @FXML private VBox cardKadaluarsa;

    // ✅ PERBAIKAN: Tambahkan kedua variabel
    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private ObservableList<Document> allDocumentsForChart = FXCollections.observableArrayList();
    private int currentUserId;
    private int currentPage = 1;
    private int totalPages = 1;
    private int itemsPerPage = 5;
    private BorderPane mainLayout;
    private String activeCardFilter = null;
    private Document selectedDocumentForRenewal;

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
            if (activeCardFilter != null) {
                showAlert("Info", "Silakan reset filter card terlebih dahulu untuk mengubah jumlah halaman.");
                if (itemsPerPage == 5) cbPerPage.setValue("5 / halaman");
                else if (itemsPerPage == 10) cbPerPage.setValue("10 / halaman");
                else if (itemsPerPage == 25) cbPerPage.setValue("25 / halaman");
                return;
            }

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
        colAksi.setResizable(false);

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

        colAksi.setCellFactory(column -> new TableCell<>() {
            private final Button btnPerpanjang = new Button("Perpanjang");
            private final Button btnRiwayat = new Button("Riwayat");
            private final VBox buttonsPane = new VBox(5, btnPerpanjang, btnRiwayat);
            private final Label lblDash = new Label("-");

            {
                btnPerpanjang.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-padding: 5 10; -fx-font-weight: bold; " +
                        "-fx-font-size: 11px; -fx-cursor: hand; -fx-min-width: 130;");
                btnRiwayat.setStyle("-fx-background-color: #06b6d4; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-padding: 5 10; -fx-font-weight: bold; " +
                        "-fx-font-size: 11px; -fx-cursor: hand; -fx-min-width: 130;");

                btnPerpanjang.setMaxWidth(Double.MAX_VALUE);
                btnRiwayat.setMaxWidth(Double.MAX_VALUE);

                lblDash.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-weight: bold;");
                lblDash.setAlignment(Pos.CENTER);
                lblDash.setMaxWidth(Double.MAX_VALUE);

                btnPerpanjang.setOnAction(e -> {
                    int rowIndex = getIndex();
                    if (rowIndex >= 0 && rowIndex < tableDocuments.getItems().size()) {
                        Document doc = tableDocuments.getItems().get(rowIndex);
                        showPerpanjangDialog(doc);
                    }
                });

                btnRiwayat.setOnAction(e -> {
                    int rowIndex = getIndex();
                    if (rowIndex >= 0 && rowIndex < tableDocuments.getItems().size()) {
                        Document doc = tableDocuments.getItems().get(rowIndex);
                        showRiwayatDialog(doc);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Document doc = getTableRow().getItem();
                    LocalDate today = LocalDate.now();

                    boolean isExpired = doc.getTanggalBerakhir() != null &&
                            doc.getTanggalBerakhir().isBefore(today);

                    if (isExpired) {
                        buttonsPane.setAlignment(Pos.CENTER);
                        buttonsPane.setPadding(new Insets(2, 0, 2, 0));
                        setGraphic(buttonsPane);
                    } else {
                        setGraphic(lblDash);
                    }
                }
            }
        });

        tableDocuments.setFixedCellSize(90);
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

            // ✅ Load semua dokumen untuk chart
            loadAllDocumentsForChart();
            initializeCharts();
            updateCharts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeCharts() {
        cbTahun.getItems().clear();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 2; year <= currentYear + 2; year++) {
            cbTahun.getItems().add(String.valueOf(year));
        }
        cbTahun.setValue(String.valueOf(currentYear));

        cbTahun.setOnAction(e -> {
            updateCharts();
        });

        setupBarChart();
        setupLineChart();
    }

    private void setupBarChart() {
        barChartMoUPks.setAnimated(false);
        barChartMoUPks.setStyle("-fx-background-color: transparent;");
        barChartMoUPks.setLegendSide(javafx.geometry.Side.BOTTOM);
        barChartMoUPks.lookup(".chart-legend").setStyle("-fx-background-color: transparent; -fx-padding: 10 0 0 0;");
    }

    private void setupLineChart() {
        lineChartTren.setAnimated(false);
        lineChartTren.setStyle("-fx-background-color: transparent;");

        CategoryAxis xAxis = (CategoryAxis) lineChartTren.getXAxis();
        xAxis.getCategories().clear();
        xAxis.getCategories().addAll("Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des");

        lineChartTren.setLegendSide(javafx.geometry.Side.BOTTOM);
        lineChartTren.lookup(".chart-legend").setStyle("-fx-background-color: transparent; -fx-padding: 10 0 0 0;");
    }

    private void loadAllDocumentsForChart() {
        allDocumentsForChart.clear();
        String sql = "SELECT * FROM documents WHERE is_deleted = 0";

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
                doc.setStatus(rs.getString("status"));
                allDocumentsForChart.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCharts() {
        updateBarChart();
        updateLineChart();
    }

    private void updateBarChart() {
        int mouAktif = 0, mouExpired = 0, pksAktif = 0, pksExpired = 0;
        LocalDate today = LocalDate.now();

        // ✅ Gunakan allDocumentsForChart
        for (Document doc : allDocumentsForChart) {
            boolean isExpired = doc.getTanggalBerakhir() != null && doc.getTanggalBerakhir().isBefore(today);

            if ("MOU".equals(doc.getJenis())) {
                if (isExpired) mouExpired++;
                else mouAktif++;
            } else if ("PKS".equals(doc.getJenis())) {
                if (isExpired) pksExpired++;
                else pksAktif++;
            }
        }

        XYChart.Series<String, Number> seriesAktif = new XYChart.Series<>();
        seriesAktif.setName("Aktif");
        seriesAktif.getData().add(new XYChart.Data<>("MoU", mouAktif));
        seriesAktif.getData().add(new XYChart.Data<>("PKS", pksAktif));

        XYChart.Series<String, Number> seriesExpired = new XYChart.Series<>();
        seriesExpired.setName("Kadaluarsa");
        seriesExpired.getData().add(new XYChart.Data<>("MoU", mouExpired));
        seriesExpired.getData().add(new XYChart.Data<>("PKS", pksExpired));

        barChartMoUPks.getData().clear();
        barChartMoUPks.getData().addAll(seriesAktif, seriesExpired);

        applyBarChartColors();
    }

    private void applyBarChartColors() {
        barChartMoUPks.getData().get(0).getNode().setStyle(
                "-fx-bar-fill: #14b8a6; -fx-background-color: #14b8a6;"
        );

        barChartMoUPks.getData().get(1).getNode().setStyle(
                "-fx-bar-fill: #fb923c; -fx-background-color: #fb923c;"
        );
    }

    private void updateLineChart() {
        String selectedYear = cbTahun.getValue();
        if (selectedYear == null) selectedYear = String.valueOf(LocalDate.now().getYear());

        int year = Integer.parseInt(selectedYear);

        int[] mouPerMonth = new int[12];
        int[] pksPerMonth = new int[12];

        // ✅ PERBAIKAN: Gunakan allDocumentsForChart, bukan documentList
        for (Document doc : allDocumentsForChart) {
            if (doc.getTanggalMulai() != null && doc.getTanggalMulai().getYear() == year) {
                int month = doc.getTanggalMulai().getMonthValue() - 1;
                if ("MOU".equals(doc.getJenis())) {
                    mouPerMonth[month]++;
                } else if ("PKS".equals(doc.getJenis())) {
                    pksPerMonth[month]++;
                }
            }
        }

        XYChart.Series<String, Number> seriesMoU = new XYChart.Series<>();
        seriesMoU.setName("MoU Baru");
        for (int i = 0; i < 12; i++) {
            seriesMoU.getData().add(new XYChart.Data<>(getMonthName(i), mouPerMonth[i]));
        }

        XYChart.Series<String, Number> seriesPKS = new XYChart.Series<>();
        seriesPKS.setName("PKS Baru");
        for (int i = 0; i < 12; i++) {
            seriesPKS.getData().add(new XYChart.Data<>(getMonthName(i), pksPerMonth[i]));
        }

        lineChartTren.getData().clear();
        lineChartTren.getData().addAll(seriesMoU, seriesPKS);

        applyLineChartColors();
    }

    private void applyLineChartColors() {
        lineChartTren.getData().get(0).getNode().setStyle(
                "-fx-stroke: #14b8a6; -fx-stroke-width: 3px;"
        );

        lineChartTren.getData().get(1).getNode().setStyle(
                "-fx-stroke: #fb923c; -fx-stroke-width: 3px;"
        );

        for (XYChart.Series<String, Number> series : lineChartTren.getData()) {
            series.getNode().lookup(".chart-series-line").setStyle(
                    "-fx-stroke: " + (series.getName().equals("MoU Baru") ? "#14b8a6" : "#fb923c") +
                            "; -fx-stroke-width: 3px;"
            );
        }
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
        return months[month];
    }

    // ✅ PERBAIKAN: Gunakan allDocumentsForChart untuk menghitung total
    private int getMouActiveCount() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Document doc : allDocumentsForChart) {
            if ("MOU".equals(doc.getJenis()) && doc.getTanggalBerakhir() != null && !doc.getTanggalBerakhir().isBefore(today)) {
                count++;
            }
        }
        return count;
    }

    // ✅ PERBAIKAN: Gunakan allDocumentsForChart untuk menghitung total
    private int getPksActiveCount() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Document doc : allDocumentsForChart) {
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

    @FXML
    public void handleDashboard() {
        currentPage = 1;

        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();

                DashboardController controller = loader.getController();
                controller.setUserData(currentUserId, lblUserName.getText(), lblUserRole.getText());

                if (root instanceof BorderPane) {
                    controller.setMainLayout((BorderPane) root);
                } else {
                    controller.setMainLayout(mainLayout);
                }

                Scene scene = mainLayout.getScene();
                if (scene != null) {
                    scene.setRoot(root);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka dashboard: " + e.getMessage());
            }
        }
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

    @FXML
    private void handleUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_management.fxml"));
            Parent root = loader.load();

            UserManagementController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            controller.setDashboardController(this);

            if (mainLayout != null) {
                mainLayout.setCenter(root);
            }

            updateMenuStyle(btnUser);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka halaman user: " + e.getMessage());
        }
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
        if (activeCardFilter != null) return;
        if (currentPage > 1) {
            currentPage--;
            loadDashboardData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (activeCardFilter != null) return;
        if (currentPage < totalPages) {
            currentPage++;
            loadDashboardData();
        }
    }

    @FXML
    private void handlePageClick(javafx.event.ActionEvent event) {
        if (activeCardFilter != null) return;
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

        Button[] buttons = {btnDashboard, btnTambah, btnSemuaDokumen, btnPerpanjangan, btnNotifikasi, btnUser};
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

    public BorderPane getMainLayout() {
        return mainLayout;
    }

    @FXML
    private void handleCardMouClick() {
        toggleCardFilter("MOU", cardMou);
    }

    @FXML
    private void handleCardPksClick() {
        toggleCardFilter("PKS", cardPks);
    }

    @FXML
    private void handleCardAktifClick() {
        toggleCardFilter("AKTIF", cardAktif);
    }

    @FXML
    private void handleCardKadaluarsaClick() {
        toggleCardFilter("KADALUARSA", cardKadaluarsa);
    }

    private void toggleCardFilter(String filterType, VBox clickedCard) {
        if (filterType.equals(activeCardFilter)) {
            activeCardFilter = null;
            resetCardStyles();
            loadDashboardData();
        } else {
            activeCardFilter = filterType;
            resetCardStyles();

            clickedCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.2), 10, 0, 0, 2); -fx-padding: 24; -fx-min-width: 200; -fx-cursor: hand;");

            applyCardFilter(filterType);
        }
    }

    private void resetCardStyles() {
        String defaultStyle = "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2); -fx-padding: 24; -fx-min-width: 200; -fx-cursor: hand;";
        if (cardMou != null) cardMou.setStyle(defaultStyle);
        if (cardPks != null) cardPks.setStyle(defaultStyle);
        if (cardAktif != null) cardAktif.setStyle(defaultStyle);
        if (cardKadaluarsa != null) cardKadaluarsa.setStyle(defaultStyle);
    }

    private void applyCardFilter(String filterType) {
        String sql = "SELECT * FROM documents WHERE is_deleted = 0";
        if ("MOU".equals(filterType)) sql += " AND jenis = 'MOU'";
        else if ("PKS".equals(filterType)) sql += " AND jenis = 'PKS'";
        else if ("AKTIF".equals(filterType)) sql += " AND (tanggal_berakhir >= CURDATE() OR tanggal_berakhir IS NULL)";
        else if ("KADALUARSA".equals(filterType)) sql += " AND tanggal_berakhir < CURDATE()";

        sql += " ORDER BY created_at DESC";

        ObservableList<Document> filteredList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getInt("id"));
                doc.setNomorDokumen(rs.getString("nomor_dokumen"));
                doc.setJenis(rs.getString("jenis"));
                doc.setMitra(rs.getString("mitra"));
                if (rs.getDate("tanggal_mulai") != null) doc.setTanggalMulai(rs.getDate("tanggal_mulai").toLocalDate());
                if (rs.getDate("tanggal_berakhir") != null) doc.setTanggalBerakhir(rs.getDate("tanggal_berakhir").toLocalDate());
                doc.setStatus(rs.getString("status"));
                filteredList.add(doc);
            }

            documentList.clear();
            documentList.addAll(filteredList);

            tableDocuments.setItems(filteredList);
            lblTotalDocs.setText("Total : " + filteredList.size());

            if (filteredList.size() > 0) {
                lblPageInfo.setText("Menampilkan 1 - " + filteredList.size() + " dari " + filteredList.size() + " dokumen (Filter: " + getFilterDisplayName(filterType) + ")");
            } else {
                lblPageInfo.setText("Tidak ada dokumen ditemukan untuk filter: " + getFilterDisplayName(filterType));
            }

            btnPage1.setVisible(false); btnPage1.setManaged(false);
            btnPage2.setVisible(false); btnPage2.setManaged(false);
            btnPage3.setVisible(false); btnPage3.setManaged(false);
            btnPage4.setVisible(false); btnPage4.setManaged(false);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memfilter dokumen: " + e.getMessage());
        }
    }

    private String getFilterDisplayName(String filterType) {
        if ("MOU".equals(filterType)) return "Total MoU";
        if ("PKS".equals(filterType)) return "Total PKS";
        if ("AKTIF".equals(filterType)) return "Dokumen Aktif";
        if ("KADALUARSA".equals(filterType)) return "Dokumen Kadaluarsa";
        return filterType;
    }

    private void showPerpanjangDialog(Document doc) {
        Stage stage = new Stage();
        stage.setTitle("Perpanjang Dokumen");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) tableDocuments).getScene().getWindow());

        VBox dialogPane = new VBox(15);
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-min-width: 500;");

        Label titleLabel = new Label("🔄 Perpanjang Dokumen");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label docInfo = new Label(String.format("%s dengan %s", doc.getJenis(), doc.getMitra()));
        docInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        docInfo.setWrapText(true);

        Label lblTanggalLama = new Label("Tanggal Berakhir Lama: " + formatTanggalIndonesia(doc.getTanggalBerakhir()));
        lblTanggalLama.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label lblTanggalBaru = new Label("Tanggal Berakhir Baru *");
        lblTanggalBaru.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        DatePicker dpTanggalBaru = new DatePicker();
        dpTanggalBaru.setPromptText("Pilih tanggal...");
        dpTanggalBaru.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-padding: 10; -fx-min-width: 450;");

        Label alertInfo = new Label("⚠️ Status dokumen akan diperbarui menjadi 'Baru' setelah perpanjangan berhasil.");
        alertInfo.setStyle("-fx-background-color: #fff7ed; -fx-border-color: #fed7aa; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; " +
                "-fx-text-fill: #9a3412; -fx-font-size: 12px;");
        alertInfo.setWrapText(true);

        Label lblCatatan = new Label("Catatan Perpanjangan (Opsional)");
        lblCatatan.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        TextArea txtCatatan = new TextArea();
        txtCatatan.setPromptText("Contoh: Perpanjangan sesuai hasil evaluasi tahun 2026");
        txtCatatan.setPrefRowCount(3);
        txtCatatan.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-padding: 10;");
        txtCatatan.setWrapText(true);

        Button btnCancel = new Button("Batal");
        btnCancel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                "-fx-background-radius: 8; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnPerpanjang = new Button("Perpanjang Sekarang");
        btnPerpanjang.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");
        btnPerpanjang.setDisable(true);

        HBox buttonBox = new HBox(15, btnCancel, btnPerpanjang);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        dpTanggalBaru.valueProperty().addListener((obs, oldVal, newVal) -> {
            btnPerpanjang.setDisable(newVal == null);
        });

        btnCancel.setOnAction(e -> stage.close());

        btnPerpanjang.setOnAction(e -> {
            LocalDate tanggalBaru = dpTanggalBaru.getValue();
            if (tanggalBaru == null) {
                showAlert("Error", "Silakan pilih tanggal berakhir baru!");
                return;
            }
            if (tanggalBaru.isBefore(LocalDate.now())) {
                showAlert("Error", "Tanggal baru harus lebih besar dari hari ini!");
                return;
            }
            savePerpanjangan(doc, tanggalBaru, txtCatatan.getText());
            stage.close();
        });

        dialogPane.getChildren().addAll(
                titleLabel, new Separator(), docInfo, lblTanggalLama,
                lblTanggalBaru, dpTanggalBaru, alertInfo,
                lblCatatan, txtCatatan, buttonBox
        );

        Scene scene = new Scene(dialogPane);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.showAndWait();
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

                if (activeCardFilter != null) {
                    applyCardFilter(activeCardFilter);
                } else {
                    loadDashboardData();
                }
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
            pstmt.setString(4, doc.getStatus() != null ? doc.getStatus() : "Kadaluarsa");
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

    private void showRiwayatDialog(Document doc) {
        Stage stage = new Stage();
        stage.setTitle("Riwayat Perpanjangan");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) tableDocuments).getScene().getWindow());

        VBox dialogPane = new VBox(20);
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-min-width: 550; -fx-max-height: 500;");

        Label titleLabel = new Label("🕐 Riwayat Perpanjangan");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label docInfo = new Label(String.format("%s - %s", doc.getJenis(), doc.getMitra()));
        docInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        docInfo.setAlignment(Pos.CENTER);
        docInfo.setWrapText(true);

        VBox historyContainer = new VBox(15);
        historyContainer.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-padding: 15;");

        List<RenewalHistory> histories = getRiwayatPerpanjangan(doc.getId());

        if (histories.isEmpty()) {
            Label noHistory = new Label("Belum ada riwayat perpanjangan");
            noHistory.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            noHistory.setAlignment(Pos.CENTER);
            noHistory.setMaxWidth(Double.MAX_VALUE);
            historyContainer.getChildren().add(noHistory);
        } else {
            HBox jumlahBox = new HBox(10);
            jumlahBox.setAlignment(Pos.CENTER);
            Label lblJumlah = new Label("Jumlah Perpanjangan : ");
            lblJumlah.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            Label lblJumlahNilai = new Label(histories.size() + "x");
            lblJumlahNilai.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            jumlahBox.getChildren().addAll(lblJumlah, lblJumlahNilai);

            historyContainer.getChildren().addAll(jumlahBox, new Separator());

            for (RenewalHistory history : histories) {
                historyContainer.getChildren().add(createHistoryItem(history));
            }
        }

        ScrollPane scrollPane = new ScrollPane(historyContainer);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(300);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button btnClose = new Button("Tutup");
        btnClose.setStyle("-fx-background-color: #06b6d4; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 12 24; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-min-width: 200;");
        btnClose.setOnAction(e -> stage.close());

        dialogPane.getChildren().addAll(titleLabel, docInfo, new Separator(), scrollPane, btnClose);

        Scene scene = new Scene(dialogPane);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.showAndWait();
    }

    private VBox createHistoryItem(RenewalHistory history) {
        VBox item = new VBox(8);
        item.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-padding: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        Label lblTanggal = new Label("📅 " + formatTanggalIndonesia(history.getCreatedAt().toLocalDateTime().toLocalDate()));
        lblTanggal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");

        Label lblOldDate = new Label("Tanggal Lama: " + formatTanggalIndonesia(history.getOldEndDate()));
        lblOldDate.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");

        Label lblNewDate = new Label("Tanggal Baru: " + formatTanggalIndonesia(history.getNewEndDate()));
        lblNewDate.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label lblStatus = new Label("Status: " + history.getPreviousStatus() + " → " + history.getNewStatus());
        lblStatus.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        item.getChildren().addAll(lblTanggal, lblOldDate, lblNewDate, lblStatus);

        if (history.getNotes() != null && !history.getNotes().isEmpty()) {
            Label lblNotes = new Label("📝 " + history.getNotes());
            lblNotes.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-style: italic;");
            lblNotes.setWrapText(true);
            item.getChildren().add(lblNotes);
        }

        return item;
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
}