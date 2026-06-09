package com.sikerma.sikerma.controller;

import com.sikerma.sikerma.config.DatabaseConfig;
import com.sikerma.sikerma.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserManagementController {

    @FXML private VBox formPanel;
    @FXML private Button btnTambahUser;
    @FXML private Label lblTotalUsers;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNamaLengkap;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private ComboBox<String> cbRole;

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
        initializeForm();
        loadUsers();
    }

    private void initializeForm() {
        cbRole.getItems().addAll("User", "Administrator");
        cbRole.setValue("User");

        formPanel.setVisible(false);
        formPanel.setManaged(false);
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

        setupTextColumn(colUsername);
        setupTextColumn(colEmail);
        setupTextColumn(colNamaLengkap);
        setupTextColumn(colDibuat);

        colRole.setCellFactory(column -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Administrator".equals(item)) {
                        badge.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #6b21a8; " +
                                "-fx-background-radius: 12; -fx-padding: 3 8; -fx-font-weight: bold; -fx-font-size: 10px;");
                    } else {
                        badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; " +
                                "-fx-background-radius: 12; -fx-padding: 3 8; -fx-font-weight: bold; -fx-font-size: 10px;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colStatus.setCellFactory(column -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if ("Aktif".equals(item)) {
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #047857; " +
                                "-fx-background-radius: 12; -fx-padding: 3 8; -fx-font-weight: bold; -fx-font-size: 10px;");
                    } else {
                        badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                                "-fx-background-radius: 12; -fx-padding: 3 8; -fx-font-weight: bold; -fx-font-size: 10px;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colAksi.setCellFactory(param -> new TableCell<>() {
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
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                            "-fx-cursor: hand; -fx-font-size: 14px;");
                    btnDelete.setTooltip(new Tooltip("Hapus User"));
                    btnDelete.setOnAction(e -> handleDeleteUser(user));

                    setGraphic(btnDelete);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tableUsers.setFixedCellSize(45);
        tableUsers.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-font-size: 11px;");
    }

    private void setupTextColumn(TableColumn<User, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-text-fill: #475569; -fx-padding: 3 8; -fx-font-size: 11px;");
            }
        });
    }

    @FXML
    private void handleTambahUser() {
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        handleResetForm();
        txtUsername.requestFocus();
    }

    @FXML
    private void handleBatalForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        handleResetForm();
    }

    @FXML
    private void handleResetForm() {
        txtUsername.clear();
        txtEmail.clear();
        txtNamaLengkap.clear();
        txtPassword.clear();
        if (txtPasswordVisible != null) {
            txtPasswordVisible.clear();
        }
        cbRole.setValue("User");
    }

    @FXML
    private void handleSaveUser() {
        if (txtUsername.getText().trim().isEmpty()) {
            showAlert("Validasi", "Username wajib diisi!", Alert.AlertType.WARNING);
            txtUsername.requestFocus();
            return;
        }

        if (txtEmail.getText().trim().isEmpty()) {
            showAlert("Validasi", "Email wajib diisi!", Alert.AlertType.WARNING);
            txtEmail.requestFocus();
            return;
        }

        if (txtNamaLengkap.getText().trim().isEmpty()) {
            showAlert("Validasi", "Nama Lengkap wajib diisi!", Alert.AlertType.WARNING);
            txtNamaLengkap.requestFocus();
            return;
        }

        if (txtPassword.getText().trim().isEmpty()) {
            showAlert("Validasi", "Password wajib diisi!", Alert.AlertType.WARNING);
            txtPassword.requestFocus();
            return;
        }

        try (Connection conn = DatabaseConfig.connect()) {
            String sql = "INSERT INTO users (username, email, full_name, password_hash, role, is_active, pic_blsdm, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtUsername.getText().trim());
            pstmt.setString(2, txtEmail.getText().trim());
            pstmt.setString(3, txtNamaLengkap.getText().trim());
            pstmt.setString(4, txtPassword.getText());

            String roleValue = cbRole.getValue();
            String dbRole = "Administrator".equals(roleValue) ? "admin" : "staff";
            pstmt.setString(5, dbRole);

            pstmt.setInt(6, 1);
            pstmt.setString(7, "");

            pstmt.executeUpdate();

            showAlert("Sukses", "User berhasil ditambahkan!", Alert.AlertType.INFORMATION);

            handleBatalForm();
            loadUsers();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menambah user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
                user.setPicBlsdm(rs.getString("pic_blsdm"));

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

            if (lblTotalUsers != null) {
                lblTotalUsers.setText("Total : " + userList.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus User");
        confirm.setContentText("Yakin ingin menghapus user: " + user.getNamaLengkap() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConfig.connect()) {
                    String sql = "DELETE FROM users WHERE id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, user.getId());
                    pstmt.executeUpdate();

                    showAlert("Sukses", "User berhasil dihapus.", Alert.AlertType.INFORMATION);
                    loadUsers();
                } catch (Exception e) {
                    showAlert("Error", "Gagal menghapus: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            javafx.scene.Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUserData(currentUserId, "Administrator Sistem", "admin");

            if (dashboardController != null && dashboardController.getMainLayout() != null) {
                dashboardController.getMainLayout().setCenter(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambahDokumen() {
        handleDashboard();
    }

    @FXML
    private void handleSemuaDokumen() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/all_documents.fxml"));
            javafx.scene.Parent root = loader.load();

            AllDocumentsController controller = loader.getController();
            controller.setUserData(currentUserId, "Administrator Sistem", "admin");
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
        javafx.stage.Stage stage = (javafx.stage.Stage) tableUsers.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}