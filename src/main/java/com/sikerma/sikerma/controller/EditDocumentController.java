package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.Document;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class EditDocumentController {

    @FXML private TextField txtNomorDokumen;
    @FXML private ComboBox<String> cbJenis;
    @FXML private TextField txtMitra;
    @FXML private DatePicker dpTanggalMulai;
    @FXML private DatePicker dpTanggalBerakhir;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextArea txtKeterangan;

    private int currentDocId;
    private int currentUserId;

    public void setCurrentUserId(int userId) { this.currentUserId = userId; }

    // ✅ METHOD INI DIPANGGIL DARI DASHBOARD SAAT KLIK EDIT
    public void setDocumentData(Document doc) {
        this.currentDocId = doc.getId();
        txtNomorDokumen.setText(doc.getNomorDokumen());

        cbJenis.getItems().addAll("MoU", "PKS");
        cbJenis.setValue(doc.getJenis());

        txtMitra.setText(doc.getMitra());

        if (doc.getTanggalBerakhir() != null) dpTanggalBerakhir.setValue(doc.getTanggalBerakhir());

        cbStatus.getItems().addAll("Aktif", "Perlu Perhatian", "Kadaluarsa", "Baru", "Dalam Proses", "Selesai");
        cbStatus.setValue(doc.getStatus());

        txtKeterangan.setText(doc.getKeterangan() != null ? doc.getKeterangan() : "");
    }

    @FXML
    private void handleUpdate() {
        if (txtNomorDokumen.getText().trim().isEmpty() || cbJenis.getValue() == null) {
            showAlert("Validasi", "Nomor Dokumen dan Jenis wajib diisi!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConfig.connect()) {
            String sql = "UPDATE documents SET nomor_dokumen = ?, jenis = ?, mitra = ?, " +
                    "tanggal_mulai = ?, tanggal_berakhir = ?, status = ?, keterangan = ? " +
                    "WHERE id = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtNomorDokumen.getText().trim());
            pstmt.setString(2, cbJenis.getValue());
            pstmt.setString(3, txtMitra.getText().trim());
            pstmt.setString(4, dpTanggalMulai.getValue() != null ? dpTanggalMulai.getValue().toString() : "");
            pstmt.setString(5, dpTanggalBerakhir.getValue() != null ? dpTanggalBerakhir.getValue().toString() : "");
            pstmt.setString(6, cbStatus.getValue());
            pstmt.setString(7, txtKeterangan.getText().trim());
            pstmt.setInt(8, currentDocId);

            pstmt.executeUpdate();
            showAlert("Sukses", "Dokumen berhasil diperbarui.", Alert.AlertType.INFORMATION);
            ((Stage) txtNomorDokumen.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menyimpan perubahan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML private void handleBack() { closeWindow(); }
    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) txtNomorDokumen.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }
}