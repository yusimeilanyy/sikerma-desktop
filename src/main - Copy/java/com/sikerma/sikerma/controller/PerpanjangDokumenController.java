package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class PerpanjangDokumenController {

    @FXML private Button btnKembali;
    @FXML private Button btnBatal;
    @FXML private Button btnSimpan;

    @FXML private Label lblJenis;
    @FXML private Label lblNomor;
    @FXML private Label lblMitra;
    @FXML private Label lblTanggalMulai;
    @FXML private Label lblTanggalBerakhir;
    @FXML private Label lblStatus;

    @FXML private DatePicker dpTanggalBaru;
    @FXML private TextArea txtCatatan;

    private Document selectedDocument;
    private BorderPane mainLayout;
    private int currentUserId;
    private String currentUserName;
    private String currentUserRole;

    public void setDocumentData(Document doc) {
        this.selectedDocument = doc;
        loadDocumentInfo();
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

        lblJenis.setText(selectedDocument.getJenis());
        lblNomor.setText(selectedDocument.getNomorDokumen());
        lblMitra.setText(selectedDocument.getMitra());

        if (selectedDocument.getTanggalMulai() != null) {
            lblTanggalMulai.setText(formatTanggalIndonesia(selectedDocument.getTanggalMulai()));
        } else {
            lblTanggalMulai.setText("-");
        }

        if (selectedDocument.getTanggalBerakhir() != null) {
            String tglBerakhir = formatTanggalIndonesia(selectedDocument.getTanggalBerakhir());
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                    selectedDocument.getTanggalBerakhir(), LocalDate.now());

            if (daysOverdue > 0) {
                tglBerakhir += " (" + daysOverdue + " hari lewat)";
            }
            lblTanggalBerakhir.setText(tglBerakhir);
        } else {
            lblTanggalBerakhir.setText("-");
        }

        lblStatus.setText(selectedDocument.getStatus());
    }

    @FXML
    private void handleKembali() {
        navigateToTindakLanjut();
    }

    @FXML
    private void handleBatal() {
        navigateToTindakLanjut();
    }

    @FXML
    private void handleSimpan() {
        if (selectedDocument == null) {
            showAlert("Error", "Data dokumen tidak ditemukan!");
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

        savePerpanjangan(tanggalBaru, txtCatatan.getText());
    }

    private void savePerpanjangan(LocalDate tanggalBaru, String catatan) {
        String sql = "UPDATE documents SET tanggal_berakhir = ?, status = 'Baru', updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(tanggalBaru));
            pstmt.setInt(2, selectedDocument.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                insertRiwayatPerpanjangan(tanggalBaru, catatan);
                showAlert("Sukses", "Dokumen berhasil diperpanjang hingga " + formatTanggalIndonesia(tanggalBaru));
                navigateToTindakLanjut();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memperpanjang dokumen: " + e.getMessage());
        }
    }

    private void insertRiwayatPerpanjangan(LocalDate tanggalBaru, String catatan) {
        ensureRiwayatTableExists();

        String sql = "INSERT INTO document_renewal_history (document_id, old_end_date, " +
                "new_end_date, previous_status, new_status, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedDocument.getId());
            pstmt.setDate(2, Date.valueOf(selectedDocument.getTanggalBerakhir()));
            pstmt.setDate(3, Date.valueOf(tanggalBaru));
            pstmt.setString(4, selectedDocument.getStatus());
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