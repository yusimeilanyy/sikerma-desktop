package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;

    @FXML
    public void initialize() {
        if (txtUsername != null) {
            txtUsername.requestFocus();
        }

        if (txtUsername != null) {
            txtUsername.textProperty().addListener((observable, oldValue, newValue) -> {
                clearErrorMessage();
            });
        }

        if (txtPassword != null) {
            txtPassword.textProperty().addListener((observable, oldValue, newValue) -> {
                clearErrorMessage();
            });
        }
    }

    private void clearErrorMessage() {
        if (lblMessage != null) {
            lblMessage.setText("");
        }
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // 1. Validasi input kosong
        if (username.isEmpty() || password.isEmpty()) {
            showError("⚠️ Username dan Password harus diisi!");
            return;
        }

        // 2. Query ke Database
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ? AND is_active = 1";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");
                int userId = rs.getInt("id");

                // Show success message briefly
                showSuccess("✅ Login berhasil! Selamat datang, " + fullName);

                // Close login stage
                Stage loginStage = (Stage) txtUsername.getScene().getWindow();
                loginStage.close();

                // Open dashboard
                openDashboard(fullName, role, userId);

            } else {
                showError("❌ Username atau Password salah!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
            lblMessage.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 600;");
        }
    }

    private void showSuccess(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
            lblMessage.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: 600;");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openDashboard(String fullName, String role, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 1400, 800);

            DashboardController controller = loader.getController();
            controller.setUserData(userId, fullName, role);
            controller.setMainLayout(root);

            Stage stage = new Stage();
            stage.setTitle("Dashboard SIKERMA - " + role.toUpperCase());
            stage.setScene(scene);
            stage.setMaximized(true); // Fullscreen
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka Dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}