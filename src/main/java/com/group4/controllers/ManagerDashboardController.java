package com.group4.controllers;

import com.group4.App;
import com.group4.models.Booking;
import com.group4.models.Issue;
import com.group4.models.IssueStatus;
import com.group4.models.User;
import com.group4.services.BookingService;
import com.group4.services.IssueService;
import com.group4.services.UserService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Modality;
import com.group4.utils.ViewManager;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.concurrent.Task;
import com.group4.utils.ImageUtils;

/**
 * Controller for the Manager Dashboard.
 */
public class ManagerDashboardController {
    // Profile Tab Components
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;

    @FXML
    private ImageView profileImageLarge;
    @FXML
    private Label profileFullName;
    @FXML
    private Label profileEmail;
    @FXML
    private Label profileRole;
    @FXML
    private Label profileContact;
    @FXML
    private Label profileUsername;
    @FXML
    private Label profileEmailField;
    @FXML
    private Label profileContactField;
    @FXML
    private Label profileCreatedDate;

    private User currentUser;
    private UserService userService;

    @FXML
    private Button logoutButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab salesTab;

    @FXML
    private Tab issuesTab;

    @FXML
    private BarChart<String, Number> salesChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private ComboBox<String> periodComboBox;

    @FXML
    private TableView<Issue> issuesTable;

    @FXML
    private TableColumn<Issue, String> issueIdColumn;

    @FXML
    private TableColumn<Issue, String> customerIdColumn;

    @FXML
    private TableColumn<Issue, String> hallIdColumn;

    @FXML
    private TableColumn<Issue, String> descriptionColumn;

    @FXML
    private TableColumn<Issue, String> assignedStaffIdColumn;

    @FXML
    private TableColumn<Issue, IssueStatus> statusColumn;

    @FXML
    private Button assignIssueButton;

    @FXML
    private Button closeIssueButton;

    @FXML
    private Button refreshIssuesButton;

    private BookingService bookingService;
    private IssueService issueService;
    private ObservableList<Issue> issuesList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Initialize services
        bookingService = new BookingService();
        issueService = new IssueService();
        userService = new UserService();

        // Get current user from session
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Setup UI components
        setupSalesChart();
        setupIssuesTable();
        loadCurrentUserInfo();

