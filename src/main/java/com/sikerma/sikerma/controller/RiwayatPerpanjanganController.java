package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import com.sikerma.sikerma.model.RenewalHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RiwayatPerpanjanganController {

    @FXML private Button btnKembali;
    @FXML private Label lblJenisBadge;
    @FXML private Label lblNomorDokumen;
    @FXML private Label lblMitra;
    @FXML private Label lblTanggalBerakhir;
    @FXML private Label lblStatus;
    @FXML private Label lblPageInfo;

    @FXML private TableView<RenewalHistory> tableRiwayat;
    @FXML private TableColumn<RenewalHistory, Integer> colNo;
    @FXML private TableColumn<RenewalHistory, LocalDateTime> colTanggalPerpanjangan;
    @FXML private TableColumn<RenewalHistory, String> colPeriodeBerlaku;
    // DIHAPUS: @FXML private TableColumn<RenewalHistory, String> colDiperpanjangOleh;
    @FXML private TableColumn<RenewalHistory, String> colCatatan;

    @FXML private Button btnPrevPage;
    @FXML private Button btnPage1;
    @FXML private Button btnPage2;
    @FXML private Button btnNextPage;

    private ObservableList<RenewalHistory> riwayatList = FXCollections.observableArrayList();
    private ObservableList<RenewalHistory> allRiwayatList = FXCollections.observableArrayList();
    private Document selectedDocument;
    private BorderPane mainLayout;
    private int currentUserId;
    private String currentUserName;
    private String currentUserRole;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;

    public void setDocumentData(Document doc) {
        this.selectedDocument = doc;
        loadDocumentInfo();
        initializeTable();
        loadRiwayatPerpanjangan();
    }

    public void setMainLayout(BorderPane layout) {
        this.mainLayout = layout;
    }

    public void setUserData(int userId, String userName, String role) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.currentUserRole = role;
    }

    private void loadDocumentInfo() {
        if (selectedDocument == null) return;

        String jenis = selectedDocument.getJenis();
        String displayText = jenis;
        if (jenis != null && jenis.contains("(")) displayText = jenis.substring(0, jenis.indexOf("(")).trim();
        if (displayText != null && displayText.contains(" ")) displayText = displayText.split(" ")[0];

        lblJenisBadge.setText(displayText != null ? displayText : "-");
        if (jenis != null && jenis.toLowerCase().contains("pks")) {
            lblJenisBadge.setStyle("-fx-background-color: #ffedd5; -fx-text-fill: #c2410c; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: bold;");
        } else {
            lblJenisBadge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #047857; -fx-padding: 6 12; -fx-background-radius: 6; -fx-font-weight: bold;");
        }

        lblNomorDokumen.setText(selectedDocument.getNomorDokumen() != null ? selectedDocument.getNomorDokumen() : "-");
        lblMitra.setText(selectedDocument.getMitra() != null ? selectedDocument.getMitra() : "-");

        if (selectedDocument.getTanggalBerakhir() != null) {
            String tglBerakhir = formatTanggalIndonesia(selectedDocument.getTanggalBerakhir());
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                    selectedDocument.getTanggalBerakhir(), LocalDate.now());

            if (daysOverdue > 0) {
                tglBerakhir += " (" + daysOverdue + " hari lewat)";
                lblTanggalBerakhir.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            } else {
                lblTanggalBerakhir.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
            }
            lblTanggalBerakhir.setText(tglBerakhir);
        } else {
            lblTanggalBerakhir.setText("-");
        }

        String status = selectedDocument.getStatus() != null ? selectedDocument.getStatus() : "-";
        lblStatus.setText(status);
        if (status.equals("Kadaluarsa")) {
            lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
        } else if (status.equals("Baru")) {
            lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #14b8a6; -fx-font-weight: bold;");
        } else {
            lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        }
    }

    private void initializeTable() {
        colNo.setCellFactory(column -> new TableCell<RenewalHistory, Integer>() {
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

        colTanggalPerpanjangan.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colTanggalPerpanjangan.setCellFactory(column -> new TableCell<RenewalHistory, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTanggalIndonesia(item.toLocalDate()) + " " +
                            item.getHour() + ":" + String.format("%02d", item.getMinute()));
                    setStyle("-fx-text-fill: #475569; -fx-padding: 12 5;");
                }
            }
        });

        colPeriodeBerlaku.setCellValueFactory(cellData -> {
            RenewalHistory history = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    history != null ? formatTanggalIndonesia(history.getOldEndDate()) + " - " +
                            formatTanggalIndonesia(history.getNewEndDate()) : ""
            );
        });
        colPeriodeBerlaku.setCellFactory(column -> new TableCell<RenewalHistory, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 12 5;");
                }
            }
        });

        // DIHAPUS: colDiperpanjangOleh dan semua kode terkait

        colCatatan.setCellValueFactory(new PropertyValueFactory<>("notes"));
        colCatatan.setCellFactory(column -> new TableCell<RenewalHistory, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("-");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #475569; -fx-padding: 12 5;");
                }
            }
        });

        tableRiwayat.setFixedCellSize(70);
        tableRiwayat.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
    }

    private void loadRiwayatPerpanjangan() {
        allRiwayatList.clear();

        String sql = "SELECT * FROM document_renewal_history WHERE document_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedDocument.getId());
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

                allRiwayatList.add(history);
            }

            totalPages = (int) Math.ceil((double) allRiwayatList.size() / itemsPerPage);
            if (totalPages == 0) totalPages = 1;
            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            updateTableDisplay();

        } catch (Exception e) {
            e.printStackTrace();
            lblPageInfo.setText("Belum ada riwayat perpanjangan");
        }
    }

    private void updateTableDisplay() {
        riwayatList.clear();
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allRiwayatList.size());

        for (int i = start; i < end; i++) {
            riwayatList.add(allRiwayatList.get(i));
        }

        tableRiwayat.setItems(riwayatList);

        if (allRiwayatList.isEmpty()) {
            lblPageInfo.setText("Belum ada riwayat perpanjangan");
        } else {
            int startDisplay = start + 1;
            int endDisplay = end;
            lblPageInfo.setText("Menampilkan " + startDisplay + " - " + endDisplay + " dari " + allRiwayatList.size() + " riwayat");
        }

        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        Button[] pageButtons = {btnPage1, btnPage2};
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
    private void handleKembali() {
        navigateToTindakLanjut();
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

    private void navigateToTindakLanjut() {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tindak_lanjut.fxml"));
                Parent root = loader.load();
                TindakLanjutController controller = loader.getController();
                controller.setUserData(currentUserId, currentUserName, currentUserRole);
                controller.setMainLayout(mainLayout);
                mainLayout.setCenter(root);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Gagal kembali ke halaman tindak lanjut: " + e.getMessage());
            }
        }
    }

    private String formatTanggalIndonesia(LocalDate date) {
        if (date == null) return "-";
        String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        return date.getDayOfMonth() + " " + bulan[date.getMonthValue() - 1] + " " + date.getYear();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}