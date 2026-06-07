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
    public void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // 1. Validasi input kosong
        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("⚠️ Username dan Password harus diisi!");
            lblMessage.setStyle("-fx-text-fill: red;");
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

                showAlert("Login Berhasil!", "Selamat datang, " + fullName + "!", Alert.AlertType.INFORMATION);

                Stage loginStage = (Stage) txtUsername.getScene().getWindow();
                loginStage.close();

                openDashboard(fullName, role, userId);

            } else {
                lblMessage.setText("❌ Username atau Password salah!");
                lblMessage.setStyle("-fx-text-fill: red;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("❌ Error: " + e.getMessage());
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
            BorderPane root = loader.load(); // ✅ Ubah dari Parent ke BorderPane

            Scene scene = new Scene(root, 1200, 750);

            DashboardController controller = loader.getController();
            controller.setUserData(userId, fullName, role);

            controller.setMainLayout(root);

            Stage stage = new Stage();
            stage.setTitle("Dashboard SIKERMA - " + role.toUpperCase());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka Dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}