        // Load data
        loadSalesData();
        loadIssuesData();
    }

    /**
     * Loads and displays the current user's information.
     */
    private void loadCurrentUserInfo() {
        if (currentUser != null) {
            // Update header
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole());
            }

            // Update profile tab
            if (profileFullName != null) {
                profileFullName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (profileEmail != null) {
                profileEmail.setText(currentUser.getEmail());
                if (profileEmailField != null) {
                    profileEmailField.setText(currentUser.getEmail());
                }
            }
            if (profileRole != null) {
                profileRole.setText(currentUser.getRole());
            }
            if (profileContact != null && profileContactField != null) {
                String contact = currentUser.getContactNumber();
                profileContact.setText(contact != null ? contact : "Not provided");
                profileContactField.setText(contact != null ? contact : "Not provided");
            }
            if (profileUsername != null) {
                // Use email as username if username is not available
                profileUsername.setText(currentUser.getEmail());
            }
            if (profileCreatedDate != null) {
                profileCreatedDate.setText(LocalDate.now().toString()); // TODO: Add created date to User model
            }

            // Load profile image
            loadProfileImage();
        }
    }

    /**
     * Loads the user's profile image.
     */
    private void loadProfileImage() {
        try {
            // Force reload the current user data to get the latest profile picture
            if (currentUser != null) {
                currentUser = userService.getUserById(currentUser.getUserId());
            }

            // Get the image path from the user object
            String imagePath = (currentUser != null) ? currentUser.getProfilePicture() : null;
            System.out.println("Loading profile image from path: " + imagePath);

            // Clear the image cache first
            if (profileImageView != null) {
                profileImageView.setImage(null);
            }

            // Use ImageUtils to load the image (it will handle all path resolution)
            Image image = ImageUtils.loadImage(imagePath);

            // Set the images
            setProfileImages(image);

            // Force a UI refresh
            if (profileImageView != null) {
                profileImageView.setImage(image);
            }

        } catch (Exception e) {
            System.err.println("Error loading profile image: " + e.getMessage());
            e.printStackTrace();
            // Load default avatar on error
            Image defaultAvatar = ImageUtils.loadDefaultAvatar();
            setProfileImages(defaultAvatar);
        }
    }

    /**
     * Sets the profile images for both the small and large image views.
     * 
     * @param image The image to set
     */
    private void setProfileImages(Image image) {
        if (image == null) {
            return;
        }

        Platform.runLater(() -> {
            if (profileImageView != null) {
                profileImageView.setImage(image);
            }
            if (profileImageLarge != null) {
                profileImageLarge.setImage(image);
            }
        });
    }

    /**
     * Sets up the sales chart.
     */
    private void setupSalesChart() {
        // Setup period filter
        periodComboBox.setItems(FXCollections.observableArrayList(
                "Weekly", "Monthly", "Yearly"));
        periodComboBox.setValue("Monthly");

        // Add listener to period filter
        periodComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadSalesData();
            }
        });

        // Setup chart
        salesChart.setTitle("Sales Overview");
        xAxis.setLabel("Period");
        yAxis.setLabel("Revenue ($)");
    }

    /**
     * Sets up the issues table.
     */
    private void setupIssuesTable() {
        issueIdColumn.setCellValueFactory(cellData -> cellData.getValue().issueIdProperty());
        customerIdColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty());
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        // Set up the assigned staff column with a cell factory for editing
        assignedStaffIdColumn.setCellValueFactory(cellData -> cellData.getValue().assignedStaffIdProperty());
        assignedStaffIdColumn.setCellFactory(column -> new TableCell<Issue, String>() {
            private final ComboBox<User> userComboBox = new ComboBox<>();
            private final List<User> allSchedulers = new ArrayList<>();

            {
                // Load all users and filter for SCHEDULER role
                List<User> allUsers = userService.getAllUsers();
                for (User user : allUsers) {
                    if ("SCHEDULER".equalsIgnoreCase(user.getRole())) {
                        allSchedulers.add(user);
                    }
                }
                userComboBox.getItems().addAll(allSchedulers);

                // Set the display text for users in the combo box
                userComboBox.setCellFactory(lv -> new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        setText(empty || user == null ? "" : user.getFirstName() + " " + user.getLastName());
                    }
                });

                userComboBox.setButtonCell(new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        setText(empty || user == null ? "" : user.getFirstName() + " " + user.getLastName());
                    }
                });

                userComboBox.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null && userComboBox.getValue() != null) {
                        Issue issue = getTableRow().getItem();
                        User selectedUser = userComboBox.getValue();
                        String previousStaffId = issue.getAssignedStaffId();

                        // Show loading state
                        setText("Saving...");

                        // Update the issue with the new assigned staff
                        issue.setAssignedStaffId(selectedUser.getUserId());

                        // Save the updated issue to the backend
                        Task<Boolean> updateTask = issueService.updateIssue(issue);
                        updateTask.setOnSucceeded(e -> {
                            if (Boolean.TRUE.equals(updateTask.getValue())) {
                                // Update successful, refresh the table
                                Platform.runLater(() -> {
                                    updateItem(issue.getAssignedStaffId(), false);
                                    // Show success message with staff name
                                    showAlert(Alert.AlertType.INFORMATION, "Success",
                                            String.format("Successfully assigned issue to %s %s",
                                                    selectedUser.getFirstName(),
                                                    selectedUser.getLastName()));
                                    // Refresh the table to reflect changes
                                    loadIssuesData();
                                });
                            } else {
                                // Revert to previous state on failure
                                issue.setAssignedStaffId(previousStaffId);
                                Platform.runLater(() -> {
                                    updateItem(previousStaffId, false);
                                    showAlert(Alert.AlertType.ERROR, "Error",
                                            "Failed to update issue assignment. Please try again.");
                                });
                            }
                        });

                        updateTask.setOnFailed(e -> {
                            // Revert to previous state on error
                            issue.setAssignedStaffId(previousStaffId);
                            Platform.runLater(() -> {
                                updateItem(previousStaffId, false);
                                showAlert(Alert.AlertType.ERROR, "Error",
                                        String.format("Failed to assign issue: %s",
                                                updateTask.getException().getMessage()));
                            });
                        });

                        // Execute the task in a background thread
                        new Thread(updateTask).start();
                    }
                });
            }

            @Override
            protected void updateItem(String staffId, boolean empty) {
                super.updateItem(staffId, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Show the assigned user's name when not editing
                    if (staffId == null || staffId.trim().isEmpty()) {
                        setText("Unassigned");
                    } else {
                        User staff = userService.getUserById(staffId);
                        setText(staff != null ? staff.getFirstName() + " " + staff.getLastName()
                                : "Unknown (" + staffId + ")");
                    }

                    // Only show the combo box when the cell is being edited
                    if (isEditing()) {
                        if (staffId != null && !staffId.trim().isEmpty()) {
                            userComboBox.getSelectionModel().select(
                                    userComboBox.getItems().stream()
                                            .filter(u -> u.getUserId().equals(staffId))
                                            .findFirst()
                                            .orElse(null));
                        }
                        setText(null);
                        setGraphic(userComboBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                if (!isEmpty()) {
                    String staffId = getItem();
                    if (staffId != null && !staffId.trim().isEmpty()) {
                        userComboBox.getSelectionModel().select(
                                userComboBox.getItems().stream()
                                        .filter(u -> u.getUserId().equals(staffId))
                                        .findFirst()
                                        .orElse(null));
                    }
                    setText(null);
                    setGraphic(userComboBox);
                    userComboBox.requestFocus();
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateItem(getItem(), false);
            }
        });

        // Make the assigned staff column editable
        assignedStaffIdColumn.setEditable(true);

        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Make the table editable
        issuesTable.setEditable(true);

        // Make status column editable with a choice box
        statusColumn.setCellFactory(column -> new TableCell<Issue, IssueStatus>() {
            private final ComboBox<IssueStatus> comboBox = new ComboBox<>();

            {
                // Include all available statuses
                comboBox.getItems().addAll(
                        IssueStatus.OPEN,
                        IssueStatus.IN_PROGRESS,
                        IssueStatus.RESOLVED,
                        IssueStatus.CLOSED);

                // Set default value to OPEN
                comboBox.setValue(IssueStatus.OPEN);

                comboBox.setOnAction(event -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        Issue issue = getTableRow().getItem();
                        IssueStatus newStatus = comboBox.getValue();

                        // Only proceed if the status has actually changed
                        if (newStatus != issue.getStatus()) {
                            IssueStatus oldStatus = issue.getStatus();
                            issue.setStatus(newStatus);

                            // Save the updated issue status
                            Task<Boolean> updateTask = issueService.updateIssue(issue);
                            updateTask.setOnSucceeded(e -> {
                                if (Boolean.TRUE.equals(updateTask.getValue())) {
                                    Platform.runLater(() -> {
                                        updateItem(newStatus, false);
                                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                                String.format("Status updated from %s to %s successfully.",
                                                        oldStatus, newStatus));
                                        loadIssuesData();
                                    });
                                } else {
                                    // Revert to old status on failure
                                    issue.setStatus(oldStatus);
                                    Platform.runLater(() -> {
                                        updateItem(oldStatus, false);
                                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
                                    });
                                }
                            });
                            updateTask.setOnFailed(e -> {
                                // Revert to old status on error
                                issue.setStatus(oldStatus);
                                Platform.runLater(() -> {
                                    updateItem(oldStatus, false);
                                    showAlert(Alert.AlertType.ERROR, "Error",
                                            "Failed to update status: " + updateTask.getException().getMessage());
                                });
                            });

                            new Thread(updateTask).start();
                        } else {
                            // If status didn't change, just cancel the edit
                            cancelEdit();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(IssueStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // If the item is null (new issue), default to OPEN
                    if (item == null) {
                        item = IssueStatus.OPEN;
                    }
                    // Show the status text when not editing
                    setText(item.toString());

                    // Only show the combo box when the cell is being edited
                    if (isEditing()) {
                        comboBox.setValue(item);
                        setText(null);
                        setGraphic(comboBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                if (!isEmpty()) {
                    comboBox.setValue(getItem());
                    setText(null);
                    setGraphic(comboBox);
                    comboBox.requestFocus();
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem() == null ? "" : getItem().toString());
                setGraphic(null);
            }
        });

        // Make the status column editable
        statusColumn.setEditable(true);
    }

    /**
     * Loads sales data for the chart based on the selected period.
     */
    private void loadSalesData() {
        String period = periodComboBox.getValue();

        TaskUtils.executeTaskWithProgress(
                bookingService.getAllBookings(),
                bookings -> {
                    // Clear previous data
                    salesChart.getData().clear();

                    // Create the series
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Revenue");

                    // Process bookings based on the selected period
                    Map<String, Double> salesData = aggregateSalesData(bookings, period);

                    // Add data points
                    for (Map.Entry<String, Double> entry : salesData.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }

                    // Add the series to the chart
                    salesChart.getData().add(series);
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load sales data: " + error.getMessage()));
    }

    /**
     * Aggregates sales data based on the specified period.
     * 
     * @param bookings The list of bookings
     * @param period   The period to aggregate by (Weekly, Monthly, Yearly)
     * @return A map of period to revenue
     */
    private Map<String, Double> aggregateSalesData(List<Booking> bookings, String period) {
        Map<String, Double> salesData = new HashMap<>();
        DateTimeFormatter formatter;

        // Set the formatter based on the period
        switch (period) {
            case "Weekly":
                formatter = DateTimeFormatter.ofPattern("w-yyyy");
                break;
            case "Monthly":
                formatter = DateTimeFormatter.ofPattern("MMM-yyyy");
                break;
            case "Yearly":
                formatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("MMM-yyyy");
                break;
        }

        // Aggregate sales data
        for (Booking booking : bookings) {
            String periodKey = booking.getStartDateTime().format(formatter);
            Double currentRevenue = salesData.getOrDefault(periodKey, 0.0);
            salesData.put(periodKey, currentRevenue + booking.getTotalCost());
        }

        return salesData;
    }

    /**
     * Loads issues data for the table.
     */
    private void loadIssuesData() {
        TaskUtils.executeTaskWithProgress(
                issueService.getAllIssues(),
                issues -> {
                    issuesList.clear();
                    issuesList.addAll(issues);
                    issuesTable.setItems(issuesList);
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load issues: " + error.getMessage()));
    }

    /**
     * Handles the assign issue button click.
     */
    @FXML
    private void handleAssignIssue() {
        Issue selectedIssue = issuesTable.getSelectionModel().getSelectedItem();
        if (selectedIssue == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an issue to assign.");
            return;
        }

        try {
            // Load the assign issue dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/AssignIssueDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Get the controller and set the issue
            AssignIssueDialogController controller = loader.getController();
            controller.setIssue(selectedIssue);

            // Create and show the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Assign Issue");

            // Show the dialog and wait for user input
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                // Assign the issue to the selected scheduler
                User selectedScheduler = controller.getSelectedScheduler();
                if (selectedScheduler != null) {
                    TaskUtils.executeTaskWithProgress(
                            issueService.assignIssue(selectedIssue.getIssueId(), selectedScheduler.getUserId()),
                            success -> {
                                if (success) {
                                    Platform.runLater(() -> {
                                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                                String.format("Issue assigned to %s %s",
                                                        selectedScheduler.getFirstName(),
                                                        selectedScheduler.getLastName()));
                                        loadIssuesData();
                                    });
                                } else {
                                    Platform.runLater(
                                            () -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign issue."));
                                }
                            },
                            error -> Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error",
                                    "Failed to assign issue: " + error.getMessage())));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load assign issue dialog: " + e.getMessage());
        }
    }

    /**
     * // Create the staff list view
     * ListView<User> staffListView = new ListView<>(staffList);
     * staffListView.setCellFactory(param -> new ListCell<User>() {
     * 
     * @Override
     *           protected void updateItem(User user, boolean empty) {
     *           super.updateItem(user, empty);
     *           if (empty || user == null) {
     *           setText(null);
     *           } else {
     *           setText(user.getFirstName() + " " + user.getLastName() + " (" +
     *           user.getRole() + ")");
     *           }
     *           }
     *           });
     * 
     *           dialog.getDialogPane().setContent(staffListView);
     * 
     *           // Convert the result to a user when the assign button is clicked
     *           dialog.setResultConverter(dialogButton -> {
     *           if (dialogButton == assignButtonType) {
     *           return staffListView.getSelectionModel().getSelectedItem();
     *           }
     *           return null;
     *           });
     * 
     *           // Show the dialog and return the result
     *           Optional<User> result = dialog.showAndWait();
     *           return result.orElse(null);
     *           }
     * 
     *           /**
     *           Handles the close issue button click.
     */
    @FXML
    private void handleCloseIssue() {
        Issue selectedIssue = issuesTable.getSelectionModel().getSelectedItem();
        if (selectedIssue == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an issue to close.");
            return;
        }

        // Confirm closing the issue
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Close Issue");
        confirm.setHeaderText("Are you sure you want to close this issue?");
        confirm.setContentText("Issue ID: " + selectedIssue.getIssueId());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            TaskUtils.executeTaskWithProgress(
                    issueService.updateIssueStatus(selectedIssue.getIssueId(), IssueStatus.CLOSED),
                    success -> {
                        if (success) {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Issue closed successfully.");
                                loadIssuesData();
                            });
                        } else {
                            Platform.runLater(
                                    () -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to close issue."));
                        }
                    },
                    error -> Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to close issue: " + error.getMessage())));
        }
    }

    /**
     * Handles the refresh issues button click.
     */
    @FXML
    private void handleRefreshIssues() {
        loadIssuesData();
    }

    /**
     * Handles the edit profile button click.
     * Opens the edit profile dialog.
     */
    @FXML
    private void handleEditProfile() {
        try {
            // Try to load the FXML file
            String fxmlPath = "/com/group4/view/EditProfile.fxml";
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Could not find FXML file at " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Get the controller and pass the current user data
            EditProfileController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Failed to initialize EditProfileController");
            }
            controller.setUserData(currentUser);

            // Create the scene
            Scene scene = new Scene(root);

            // Try to load the CSS file (but don't fail if it's not found)
            String cssPath = "/com/group4/css/styles.css";
            try {
                URL cssUrl = getClass().getResource(cssPath);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("Warning: Could not find CSS file at " + cssPath);
                }
            } catch (Exception e) {
                System.err.println("Warning: Error loading CSS file: " + e.getMessage());
            }

            // Show the edit profile dialog
            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh profile data after editing
            currentUser = SessionManager.getInstance().getCurrentUser();
            loadCurrentUserInfo();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to open edit profile: " + e.getMessage() +
                            "\nPlease contact support if the problem persists.");
        }
    }

    /**
     * Handles the click event on the profile section in the header.
     * Navigates to the profile tab.
     */
    @FXML
    private void handleProfileSectionClick() {
        if (tabPane != null) {
            // Find the profile tab by checking tab text
            for (Tab tab : tabPane.getTabs()) {
                if ("My Profile".equals(tab.getText())) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }
            System.err.println("Profile tab not found");
        } else {
            System.err.println("Error: tabPane is not initialized");
        }
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login screen: " + e.getMessage());
        }
    }

    /**
     * Shows an alert dialog.
     * 
     * @param alertType the type of alert
     * @param title     the alert title
     * @param message   the alert message
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Set the owner to ensure the dialog appears in front
            if (issuesTable != null && issuesTable.getScene() != null) {
                Stage stage = (Stage) issuesTable.getScene().getWindow();
                alert.initOwner(stage);

                // Apply the scene's stylesheets to the dialog
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().addAll(stage.getScene().getStylesheets());
                dialogPane.getStyleClass().add("dialog-pane");

                // Apply specific styles based on alert type
                switch (alertType) {
                    case INFORMATION:
                        dialogPane.getStyleClass().add("info-dialog");
                        break;
                    case WARNING:
                        dialogPane.getStyleClass().add("warning-dialog");
                        break;
                    case ERROR:
                        dialogPane.getStyleClass().add("error-dialog");
                        break;
                    case CONFIRMATION:
                        dialogPane.getStyleClass().add("confirm-dialog");
                        break;
                    case NONE:
                    default:
                        dialogPane.getStyleClass().add("info-dialog");
                        break;
                }

                // Style buttons
                for (ButtonType bt : dialogPane.getButtonTypes()) {
                    Node button = dialogPane.lookupButton(bt);
                    if (button instanceof Button) {
                        button.getStyleClass().add("dialog-button");
                    }
                }

                // Set minimum width for better readability
                dialogPane.setMinWidth(400);
            }

            // Make sure the dialog is always on top
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
    }
}