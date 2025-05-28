package com.group4.controllers;

import com.group4.App;
import com.group4.utils.SessionManager;
import com.group4.utils.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

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
    private Button logoutButton;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Initialization code can go here
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/Login.fxml"));
            Parent root = loader.load();
            ViewManager.switchView(root);
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message if needed
        }
    }
}