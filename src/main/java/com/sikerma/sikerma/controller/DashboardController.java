package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import com.sikerma.sikerma.model.RenewalHistory;
import javafx.application.Platform;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @FXML private Button btnDashboard;
    @FXML private Button btnTambah;
    @FXML private Button btnSemuaDokumen;
    @FXML private Button btnUser;
    @FXML private Button btnTindakLanjut;
    @FXML private Button btnPage1;
    @FXML private Button btnPage2;
    @FXML private Button btnPage3;
    @FXML private Button btnPage4;
    @FXML private Button btnLogout;

    @FXML private StackedBarChart<String, Number> barChartMoUPks;
    @FXML private AreaChart<String, Number> areaChartTren;

    @FXML private VBox cardMou;
    @FXML private VBox cardPks;
    @FXML private VBox cardAktif;
    @FXML private VBox cardKadaluarsa;

    @FXML private HBox alertBanner;
    @FXML private VBox alertDetailContainer;
    @FXML private Label lblAlertToggle;

    private ObservableList<Document> documentList = FXCollections.observableArrayList();
    private ObservableList<Document> allDocumentsForChart = FXCollections.observableArrayList();
    private int currentUserId;
    private String currentUserRole;
    private int currentPage = 1;
    private int totalPages = 1;
    private int itemsPerPage = 5;
    private BorderPane mainLayout;
    private String activeCardFilter = null;
    private Document selectedDocumentForRenewal;
    private boolean alertExpanded = false;

    public void setUserData(int userId, String userName, String role) {
        this.currentUserId = userId;
        this.currentUserRole = role;
        lblUserName.setText(userName);
        lblUserRole.setText(role.toUpperCase());

        // Sembunyikan menu User dan Tindak Lanjut untuk Staff
        if (role != null && (role.equalsIgnoreCase("staff") || role.equalsIgnoreCase("user"))) {
            if (btnUser != null) {
                btnUser.setVisible(false);
                btnUser.setManaged(false);
            }
            if (btnTindakLanjut != null) {
                btnTindakLanjut.setVisible(false);
                btnTindakLanjut.setManaged(false);
            }
        }

        initializeComboBox();
        initializeTable();
        loadDashboardData();
    }

    public void setMainLayout(BorderPane layout) {
        this.mainLayout = layout;
    }

    public void refreshDashboardData() {
        currentPage = 1;
        activeCardFilter = null;
        resetCardStyles();
        loadDashboardData();
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
        allDocumentsForChart.clear();
        documentList.clear();

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
            }

            loadAllDocumentsForChart();

            int totalMou = 0, totalPks = 0, active = 0, expired = 0, mendesak = 0, peringatan = 0;
            LocalDate today = LocalDate.now();

            for (Document doc : allDocumentsForChart) {
                String jenis = doc.getJenis() != null ? doc.getJenis().toUpperCase() : "";

                if (jenis.contains("MOU")) totalMou++;
                if (jenis.contains("PKS")) totalPks++;

                if (doc.getTanggalBerakhir() != null && doc.getTanggalBerakhir().isBefore(today)) {
                    expired++;
                } else {
                    active++;
                }

                // Mendesak: tanggal_berakhir dalam 7 hari atau kurang (dan belum lewat)
                // Peringatan: tanggal_berakhir dalam 8-14 hari
                if (doc.getTanggalBerakhir() != null) {
                    long daysRemaining = ChronoUnit.DAYS.between(today, doc.getTanggalBerakhir());
                    if (daysRemaining >= 0 && daysRemaining <= 7) {
                        mendesak++;
                    } else if (daysRemaining > 7 && daysRemaining <= 14) {
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

            // Update alert detail content if expanded
            if (alertExpanded) {
                buildAlertDetailContent();
            }

            lblTotalDocs.setText("Total : " + totalItems);
            int start = offset + 1;
            int end = Math.min(offset + itemsPerPage, totalItems);
            lblPageInfo.setText("Menampilkan " + start + " - " + end + " dari " + totalItems + " dokumen");

            tableDocuments.setItems(documentList);
            updatePaginationButtons();

            initializeCharts();
            updateCharts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeCharts() {
        if (barChartMoUPks != null) barChartMoUPks.getData().clear();
        if (areaChartTren != null) areaChartTren.getData().clear();

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
        setupAreaChart();
    }

    private void setupBarChart() {
        barChartMoUPks.setAnimated(false);
        barChartMoUPks.setStyle("-fx-background-color: transparent;");
        barChartMoUPks.setLegendSide(javafx.geometry.Side.BOTTOM);

        Node legend = barChartMoUPks.lookup(".chart-legend");
        if (legend != null) {
            legend.setStyle("-fx-background-color: transparent; -fx-padding: 10 0 0 0; -fx-font-size: 11px;");
        }

        CategoryAxis xAxis = (CategoryAxis) barChartMoUPks.getXAxis();
        xAxis.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#64748b"));

        NumberAxis yAxis = (NumberAxis) barChartMoUPks.getYAxis();
        yAxis.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#64748b"));
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object.doubleValue() == object.intValue()) {
                    return String.valueOf(object.intValue());
                }
                return "";
            }
            @Override
            public Number fromString(String string) {
                return Integer.parseInt(string);
            }
        });
    }

    private void setupAreaChart() {
        areaChartTren.setAnimated(false);
        areaChartTren.setStyle("-fx-background-color: transparent;");
        areaChartTren.setCreateSymbols(true);

        CategoryAxis xAxis = (CategoryAxis) areaChartTren.getXAxis();
        xAxis.getCategories().clear();
        xAxis.getCategories().addAll("Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des");
        xAxis.setStyle("-fx-font-size: 11px;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#64748b"));

        NumberAxis yAxis = (NumberAxis) areaChartTren.getYAxis();
        yAxis.setStyle("-fx-font-size: 11px;");
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#64748b"));
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object.doubleValue() == object.intValue()) {
                    return String.valueOf(object.intValue());
                }
                return "";
            }
            @Override
            public Number fromString(String string) {
                return Integer.parseInt(string);
            }
        });

        areaChartTren.setLegendSide(javafx.geometry.Side.BOTTOM);
        Node legend = areaChartTren.lookup(".chart-legend");
        if (legend != null) {
            legend.setStyle("-fx-background-color: transparent; -fx-padding: 10 0 0 0; -fx-font-size: 11px;");
        }
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
        updateAreaChart();
    }

    private void updateBarChart() {
        int mouAktif = 0, mouExpired = 0, pksAktif = 0, pksExpired = 0;
        LocalDate today = LocalDate.now();

        for (Document doc : allDocumentsForChart) {
            boolean isExpired = doc.getTanggalBerakhir() != null && doc.getTanggalBerakhir().isBefore(today);
            String jenis = doc.getJenis() != null ? doc.getJenis().toUpperCase() : "";

            if (jenis.contains("MOU")) {
                if (isExpired) mouExpired++;
                else mouAktif++;
            } else if (jenis.contains("PKS")) {
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
        final String TEAL_COLOR = "#07b8af";
        final String TEAL_SHADOW = "rgba(7, 184, 175, 0.3)";
        final String ORANGE_COLOR = "#FF6B35";
        final String ORANGE_SHADOW = "rgba(255, 107, 53, 0.3)";

        Platform.runLater(() -> {
            // Series 0 = Aktif (teal) - bottom of stacked bar, no rounded corners on top
            if (barChartMoUPks.getData().size() > 0) {
                for (XYChart.Data<String, Number> data : barChartMoUPks.getData().get(0).getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle(
                                "-fx-background-color: " + TEAL_COLOR + "; " +
                                        "-fx-background-radius: 0 0 0 0; " +
                                        "-fx-effect: dropshadow(gaussian, " + TEAL_SHADOW + ", 8, 0, 0, 2);"
                        );
                    }
                }
            }

            // Series 1 = Kadaluarsa (orange) - top of stacked bar, rounded top corners
            if (barChartMoUPks.getData().size() > 1) {
                for (XYChart.Data<String, Number> data : barChartMoUPks.getData().get(1).getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle(
                                "-fx-background-color: " + ORANGE_COLOR + "; " +
                                        "-fx-background-radius: 8 8 0 0; " +
                                        "-fx-effect: dropshadow(gaussian, " + ORANGE_SHADOW + ", 8, 0, 0, 2);"
                        );
                    }
                }
            }
        });
    }

    private void updateAreaChart() {
        String selectedYear = cbTahun.getValue();
        if (selectedYear == null) selectedYear = String.valueOf(LocalDate.now().getYear());

        int year = Integer.parseInt(selectedYear);

        int[] mouPerMonth = new int[12];
        int[] pksPerMonth = new int[12];

        for (Document doc : allDocumentsForChart) {
            if (doc.getTanggalMulai() != null && doc.getTanggalMulai().getYear() == year) {
                int month = doc.getTanggalMulai().getMonthValue() - 1;
                String jenis = doc.getJenis() != null ? doc.getJenis().toUpperCase() : "";
                if (jenis.contains("MOU")) {
                    mouPerMonth[month]++;
                } else if (jenis.contains("PKS")) {
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

        areaChartTren.getData().clear();
        areaChartTren.getData().addAll(seriesMoU, seriesPKS);

        applyAreaChartColors();
    }

    private void applyAreaChartColors() {
        final String TEAL_COLOR = "#07b8af";
        final String TEAL_FILL = "rgba(7, 184, 175, 0.2)";
        final String TEAL_SHADOW = "rgba(7, 184, 175, 0.4)";
        final String ORANGE_COLOR = "#FF6B35";
        final String ORANGE_FILL = "rgba(255, 107, 53, 0.2)";
        final String ORANGE_SHADOW = "rgba(255, 107, 53, 0.4)";

        // Force CSS and layout pass first so chart creates all Path nodes,
        // then apply styling and smoothing in a second deferred pass
        Platform.runLater(() -> {
            areaChartTren.applyCss();
            areaChartTren.layout();

            Platform.runLater(() -> {
                // Style and smooth MoU Baru series (teal area)
                if (areaChartTren.getData().size() > 0) {
                    XYChart.Series<String, Number> seriesMoU = areaChartTren.getData().get(0);
                    int dataCount0 = seriesMoU.getData().size();
                    Node seriesNode = seriesMoU.getNode();
                    if (seriesNode != null) {
                        Node areaLine = seriesNode.lookup(".chart-series-area-line");
                        if (areaLine != null) {
                            areaLine.setStyle(
                                    "-fx-stroke: " + TEAL_COLOR + "; -fx-stroke-width: 2.5px;"
                            );
                            if (areaLine instanceof Path) {
                                smoothPath((Path) areaLine, dataCount0);
                            }
                        }
                        Node areaFill = seriesNode.lookup(".chart-series-area-fill");
                        if (areaFill != null) {
                            areaFill.setStyle(
                                    "-fx-fill: " + TEAL_FILL + ";"
                            );
                            if (areaFill instanceof Path) {
                                smoothPath((Path) areaFill, dataCount0);
                            }
                        }
                    }

                    // Style data point symbols
                    for (XYChart.Data<String, Number> data : seriesMoU.getData()) {
                        Node node = data.getNode();
                        if (node != null) {
                            node.setStyle(
                                    "-fx-background-color: " + TEAL_COLOR + ", white; " +
                                            "-fx-background-insets: 0, 2; " +
                                            "-fx-background-radius: 5px; " +
                                            "-fx-padding: 4px; " +
                                            "-fx-effect: dropshadow(gaussian, " + TEAL_SHADOW + ", 4, 0, 0, 1);"
                            );
                        }
                    }
                }

                // Style and smooth PKS Baru series (orange area)
                if (areaChartTren.getData().size() > 1) {
                    XYChart.Series<String, Number> seriesPKS = areaChartTren.getData().get(1);
                    int dataCount1 = seriesPKS.getData().size();
                    Node seriesNode = seriesPKS.getNode();
                    if (seriesNode != null) {
                        Node areaLine = seriesNode.lookup(".chart-series-area-line");
                        if (areaLine != null) {
                            areaLine.setStyle(
                                    "-fx-stroke: " + ORANGE_COLOR + "; -fx-stroke-width: 2.5px;"
                            );
                            if (areaLine instanceof Path) {
                                smoothPath((Path) areaLine, dataCount1);
                            }
                        }
                        Node areaFill = seriesNode.lookup(".chart-series-area-fill");
                        if (areaFill != null) {
                            areaFill.setStyle(
                                    "-fx-fill: " + ORANGE_FILL + ";"
                            );
                            if (areaFill instanceof Path) {
                                smoothPath((Path) areaFill, dataCount1);
                            }
                        }
                    }

                    // Style data point symbols
                    for (XYChart.Data<String, Number> data : seriesPKS.getData()) {
                        Node node = data.getNode();
                        if (node != null) {
                            node.setStyle(
                                    "-fx-background-color: " + ORANGE_COLOR + ", white; " +
                                            "-fx-background-insets: 0, 2; " +
                                            "-fx-background-radius: 5px; " +
                                            "-fx-padding: 4px; " +
                                            "-fx-effect: dropshadow(gaussian, " + ORANGE_SHADOW + ", 4, 0, 0, 1);"
                            );
                        }
                    }
                }
            });
        });
    }

    /**
     * Converts straight-line segments in a Path to smooth cubic Bezier curves
     * using Catmull-Rom spline interpolation. Only smooths the top data-point
     * segments, leaving the bottom fill border of area charts unchanged.
     *
     * @param path           the Path to smooth
     * @param numDataPoints  how many data points form the top curve
     */
    private void smoothPath(Path path, int numDataPoints) {
        javafx.collections.ObservableList<PathElement> elements = path.getElements();
        if (elements.size() < 3) return;

        // Extract all coordinate points from the top curve (MoveTo + LineTo)
        List<double[]> points = new ArrayList<>();
        int elementIndex = 0;

        for (PathElement elem : elements) {
            if (points.size() >= numDataPoints) break;
            if (elem instanceof MoveTo) {
                MoveTo mt = (MoveTo) elem;
                points.add(new double[]{mt.getX(), mt.getY()});
            } else if (elem instanceof javafx.scene.shape.LineTo) {
                javafx.scene.shape.LineTo lt = (javafx.scene.shape.LineTo) elem;
                points.add(new double[]{lt.getX(), lt.getY()});
            }
            elementIndex++;
        }

        if (points.size() < 3) return;

        // Build new smooth path elements
        List<PathElement> newElements = new ArrayList<>();
        // Keep the original MoveTo
        newElements.add(new MoveTo(points.get(0)[0], points.get(0)[1]));

        // Convert each LineTo to a CubicCurveTo using Catmull-Rom to Bezier
        for (int i = 1; i < points.size(); i++) {
            double[] pPrev = points.get(Math.max(0, i - 2));
            double[] p0 = points.get(i - 1);
            double[] p1 = points.get(i);
            double[] pNext = points.get(Math.min(points.size() - 1, i + 1));

            // Catmull-Rom to cubic Bezier control points
            double cp1x = p0[0] + (p1[0] - pPrev[0]) / 6.0;
            double cp1y = p0[1] + (p1[1] - pPrev[1]) / 6.0;
            double cp2x = p1[0] - (pNext[0] - p0[0]) / 6.0;
            double cp2y = p1[1] - (pNext[1] - p0[1]) / 6.0;

            newElements.add(new CubicCurveTo(cp1x, cp1y, cp2x, cp2y, p1[0], p1[1]));
        }

        // Append remaining elements unchanged (bottom border of fill path)
        int topElementCount = points.size(); // MoveTo + (n-1) LineTo = n elements total
        for (int i = topElementCount; i < elements.size(); i++) {
            newElements.add(elements.get(i));
        }

        elements.clear();
        elements.addAll(newElements);
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
        return months[month];
    }

    private int getMouActiveCount() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Document doc : allDocumentsForChart) {
            String jenis = doc.getJenis() != null ? doc.getJenis().toUpperCase() : "";
            if (jenis.contains("MOU") && doc.getTanggalBerakhir() != null && !doc.getTanggalBerakhir().isBefore(today)) {
                count++;
            }
        }
        return count;
    }

    private int getPksActiveCount() {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (Document doc : allDocumentsForChart) {
            String jenis = doc.getJenis() != null ? doc.getJenis().toUpperCase() : "";
            if (jenis.contains("PKS") && doc.getTanggalBerakhir() != null && !doc.getTanggalBerakhir().isBefore(today)) {
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
        activeCardFilter = null;

        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();

                DashboardController controller = loader.getController();
                controller.setUserData(currentUserId, lblUserName.getText(), currentUserRole);

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_document_tabs.fxml"));
            Parent formRoot = loader.load();

            AddDocumentTabsController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            controller.setDashboardController(this);

            if (mainLayout != null) {
                mainLayout.setCenter(formRoot);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form: " + e.getMessage());
        }
        updateMenuStyle(btnTambah);
    }

    @FXML
    private void handleViewDocuments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/all_documents.fxml"));
            Parent root = loader.load();

            AllDocumentsController controller = loader.getController();
            controller.setUserData(currentUserId, lblUserName.getText(), currentUserRole);
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

    @FXML
    private void handleTindakLanjut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tindak_lanjut.fxml"));
            Parent root = loader.load();

            TindakLanjutController controller = loader.getController();
            controller.setUserData(currentUserId, lblUserName.getText(), currentUserRole);
            controller.setMainLayout(mainLayout);

            if (mainLayout != null) {
                mainLayout.setCenter(root);
            }

            updateMenuStyle(btnTindakLanjut);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka halaman tindak lanjut: " + e.getMessage());
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

    @FXML
    private void handleToggleAlert() {
        alertExpanded = !alertExpanded;
        alertDetailContainer.setVisible(alertExpanded);
        alertDetailContainer.setManaged(alertExpanded);

        if (alertExpanded) {
            lblAlertToggle.setText("Klik untuk sembunyikan");
            alertBanner.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-width: 1 1 0 1; -fx-border-radius: 12 12 0 0; -fx-background-radius: 12 12 0 0; -fx-padding: 18 24; -fx-cursor: hand;");
            buildAlertDetailContent();
        } else {
            lblAlertToggle.setText("Klik untuk selengkapnya");
            alertBanner.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 18 24; -fx-cursor: hand;");
            alertDetailContainer.getChildren().clear();
        }
    }

    private void buildAlertDetailContent() {
        alertDetailContainer.getChildren().clear();

        LocalDate today = LocalDate.now();
        List<Document> mendesakDocs = new ArrayList<>();
        List<Document> peringatanDocs = new ArrayList<>();

        for (Document doc : allDocumentsForChart) {
            if (doc.getTanggalBerakhir() != null) {
                long daysRemaining = ChronoUnit.DAYS.between(today, doc.getTanggalBerakhir());
                if (daysRemaining >= 0 && daysRemaining <= 7) {
                    mendesakDocs.add(doc);
                } else if (daysRemaining > 7 && daysRemaining <= 14) {
                    peringatanDocs.add(doc);
                }
            }
        }

        // Section: SANGAT MENDESAK
        if (!mendesakDocs.isEmpty()) {
            Label sectionTitle = new Label("⚠ SANGAT MENDESAK (≤7 hari)");
            sectionTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            alertDetailContainer.getChildren().add(sectionTitle);

            for (Document doc : mendesakDocs) {
                alertDetailContainer.getChildren().add(createAlertDocCard(doc, true));
            }
        }

        // Section: PERINGATAN
        if (!peringatanDocs.isEmpty()) {
            Label sectionTitle2 = new Label("⏰ PERINGATAN (≤14 hari)");
            sectionTitle2.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f97316; -fx-padding: 10 0 0 0;");
            alertDetailContainer.getChildren().add(sectionTitle2);

            for (Document doc : peringatanDocs) {
                alertDetailContainer.getChildren().add(createAlertDocCard(doc, false));
            }
        }

        if (mendesakDocs.isEmpty() && peringatanDocs.isEmpty()) {
            Label noAlert = new Label("✅ Tidak ada dokumen yang memerlukan perhatian saat ini.");
            noAlert.setStyle("-fx-font-size: 14px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
            alertDetailContainer.getChildren().add(noAlert);
        }
    }

    private VBox createAlertDocCard(Document doc, boolean isMendesak) {
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, doc.getTanggalBerakhir());

        String borderColor = isMendesak ? "#ef4444" : "#f97316";
        String bgColor = isMendesak ? "#fef2f2" : "#fff7ed";

        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; " +
                "-fx-border-radius: 10; -fx-padding: 16 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);");

        // Header: Jenis badge + Mitra
        String jenisText = doc.getJenis() != null ? doc.getJenis() : "";
        if (jenisText.contains("(")) jenisText = jenisText.substring(0, jenisText.indexOf("(")).trim();
        if (jenisText.contains(" ")) jenisText = jenisText.split(" ")[0];

        Label jenisBadge = new Label(jenisText);
        jenisBadge.setStyle(getJenisStyle(jenisText) + "-fx-padding: 4 10; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label mitraLabel = new Label(doc.getMitra() != null ? doc.getMitra() : "-");
        mitraLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        mitraLabel.setWrapText(true);

        HBox headerBox = new HBox(10, jenisBadge, mitraLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Date info
        Label dateLabel = new Label("📅 " + formatTanggalIndonesia(doc.getTanggalBerakhir()));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        // Days remaining
        Label daysLabel = new Label("⏱ " + daysRemaining + " hari lagi");
        daysLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (isMendesak ? "#dc2626" : "#ea580c") + "; -fx-font-weight: bold;");

        HBox dateBox = new HBox(20, dateLabel, daysLabel);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        // Warning message box
        String warningTitle = isMendesak
                ? "🚨 SEGERA SIAPKAN DOKUMEN DAN LAKUKAN KOORDINASI"
                : "⚠ PERHATIKAN MASA BERLAKU DOKUMEN";
        String warningMsg = isMendesak
                ? "Masa berlaku dokumen akan segera berakhir dalam waktu kurang dari 7 hari.\nSegera lakukan koordinasi dengan pihak terkait untuk memastikan kelengkapan administrasi."
                : "Masa berlaku dokumen akan berakhir dalam waktu kurang dari 14 hari.\nPersiapkan dokumen perpanjangan atau tindak lanjut yang diperlukan.";

        Label warningTitleLabel = new Label(warningTitle);
        warningTitleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (isMendesak ? "#991b1b" : "#9a3412") + ";");

        Label warningMsgLabel = new Label(warningMsg);
        warningMsgLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (isMendesak ? "#b91c1c" : "#c2410c") + ";");
        warningMsgLabel.setWrapText(true);

        VBox warningBox = new VBox(4, warningTitleLabel, warningMsgLabel);
        warningBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8; -fx-padding: 12 16;");

        card.getChildren().addAll(headerBox, dateBox, warningBox);
        return card;
    }

    private void updateMenuStyle(Button activeButton) {
        String activeStyle = "-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #475569; -fx-padding: 12 16; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

        Button[] buttons = {btnDashboard, btnTambah, btnSemuaDokumen, btnTindakLanjut, btnUser};
        for (Button btn : buttons) {
            if (btn != null && btn.isVisible()) {
                if (btn == activeButton) {
                    btn.setStyle(activeStyle);
                } else {
                    btn.setStyle(inactiveStyle);
                }
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
        if ("MOU".equals(filterType)) sql += " AND UPPER(jenis) LIKE '%MOU%'";
        else if ("PKS".equals(filterType)) sql += " AND UPPER(jenis) LIKE '%PKS%'";
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

        Label lblTanggal = new Label(" " + formatTanggalIndonesia(history.getCreatedAt().toLocalDateTime().toLocalDate()));
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