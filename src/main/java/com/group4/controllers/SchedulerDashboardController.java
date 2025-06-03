package com.group4.controllers;

import com.group4.models.Issue;
import com.group4.models.IssueStatus;
import com.group4.models.User;
import java.util.stream.Collectors;
import com.group4.services.IssueService;
import com.group4.services.UserService;
import com.group4.utils.AlertUtils;
import com.group4.utils.SessionManager;
import com.group4.utils.ViewManager;
import com.group4.utils.ImageUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Scheduler Dashboard with tabbed interface.
 */
public class SchedulerDashboardController {
    private static final Logger logger = Logger.getLogger(SchedulerDashboardController.class.getName());

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab dashboardTab;

    @FXML
    private Tab reportsTab;

    @FXML
    private Tab issuesTab;

    @FXML
    private Button logoutButton;

    // Issues Tab Components
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

    private IssueService issueService;
    private UserService userService;
    private ObservableList<Issue> issuesList = FXCollections.observableArrayList();

    // Profile Section
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;

    // Profile Tab
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
    private Label profileEmailField;
    @FXML
    private Label profileContactField;
    @FXML
    private Label profileCreatedDate;

    private User currentUser;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Initialize services
        issueService = new IssueService();
        userService = new UserService();
        issuesList = FXCollections.observableArrayList();

        // Get current user from session
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Set up issues table
        setupIssuesTable();

        // Load user data immediately
        loadCurrentUserInfo();

        // Load initial data
        loadIssuesData();

