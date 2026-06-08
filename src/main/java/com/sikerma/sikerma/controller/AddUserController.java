package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddUserController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNamaLengkap;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;

    private UserManagementController managementController;

    public void setDashboardController(UserManagementController controller) {
        this.managementController = controller;
    }

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("User", "Administrator");
        cbRole.setValue("User");
    }

    @FXML
    private void handleSave() {
        if (txtUsername.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() ||
                txtNamaLengkap.getText().trim().isEmpty() ||
                txtPassword.getText().trim().isEmpty()) {
            showAlert("Validasi", "Semua field harus diisi!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseConfig.connect()) {
            // ✅ SESUAIKAN DENGAN STRUKTUR TABEL: full_name, password_hash, role (admin/staff), is_active
            String sql = "INSERT INTO users (username, email, full_name, password_hash, role, is_active, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, 1, NOW())";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtUsername.getText().trim());
            pstmt.setString(2, txtEmail.getText().trim());
            pstmt.setString(3, txtNamaLengkap.getText().trim());
            pstmt.setString(4, txtPassword.getText()); // TODO: Hash password untuk production

            // ✅ KONVERSI ROLE: "Administrator" -> "admin", "User" -> "staff"
            String roleValue = cbRole.getValue();
            String dbRole = "Administrator".equals(roleValue) ? "admin" : "staff";
            pstmt.setString(5, dbRole);

            pstmt.executeUpdate();

            showAlert("Sukses", "User berhasil ditambahkan!", Alert.AlertType.INFORMATION);

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menambah user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}