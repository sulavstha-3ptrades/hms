package com.group4.controllers;

import com.group4.models.Maintenance;
import com.group4.services.MaintenanceService;
import com.group4.utils.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import java.util.List;


public class MaintenanceTabController {
    @FXML private TableView<Maintenance> maintenanceTable;
    @FXML private TableColumn<Maintenance, String> hallIdColumn;
    @FXML private TableColumn<Maintenance, String> descriptionColumn;
    @FXML private TableColumn<Maintenance, LocalDateTime> startTimeColumn;
    @FXML private TableColumn<Maintenance, LocalDateTime> endTimeColumn;
    @FXML private TableColumn<Maintenance, Void> actionsColumn;

    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final ObservableList<Maintenance> maintenanceList = FXCollections.observableArrayList();
    // Primary stage reference (kept for potential future use)

    @FXML
    public void initialize() {
        try {
            setupTableColumns();
            loadMaintenanceData();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to initialize maintenance tab", e.getMessage());
        }
    }

    private void setupTableColumns() {
        hallIdColumn.setCellValueFactory(new PropertyValueFactory<>("hallId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        
        // Format date/time columns
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        // Update column types to use LocalDateTime
        startTimeColumn.setCellFactory(column -> new TableCell<Maintenance, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        
        // Update column types to use LocalDateTime
        endTimeColumn.setCellFactory(column -> new TableCell<Maintenance, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        
        // Add action buttons
        setupActionButtons();
    }
    
    private void setupActionButtons() {
        Callback<TableColumn<Maintenance, Void>, TableCell<Maintenance, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Maintenance, Void> call(final TableColumn<Maintenance, Void> param) {
                return new TableCell<Maintenance, Void>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    
                    {
                        editBtn.getStyleClass().add("edit-button");
                        deleteBtn.getStyleClass().add("delete-button");
                        
                        editBtn.setOnAction(event -> {
                            Maintenance maintenance = getTableView().getItems().get(getIndex());
                            handleEditMaintenance(maintenance);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Maintenance maintenance = getTableView().getItems().get(getIndex());
                            handleDeleteMaintenance(maintenance);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void handleEditMaintenance(Maintenance maintenance) {
        try {
            // Load the maintenance dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/MaintenanceDialog.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set up for editing
            MaintenanceDialogController controller = loader.getController();
            controller.setupForEdit(maintenance);
            
            // Create and show the dialog
            Stage dialog = new Stage();
            dialog.setTitle("Edit Maintenance");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root, 500, 400));
            
            // Refresh the table after the dialog is closed
            dialog.showAndWait();
            loadMaintenanceData();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open editor");
            alert.setContentText("An error occurred while trying to open the editor: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadMaintenanceData() {
        Task<List<Maintenance>> task = maintenanceService.getAllMaintenanceSchedules();
        task.setOnSucceeded(event -> {
            maintenanceList.setAll(task.getValue());
            maintenanceTable.setItems(maintenanceList);
        });
        task.setOnFailed(event -> {
            AlertUtils.showError("Error", "Failed to load maintenance data", 
                task.getException() != null ? task.getException().getMessage() : "Unknown error");
        });
        new Thread(task).start();
    }

    @FXML
    private void handleRefresh() {
        loadMaintenanceData();
    }

    @FXML
    private void handleDeleteMaintenance(Maintenance maintenance) {
        if (maintenance == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Maintenance");
        alert.setContentText("Are you sure you want to delete this maintenance schedule?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = maintenanceService.deleteMaintenance(maintenance.getMaintenanceId());
            task.setOnSucceeded(event -> {
                if (task.getValue()) {
                    maintenanceList.remove(maintenance);
                    Platform.runLater(() -> {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Maintenance schedule deleted successfully.");
                        successAlert.showAndWait();
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Failed to delete maintenance schedule.");
                        errorAlert.showAndWait();
                    });
                }
            });
            
            task.setOnFailed(event -> Platform.runLater(() -> {
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Deletion Failed");
                errorAlert.setContentText("An error occurred while deleting the maintenance schedule: " + 
                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                errorAlert.showAndWait();
            }));
            
            new Thread(task).start();
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        // Method kept for interface compatibility
        // No implementation needed as the stage is not used
    }
}
