package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserManagementController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colNamaLengkap;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colDibuat;
    @FXML private TableColumn<User, Void> colAksi;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private int currentUserId;
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        initializeTable();
        loadUsers();
    }

    private void initializeTable() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNamaLengkap.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDibuat.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "-"
            );
        });

        // ✅ CENTER ALIGNMENT UNTUK USERNAME
        colUsername.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569;");
                }
            }
        });

        // ✅ CENTER ALIGNMENT UNTUK EMAIL
        colEmail.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569;");
                }
            }
        });

        // ✅ CENTER ALIGNMENT UNTUK NAMA LENGKAP
        colNamaLengkap.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569;");
                }
            }
        });

        // ✅ CENTER ALIGNMENT UNTUK DIBUAT
        colDibuat.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-text-fill: #475569;");
                }
            }
        });

        colRole.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Administrator".equals(item)) {
                        badge.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #6b21a8; " +
                                "-fx-background-radius: 6; -fx-padding: 4 12; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                                "-fx-background-radius: 6; -fx-padding: 4 12; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-background-color: #ccfbf1; -fx-text-fill: #0f766e; " +
                            "-fx-background-radius: 6; -fx-padding: 4 12; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colAksi.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user == null) {
                        setGraphic(null);
                        return;
                    }

                    Button btnDelete = new Button("🗑️");
                    btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; " +
                            "-fx-cursor: hand; -fx-font-size: 16px; -fx-background-radius: 6; " +
                            "-fx-padding: 4 10;");
                    btnDelete.setTooltip(new Tooltip("Hapus User"));
                    btnDelete.setOnAction(e -> handleDeleteUser(user));

                    setGraphic(btnDelete);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tableUsers.setFixedCellSize(70);
        tableUsers.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
    }

    private void loadUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            userList.clear();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));

                user.setNamaLengkap(rs.getString("full_name"));

                String dbRole = rs.getString("role");
                if ("admin".equals(dbRole)) {
                    user.setRole("Administrator");
                } else if ("staff".equals(dbRole)) {
                    user.setRole("User");
                } else {
                    user.setRole(dbRole);
                }

                int isActive = rs.getInt("is_active");
                user.setStatus(isActive == 1 ? "Aktif" : "Nonaktif");

                if (rs.getDate("created_at") != null) {
                    user.setCreatedAt(rs.getDate("created_at").toLocalDate());
                }
                userList.add(user);
            }

            tableUsers.setItems(userList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat data: " + e.getMessage());
        }
    }

    @FXML
    private void handleTambahUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_user.fxml"));
            Parent formRoot = loader.load();

            AddUserController controller = loader.getController();
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Tambah User Baru");
            stage.setScene(new Scene(formRoot, 600, 500));
            stage.showAndWait();

            loadUsers();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka form: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setContentText("Yakin ingin menghapus user: " + user.getNamaLengkap() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.connect()) {
                    String sql = "DELETE FROM users WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, user.getId());
                    pstmt.executeUpdate();

                    showAlert("Sukses", "User berhasil dihapus.");
                    loadUsers();
                } catch (Exception e) {
                    showAlert("Error", "Gagal menghapus: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserData(currentUserId, "Administrator Sistem", "ADMIN");

            if (dashboardController != null && dashboardController.getMainLayout() != null) {
                dashboardController.getMainLayout().setCenter(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambahDokumen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserData(currentUserId, "Administrator Sistem", "ADMIN");

            if (dashboardController != null && dashboardController.getMainLayout() != null) {
                dashboardController.getMainLayout().setCenter(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSemuaDokumen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/all_documents.fxml"));
            Parent root = loader.load();

            AllDocumentsController controller = loader.getController();
            controller.setUserData(currentUserId, "Administrator Sistem");
            controller.setDashboardController(dashboardController);

            if (dashboardController != null && dashboardController.getMainLayout() != null) {
                dashboardController.getMainLayout().setCenter(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) tableUsers.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}