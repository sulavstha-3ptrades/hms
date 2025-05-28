package com.group4.controllers;

import com.group4.App;
import com.group4.models.Booking;
import com.group4.models.Issue;
import com.group4.models.IssueStatus;
import com.group4.models.User;
import com.group4.services.BookingService;
import com.group4.services.IssueService;
import com.group4.services.AdminService;
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
import com.group4.utils.ViewManager;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import com.group4.utils.ImageUtils;

/**
 * Controller for the Manager Dashboard.
 */
public class ManagerDashboardController {
    // Profile Tab Components
    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    
    @FXML private ImageView profileImageLarge;
    @FXML private Label profileFullName;
    @FXML private Label profileEmail;
    @FXML private Label profileRole;
    @FXML private Label profileContact;
    @FXML private Label profileUsername;
    @FXML private Label profileEmailField;
    @FXML private Label profileContactField;
    @FXML private Label profileCreatedDate;
    
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
    private AdminService adminService;
    private ObservableList<Issue> issuesList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        bookingService = new BookingService();
        issueService = new IssueService();
        adminService = new AdminService();
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
        // Initialize table columns using direct property references
        issueIdColumn.setCellValueFactory(cellData -> cellData.getValue().issueIdProperty());
        customerIdColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty());
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        assignedStaffIdColumn.setCellValueFactory(cellData -> cellData.getValue().assignedStaffIdProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
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

        // Get staff members
        TaskUtils.executeTaskWithProgress(
                adminService.getAllUsers(),
                users -> {
                    // Filter for staff (non-customer roles)
                    ObservableList<User> staffList = FXCollections.observableArrayList();
                    for (User user : users) {
                        if (!user.getRole().equalsIgnoreCase("CUSTOMER") && user.getStatus().equals("ACTIVE")) {
                            staffList.add(user);
                        }
                    }

                    // Show staff selection dialog
                    User selectedStaff = showStaffSelectionDialog(staffList);
                    if (selectedStaff != null) {
                        // Assign the issue
                        TaskUtils.executeTaskWithProgress(
                                issueService.assignIssue(selectedIssue.getIssueId(), selectedStaff.getUserId()),
                                success -> {
                                    if (success) {
                                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                                "Issue assigned successfully.");
                                        loadIssuesData();
                                    } else {
                                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign issue.");
                                    }
                                },
                                error -> showAlert(Alert.AlertType.ERROR, "Error",
                                        "Failed to assign issue: " + error.getMessage()));
                    }
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load staff: " + error.getMessage()));
    }

    /**
     * Shows a dialog for selecting a staff member.
     * 
     * @param staffList The list of staff members
     * @return The selected staff member, or null if cancelled
     */
    private User showStaffSelectionDialog(ObservableList<User> staffList) {
        // Create the dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Assign Issue");
        dialog.setHeaderText("Select a staff member to assign this issue to:");

        // Set the button types
        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        // Create the staff list view
        ListView<User> staffListView = new ListView<>(staffList);
        staffListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getFirstName() + " " + user.getLastName() + " (" + user.getRole() + ")");
                }
            }
        });

        dialog.getDialogPane().setContent(staffListView);

        // Convert the result to a user when the assign button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return staffListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        // Show the dialog and return the result
        Optional<User> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Handles the close issue button click.
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
                    issueService.updateIssueStatus(selectedIssue.getIssueId(), IssueStatus.RESOLVED),
                    success -> {
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Issue closed successfully.");
                            loadIssuesData();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to close issue.");
                        }
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to close issue: " + error.getMessage()));
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
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}