package com.group4.controllers;

import com.group4.App;
import com.group4.models.User;
import com.group4.services.AdminService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import com.group4.utils.ViewManager;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the Admin Dashboard.
 */
public class AdminDashboardController {

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> userIdColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> contactNumberColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button editUserButton;

    @FXML
    private Button blockUserButton;

    @FXML
    private Button refreshButton;

    @FXML
    private ComboBox<String> roleFilterComboBox;

    private AdminService adminService;
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        adminService = new AdminService();

        // Initialize table columns using direct property references
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty());
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        contactNumberColumn.setCellValueFactory(cellData -> cellData.getValue().contactNumberProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Initialize role filter
        roleFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Admin", "Manager", "Scheduler", "Customer"));
        roleFilterComboBox.setValue("All");

        // Add listener to the role filter
        roleFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                filterUsersByRole(newValue);
            }
        });

        // Load all users
        loadAllUsers();
    }

    /**
     * Loads all users from the database.
     */
    private void loadAllUsers() {
        TaskUtils.executeTaskWithProgress(
                adminService.getAllUsers(),
                users -> {
                    usersList.clear();
                    usersList.addAll(users);
                    filterUsersByRole(roleFilterComboBox.getValue());
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + error.getMessage()));
    }

    /**
     * Filters users by role.
     * 
     * @param role The role to filter by or "All" to show all users
     */
    private void filterUsersByRole(String role) {
        if (role == null || role.equals("All")) {
            usersTable.setItems(usersList);
            return;
        }

        ObservableList<User> filteredList = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getRole().equalsIgnoreCase(role)) {
                filteredList.add(user);
            }
        }

        usersTable.setItems(filteredList);
    }

    /**
     * Handles the add user button click.
     */
    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    /**
     * Handles the edit user button click.
     */
    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
            return;
        }

        showUserDialog(selectedUser);
    }

    /**
     * Handles the toggle user status (block/unblock) button click.
     */
    @FXML
    private void handleToggleUserStatus() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to block/unblock.");
            return;
        }

        String currentStatus = selectedUser.getStatus();
        String newStatus = currentStatus.equals("ACTIVE") ? "BLOCKED" : "ACTIVE";
        String actionText = currentStatus.equals("ACTIVE") ? "block" : "unblock";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Status Change");
        confirm.setHeaderText("Are you sure you want to " + actionText + " this user?");
        confirm.setContentText("User: " + selectedUser.getFirstName() + " " + selectedUser.getLastName() + " ("
                + selectedUser.getEmail() + ")");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            TaskUtils.executeTaskWithProgress(
                    adminService.updateUserStatus(selectedUser.getUserId(), newStatus),
                    success -> {
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User status updated successfully.");
                            loadAllUsers();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user status.");
                        }
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update user status: " + error.getMessage()));
        }
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        loadAllUsers();
    }

    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleLogout() {
        try {
            // Clear the current user session
            SessionManager.getInstance().logout();

            // Load the login screen using ViewManager
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
            Parent root = loader.load();
            ViewManager.switchView(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Logout Failed");
            alert.setContentText("An error occurred while trying to log out: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Shows the user dialog for adding or editing a user.
     * 
     * @param user The user to edit, or null to add a new user
     */
    private void showUserDialog(User user) {
        // Create the dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Add User" : "Edit User");
        dialog.setHeaderText(user == null ? "Create a new user" : "Edit user information");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField contactNumberField = new TextField();
        contactNumberField.setPromptText("Contact Number");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Admin", "Manager", "Scheduler", "Customer"));
        roleComboBox.setValue("Customer");

        // If editing a user, populate the fields
        if (user != null) {
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
            contactNumberField.setText(user.getContactNumber());
            roleComboBox.setValue(user.getRole());
        }

        // Add fields to the grid
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Contact Number:"), 0, 4);
        grid.add(contactNumberField, 1, 4);
        grid.add(new Label("Role:"), 0, 5);
        grid.add(roleComboBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a user when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate fields
                if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                        emailField.getText().isEmpty() || contactNumberField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "All fields must be filled in.");
                    return null;
                }

                if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
                    return null;
                }

                // Create or update the user
                User newUser = user == null ? new User() : user;
                newUser.setFirstName(firstNameField.getText());
                newUser.setLastName(lastNameField.getText());
                newUser.setEmail(emailField.getText());
                if (!passwordField.getText().isEmpty()) {
                    newUser.setPassword(passwordField.getText());
                }
                newUser.setContactNumber(contactNumberField.getText());
                newUser.setRole(roleComboBox.getValue());
                newUser.setStatus("ACTIVE");

                return newUser;
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(updatedUser -> {
            TaskUtils.executeTaskWithProgress(
                    user == null ? adminService.createUser(updatedUser) : adminService.updateUser(updatedUser),
                    success -> {
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Success",
                                    user == null ? "User created successfully." : "User updated successfully.");
                            loadAllUsers();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    user == null ? "Failed to create user." : "Failed to update user.");
                        }
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "Error",
                            (user == null ? "Failed to create user: " : "Failed to update user: ")
                                    + error.getMessage()));
        });
    }

    /**
     * Shows an alert dialog.
     * 
     * @param alertType the type of alert
     * @param title     the alert title
     * @param message   the alert message
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}