        // Set up tab change listener
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == issuesTab) {
                // Refresh issues data when switching to issues tab
                loadIssuesData();
            }
        });
    }

    /**
     * Loads and displays the current user's information.
     */
    private void loadCurrentUserInfo() {
        if (currentUser != null) {
            Platform.runLater(() -> {
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
                if (profileCreatedDate != null) {
                    profileCreatedDate.setText(LocalDate.now().toString()); // TODO: Add created date to User model
                }

                // Load profile image
                loadProfileImage();
            });
        }
    }

    /**
     * Loads the user's profile image.
     */
    private void loadProfileImage() {
        try {
            // Get the image path from the user object
            String imagePath = (currentUser != null) ? currentUser.getProfilePicture() : null;

            // Load the image using ImageUtils
            Image image = ImageUtils.loadImage(imagePath);

            // Set the images on the UI thread
            Platform.runLater(() -> setProfileImages(image));

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading profile image", e);
            // Load default avatar on error
            Platform.runLater(() -> setProfileImages(ImageUtils.loadDefaultAvatar()));
        }
    }

    /**
     * Sets the profile images for both the small and large image views.
     * 
     * @param image The image to set
     */
    private void setProfileImages(Image image) {
        if (image == null) {
            image = ImageUtils.loadDefaultAvatar();
        }

        if (profileImageView != null) {
            profileImageView.setImage(image);
        }
        if (profileImageLarge != null) {
            profileImageLarge.setImage(image);
        }
    }

    /**
     * Handles the profile section click in the header.
     */
    @FXML
    private void handleProfileSectionClick() {
        if (mainTabPane != null) {
            // Find the profile tab by checking tab text
            for (Tab tab : mainTabPane.getTabs()) {
                if ("My Profile".equals(tab.getText())) {
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }
            logger.warning("Profile tab not found");
        } else {
            logger.warning("Error: mainTabPane is not initialized");
        }
    }

    /**
     * Handles the edit profile button click.
     */
    @FXML
    private void handleEditProfile() {
        try {
            // Store the current user data before showing the dialog
            User originalUser = new User();
            originalUser.setUserId(currentUser.getUserId());
            originalUser.setFirstName(currentUser.getFirstName());
            originalUser.setLastName(currentUser.getLastName());
            originalUser.setEmail(currentUser.getEmail());
            originalUser.setContactNumber(currentUser.getContactNumber());
            originalUser.setRole(currentUser.getRole());
            originalUser.setStatus(currentUser.getStatus());
            originalUser.setProfilePicture(currentUser.getProfilePicture());

            // Load the edit profile view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/EditProfile.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current user data
            EditProfileController controller = loader.getController();
            controller.setUserData(currentUser);

            Stage dialog = new Stage();
            dialog.setTitle("Edit Profile");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            // After dialog is closed, refresh the current user data
            User updatedUser = userService.getUserById(currentUser.getUserId());
            if (updatedUser != null) {
                // Check if any profile data has changed
                boolean profileChanged = !originalUser.equals(updatedUser);

                if (profileChanged) {
                    // Update the current user reference
                    currentUser = updatedUser;

                    // Update the session user if it's the same user
                    if (SessionManager.getInstance().getCurrentUser().getUserId().equals(currentUser.getUserId())) {
                        SessionManager.getInstance().setCurrentUser(currentUser);
                    }

                    // Force update the UI
                    Platform.runLater(() -> {
                        try {
                            // Update UI elements
                            loadCurrentUserInfo();

                            // Show success message
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Profile updated successfully!");
                            alert.showAndWait();
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error updating profile UI", e);
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Error updating profile");
                            alert.setContentText(e.getMessage());
                            alert.showAndWait();
                        }
                    });
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to load updated profile data");
                alert.showAndWait();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to open edit profile", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open edit profile: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear the current user session
            SessionManager.getInstance().logout();

            // Load the login screen using ViewManager
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/Login.fxml"));
            Parent root = loader.load();
            ViewManager.switchView(root);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to logout", e.getMessage());
        }
    }

    /**
     * Sets up the issues table with appropriate cell value factories and cell
     * factories.
     */
    private void setupIssuesTable() {
        // Set up the columns
        issueIdColumn.setCellValueFactory(new PropertyValueFactory<>("issueId"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        hallIdColumn.setCellValueFactory(new PropertyValueFactory<>("hallId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        assignedStaffIdColumn.setCellValueFactory(new PropertyValueFactory<>("assignedStaffId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Make status column editable with a combo box
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(IssueStatus.values()));
        statusColumn.setOnEditCommit(event -> {
            Issue issue = event.getRowValue();
            IssueStatus newStatus = event.getNewValue();
            issue.setStatus(newStatus);

            // Update the issue in the database
            Task<Void> updateTask = new Task<Void>() {
                private boolean updateSuccessful = false;

                @Override
                protected Void call() throws Exception {
                    try {
                        Task<Boolean> updateTask = issueService.updateIssue(issue);
                        updateTask.run(); // Run the task
                        updateSuccessful = updateTask.get(); // Wait for completion and get result
                        if (!updateSuccessful) {
                            throw new Exception("Failed to update issue status");
                        }
                    } catch (Exception e) {
                        throw new Exception("Error updating issue: " + e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> AlertUtils.showInfo("Success", "Issue status updated successfully."));
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        AlertUtils.showError("Error", "Failed to update issue status: " +
                                (getException() != null ? getException().getMessage() : "Unknown error"));
                        // Revert the change in the UI if update failed
                        issuesTable.refresh();
                    });
                }
            };

            new Thread(updateTask).start();
        });
    }

    /**
     * Loads issues data into the table, showing only issues assigned to the current
     * scheduler.
     */
    private void loadIssuesData() {
        if (currentUser == null) {
            AlertUtils.showError("Error", "No user logged in");
            return;
        }

        final String currentUserId = currentUser.getUserId();

        Task<List<Issue>> loadTask = new Task<List<Issue>>() {
            @Override
            protected List<Issue> call() throws Exception {
                Task<List<Issue>> task = issueService.getAllIssues();
                task.run();
                // Filter issues to only show those assigned to the current user
                return task.get().stream()
                        .filter(issue -> currentUserId.equals(issue.getAssignedStaffId()))
                        .collect(Collectors.toList());
            }

            @Override
            protected void succeeded() {
                List<Issue> issues = getValue();
                Platform.runLater(() -> {
                    issuesList.setAll(issues);
                    issuesTable.setItems(issuesList);

                    if (issues.isEmpty()) {
                        AlertUtils.showInfo("No Issues", "You don't have any assigned issues.");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> AlertUtils.showError("Error", "Failed to load issues: " +
                        (getException() != null ? getException().getMessage() : "Unknown error")));
            }
        };

        new Thread(loadTask).start();
    }

    @FXML
    private void handleRefreshIssues() {
        loadIssuesData();
    }

    @FXML
    private void handleAssignIssue() {
        Issue selectedIssue = issuesTable.getSelectionModel().getSelectedItem();
        if (selectedIssue == null) {
            AlertUtils.showWarning("No Selection", "Please select an issue to assign.");
            return;
        }

        // Get list of available staff
        Task<Void> getStaffTask = new Task<Void>() {
            private List<User> staffList;

            @Override
            protected Void call() throws Exception {
                staffList = userService.getUsersByRole("STAFF");
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> showStaffSelectionDialog(selectedIssue, staffList));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> AlertUtils.showError("Error", "Failed to load staff list: " +
                        (getException() != null ? getException().getMessage() : "Unknown error")));
            }
        };

        new Thread(getStaffTask).start();
    }

    @FXML
    private void handleCloseIssue() {
        Issue selectedIssue = issuesTable.getSelectionModel().getSelectedItem();
        if (selectedIssue == null) {
            AlertUtils.showWarning("No Selection", "Please select an issue to close.");
            return;
        }

        // Create confirmation dialog
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Close Issue");
        confirmationDialog.setHeaderText("Are you sure you want to close this issue?");
        confirmationDialog.setContentText("This action cannot be undone.");

        // Show the dialog and wait for user response
        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Update issue status to CLOSED
                selectedIssue.setStatus(IssueStatus.CLOSED);

                Task<Void> closeIssueTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            Task<Boolean> updateTask = issueService.updateIssue(selectedIssue);
                            updateTask.run(); // Run the task
                            boolean success = updateTask.get(); // Wait for completion and get result
                            if (!success) {
                                throw new Exception("Failed to update issue status");
                            }
                        } catch (Exception e) {
                            throw new Exception("Error updating issue: " + e.getMessage(), e);
                        }
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        Platform.runLater(() -> {
                            AlertUtils.showInfo("Success", "Issue has been closed successfully.");
                            loadIssuesData();
                        });
                    }

                    @Override
                    protected void failed() {
                        Platform.runLater(() -> AlertUtils.showError("Error", "Failed to close the issue: " +
                                (getException() != null ? getException().getMessage() : "Unknown error")));
                    }
                };

                new Thread(closeIssueTask).start();
            }
        });
    }

    /**
     * Shows a dialog to select staff for issue assignment.
     */
    private void showStaffSelectionDialog(Issue issue, List<User> staffList) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Assign Staff");
        dialog.setHeaderText("Select staff to assign to this issue");

        // Set the button types
        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        // Create the staff list view
        ListView<User> staffListView = new ListView<>();
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

        // Add staff to the list view
        staffListView.getItems().addAll(staffList);

        // Enable/disable assign button based on selection
        Node assignButton = dialog.getDialogPane().lookupButton(assignButtonType);
        assignButton.setDisable(true);

        staffListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            assignButton.setDisable(newVal == null);
        });

        dialog.getDialogPane().setContent(staffListView);

        // Set the result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return staffListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(selectedStaff -> {
            // Assign the selected staff to the issue
            issue.setAssignedStaffId(selectedStaff.getUserId());

            Task<Void> assignIssueTask = new Task<Void>() {
                private boolean updateSuccessful;

                @Override
                protected Void call() throws Exception {
                    try {
                        Task<Boolean> updateTask = issueService.updateIssue(issue);
                        updateTask.run(); // Run the task
                        updateSuccessful = updateTask.get(); // Wait for completion and get result
                        if (!updateSuccessful) {
                            throw new Exception("Failed to assign issue");
                        }
                    } catch (Exception e) {
                        throw new Exception("Error assigning issue: " + e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        AlertUtils.showInfo("Success", "Issue assigned successfully.");
                        loadIssuesData();
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> AlertUtils.showError("Error", "Failed to assign issue: " +
                            (getException() != null ? getException().getMessage() : "Unknown error")));
                }
            };

            new Thread(assignIssueTask).start();
        });
    }
}