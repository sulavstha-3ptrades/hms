package com.group4.controllers;

import com.group4.models.Hall;
import com.group4.models.Issue;
import com.group4.models.User;
import com.group4.services.HallService;
import com.group4.services.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;

import java.util.List;

public class AssignIssueDialogController {
    @FXML private Label issueIdLabel;
    @FXML private Label hallIdLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private Label hallNameLabel;
    @FXML private Label locationLabel;
    @FXML private ComboBox<User> schedulerComboBox;
    @FXML private DialogPane dialogPane; // Inject the dialog pane directly
    private final UserService userService = new UserService();
    private final HallService hallService = new HallService();

    @FXML
    public void initialize() {
        // Set up cell factory to display user names in the combo box
        schedulerComboBox.setCellFactory(param -> new UserListCell());
        schedulerComboBox.setButtonCell(new UserListCell());
        
        // Ensure dialogPane is initialized before using it
        Platform.runLater(() -> {
            // Enable/disable assign button based on selection
            schedulerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (dialogPane != null) {
                    Button assignButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                    if (assignButton != null) {
                        assignButton.setDisable(newVal == null);
                    }
                }
            });
            
            // Initially disable the OK button until a scheduler is selected
            if (dialogPane != null) {
                Button assignButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                if (assignButton != null) {
                    assignButton.setDisable(true);
                }
            }
        });
        
        // Load scheduler users
        loadSchedulerUsers();
    }

    public void setIssue(Issue issue) {
        if (issue != null) {
            issueIdLabel.setText(issue.getIssueId());
            hallIdLabel.setText(issue.getHallId());
            descriptionTextArea.setText(issue.getDescription());
            
            // Load hall information
            Task<Hall> getHallTask = hallService.getHallById(issue.getHallId());
            
            getHallTask.setOnSucceeded(event -> {
                Hall hall = getHallTask.getValue();
                if (hall != null) {
                    Platform.runLater(() -> {
                        // Display hall type and capacity as name and location
                        hallNameLabel.setText(hall.getType().toString());
                        locationLabel.setText("Capacity: " + hall.getCapacity());
                    });
                }
            });
            
            getHallTask.setOnFailed(event -> {
                showError("Failed to load hall information: " + 
                    (getHallTask.getException() != null ? 
                     getHallTask.getException().getMessage() : "Unknown error"));
            });
            
            // Load scheduler users and pre-select the currently assigned staff if any
            Task<List<User>> loadUsersTask = new Task<List<User>>() {
                @Override
                protected List<User> call() throws Exception {
                    return userService.getAllUsers();
                }
            };
            
            loadUsersTask.setOnSucceeded(e -> {
                List<User> users = loadUsersTask.getValue();
                if (users != null) {
                    ObservableList<User> schedulerUsers = FXCollections.observableArrayList();
                    final User[] currentlyAssigned = { null };
                    
                    // Filter for scheduler users and find currently assigned staff
                    for (User user : users) {
                        if (user != null && "SCHEDULER".equalsIgnoreCase(user.getRole()) 
                                && "ACTIVE".equals(user.getStatus())) {
                            schedulerUsers.add(user);
                            // Check if this is the currently assigned staff
                            if (user.getUserId().equals(issue.getAssignedStaffId())) {
                                currentlyAssigned[0] = user;
                            }
                        }
                    }
                    
                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        schedulerComboBox.setItems(schedulerUsers);
                        // Select the currently assigned staff if any
                        if (currentlyAssigned[0] != null) {
                            schedulerComboBox.getSelectionModel().select(currentlyAssigned[0]);
                        }
                        
                        // Enable/disable assign button based on selection
                        Button assignButton = (Button) dialogPane.lookupButton(ButtonType.APPLY);
                        if (assignButton != null) {
                            assignButton.setDisable(currentlyAssigned[0] == null);
                        }
                    });
                }
            });
            
            loadUsersTask.setOnFailed(e -> {
                Throwable exception = loadUsersTask.getException();
                Platform.runLater(() -> {
                    if (exception != null) {
                        showError("Error loading scheduler users: " + exception.getMessage());
                    } else {
                        showError("Unknown error loading scheduler users");
                    }
                });
            });
            
            // Start both tasks
            new Thread(getHallTask).start();
            new Thread(loadUsersTask).start();
        }
    }

    public User getSelectedScheduler() {
        return schedulerComboBox.getSelectionModel().getSelectedItem();
    }

    private void loadSchedulerUsers() {
        // Create a task to load users
        Task<List<User>> loadUsersTask = new Task<List<User>>() {
            @Override
            protected List<User> call() throws Exception {
                return userService.getAllUsers();
            }
        };
        
        // Set up success handler
        loadUsersTask.setOnSucceeded(event -> {
            List<User> users = loadUsersTask.getValue();
            if (users != null) {
                ObservableList<User> schedulerUsers = FXCollections.observableArrayList();
                for (User user : users) {
                    if (user != null && "SCHEDULER".equalsIgnoreCase(user.getRole()) 
                            && "ACTIVE".equals(user.getStatus())) {
                        schedulerUsers.add(user);
                    }
                }
                Platform.runLater(() -> schedulerComboBox.setItems(schedulerUsers));
            }
        });
        
        // Set up error handler
        loadUsersTask.setOnFailed(event -> {
            Throwable exception = loadUsersTask.getException();
            Platform.runLater(() -> {
                if (exception != null) {
                    showError("Error loading scheduler users: " + exception.getMessage());
                } else {
                    showError("Unknown error loading scheduler users");
                }
            });
        });
        
        // Start the task in a new thread
        new Thread(loadUsersTask).start();
    }

    @FXML
    private void handleAssign() {
        User selectedScheduler = schedulerComboBox.getSelectionModel().getSelectedItem();
        if (selectedScheduler != null) {
            // Close the dialog with OK result
            if (dialogPane != null && dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
                dialogPane.getScene().getWindow().hide();
            }
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Custom ListCell for displaying user names in the ComboBox
    private static class UserListCell extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            if (empty || user == null) {
                setText(null);
            } else {
                setText(String.format("%s %s (%s)", user.getFirstName(), user.getLastName(), user.getEmail()));
            }
        }
        
        @Override
        public String toString() {
            return getItem() == null ? "" : getItem().getFirstName() + " " + getItem().getLastName();
        }
    }
}
