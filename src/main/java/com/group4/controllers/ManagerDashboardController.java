package com.group4.controllers;

import com.group4.App;
import com.group4.models.Booking;
import com.group4.models.Issue;
import com.group4.models.IssueStatus;
import com.group4.models.User;
import com.group4.services.BookingService;
import com.group4.services.IssueService;
import com.group4.services.AdminService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the Manager Dashboard.
 */
public class ManagerDashboardController {

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

        // Setup sales chart
        setupSalesChart();

        // Setup issues table
        setupIssuesTable();

        // Load data
        loadSalesData();
        loadIssuesData();
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
     * Handles the logout button click.
     */
    @FXML
    private void handleLogout() {
        // Clear the session
        SessionManager.getInstance().setCurrentUser(null);

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
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