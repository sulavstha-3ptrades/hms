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

/**
 * Controller for the Scheduler Dashboard with tabbed interface.
 */
public class SchedulerDashboardController {

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
    private Label profileFirstName;
    @FXML
    private Label profileLastName;
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
        
        // Initialize user data
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            // Set up issues table
            setupIssuesTable();
            
            // Load initial data
            loadIssuesData();
            
            // Set up tab change listener
            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == issuesTab) {
                    // Refresh issues data when switching to issues tab
                    loadIssuesData();
                }
            });
            // Set welcome message
            userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            userRoleLabel.setText(currentUser.getRole());
            
            // Set profile tab data
            profileFullName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            profileEmail.setText(currentUser.getEmail());
            profileRole.setText(currentUser.getRole());
            
            // Set profile image if available
            if (currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
                try {
                    // If it's a base64 encoded image
                    if (currentUser.getProfilePicture().startsWith("data:image")) {
                        String base64Image = currentUser.getProfilePicture().split(",")[1];
                        byte[] imageData = Base64.getDecoder().decode(base64Image);
                        Image image = new Image(new ByteArrayInputStream(imageData));
                        profileImageView.setImage(image);
                        profileImageLarge.setImage(image);
                    } else {
                        // If it's a file path
                        Image image = new Image(currentUser.getProfilePicture());
                        profileImageView.setImage(image);
                        profileImageLarge.setImage(image);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image: " + e.getMessage());
                    // Set default avatar on error
                    Image defaultImage = new Image("/com/group4/assets/images/users/default-avatar.jpg");
                    profileImageView.setImage(defaultImage);
                    profileImageLarge.setImage(defaultImage);
                }
            } else {
                // Load default avatar if no profile picture is set
                Image defaultImage = new Image("/com/group4/assets/images/users/default-avatar.jpg");
                profileImageView.setImage(defaultImage);
                profileImageLarge.setImage(defaultImage);
            }
        }
    }

    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleProfileSectionClick() {
        try {
            // Switch to the profile tab when clicking on the profile section
            if (mainTabPane != null) {
                mainTabPane.getSelectionModel().select(mainTabPane.getTabs().size() - 1); // Select profile tab (last tab)
            } else {
                System.err.println("Error: mainTabPane is not initialized");
            }
        } catch (Exception e) {
            System.err.println("Error switching to profile tab: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/EditProfile.fxml"));
            Parent root = loader.load();
            
            EditProfileController controller = loader.getController();
            controller.setUserData(currentUser);
            
            Stage dialog = new Stage();
            dialog.setTitle("Edit Profile");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            
            // Reload current user data after editing
            currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                userRoleLabel.setText(currentUser.getRole());
                profileFullName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                profileEmail.setText(currentUser.getEmail());
                profileRole.setText(currentUser.getRole());
                
                // Reload profile image
                if (currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
                    try {
                        if (currentUser.getProfilePicture().startsWith("data:image")) {
                            String base64Image = currentUser.getProfilePicture().split(",")[1];
                            byte[] imageData = Base64.getDecoder().decode(base64Image);
                            Image image = new Image(new ByteArrayInputStream(imageData));
                            profileImageView.setImage(image);
                            profileImageLarge.setImage(image);
                        } else {
                            Image image = new Image(currentUser.getProfilePicture());
                            profileImageView.setImage(image);
                            profileImageLarge.setImage(image);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error reloading profile image: " + ex.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load edit profile form", e.getMessage());
        }
    }
    
    // Removed unused field
    
    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) profileImageLarge.getScene().getWindow();
        File selectedImageFile = fileChooser.showOpenDialog(stage);
        
        if (selectedImageFile != null) {
            try {
                // Load and resize the image
                BufferedImage bufferedImage = ImageIO.read(selectedImageFile);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                
                // Update both image views
                profileImageView.setImage(image);
                profileImageLarge.setImage(image);
                
                // Save the image to the user's profile
                saveProfileImage(selectedImageFile);
                
            } catch (IOException e) {
                AlertUtils.showError("Image Error", "Failed to load image", e.getMessage());
            }
        }
    }
    
    private void saveProfileImage(File imageFile) {
        try {
            // Convert image to base64
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            // Update user's profile picture
            currentUser.setProfilePicture("data:image/png;base64," + base64Image);
            userService.updateUser(currentUser);
            
            // Update session
            SessionManager.getInstance().setCurrentUser(currentUser);
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to save profile image", e.getMessage());
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
     * Sets up the issues table with appropriate cell value factories and cell factories.
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
                    Platform.runLater(() -> 
                        AlertUtils.showInfo("Success", "Issue status updated successfully."));
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        AlertUtils.showError("Error", "Failed to update issue status: " + 
                            (getException() != null ? 
                             getException().getMessage() : "Unknown error"));
                        // Revert the change in the UI if update failed
                        issuesTable.refresh();
                    });
                }
            };

            new Thread(updateTask).start();
        });
    }
    
    /**
     * Loads issues data into the table, showing only issues assigned to the current scheduler.
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
                Platform.runLater(() -> 
                    AlertUtils.showError("Error", "Failed to load issues: " + 
                        (getException() != null ? 
                         getException().getMessage() : "Unknown error")));
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
                Platform.runLater(() -> 
                    AlertUtils.showError("Error", "Failed to load staff list: " + 
                        (getException() != null ? 
                         getException().getMessage() : "Unknown error")));
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
                        Platform.runLater(() -> 
                            AlertUtils.showError("Error", "Failed to close the issue: " + 
                                (getException() != null ? 
                                 getException().getMessage() : "Unknown error")));
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
                    Platform.runLater(() -> 
                        AlertUtils.showError("Error", "Failed to assign issue: " + 
                            (getException() != null ? 
                             getException().getMessage() : "Unknown error")));
                }
            };

            new Thread(assignIssueTask).start();
        });
    }
}