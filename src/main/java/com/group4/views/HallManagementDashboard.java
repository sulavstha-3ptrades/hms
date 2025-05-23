package com.group4.views;

import com.group4.models.NavigationItem;
import com.group4.models.UserRole;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

import com.group4.views.components.*;

/**
 * Main dashboard view for the Hall Management System
 */
public class HallManagementDashboard extends DashboardLayout {

    private final StyledTable<BookingEntry> bookingsTable;
    private final StyledSidebar sidebar;
    private UserRole currentRole = UserRole.ADMIN; // Default role for testing

    /**
     * Creates a new hall management dashboard
     */
    public HallManagementDashboard() {
        super("Hall Management System");

        // Create sidebar
        sidebar = new StyledSidebar();

        // Add navigation items with role-based access
        sidebar.addItem(new NavigationItem("dashboard", "Dashboard", "📊", createDashboardView(),
                UserRole.ADMIN, UserRole.MANAGER, UserRole.STAFF, UserRole.USER));

        sidebar.addItem(new NavigationItem("bookings", "Bookings", "📅", createBookingsView(),
                UserRole.ADMIN, UserRole.MANAGER, UserRole.STAFF));

        sidebar.addItem(new NavigationItem("halls", "Halls", "🏛️", createHallsView(),
                UserRole.ADMIN, UserRole.MANAGER));

        sidebar.addItem(new NavigationItem("users", "Users", "👥", createUsersView(),
                UserRole.ADMIN));

        sidebar.addItem(new NavigationItem("reports", "Reports", "📈", createReportsView(),
                UserRole.ADMIN, UserRole.MANAGER));

        // Set current role and handle navigation
        sidebar.setUserRole(currentRole);
        sidebar.setSelectedItem("dashboard");
        sidebar.setOnItemSelected(item -> setContent(item.getView()));

        // Add header actions
        StyledButton newBookingButton = new StyledButton("New Booking");
        newBookingButton.setOnAction(e -> showBookingDialog());

        StyledButton settingsButton = new StyledButton("Settings", StyledButton.ButtonType.SECONDARY);
        settingsButton.setOnAction(e -> showSettings());

        addHeaderAction(settingsButton);
        addHeaderAction(newBookingButton);

        // Create bookings table
        bookingsTable = new StyledTable<>();

        TableColumn<BookingEntry, String> hallColumn = new TableColumn<>("Hall");
        hallColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().hall));

        TableColumn<BookingEntry, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().date.toString()));

        TableColumn<BookingEntry, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().customer));

        TableColumn<BookingEntry, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        bookingsTable.addColumn(hallColumn);
        bookingsTable.addColumn(dateColumn);
        bookingsTable.addColumn(customerColumn);
        bookingsTable.addColumn(statusColumn);

        // Add sample data
        bookingsTable.getItems().addAll(
                new BookingEntry("Main Hall", LocalDate.now(), "John Doe", "Confirmed"),
                new BookingEntry("Conference Room A", LocalDate.now().plusDays(1), "Jane Smith", "Pending"),
                new BookingEntry("Auditorium", LocalDate.now().plusDays(2), "Bob Wilson", "Confirmed"));

        // Set initial content
        setContent(createDashboardView());
    }

    private VBox createDashboardView() {
        VBox view = new VBox(16);

        // Add statistics cards
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        statsGrid.add(createStatsCard("Total Halls", "12", "+2 this month"), 0, 0);
        statsGrid.add(createStatsCard("Active Bookings", "48", "12 today"), 1, 0);
        statsGrid.add(createStatsCard("Available Halls", "5", "Next 24h"), 2, 0);
        statsGrid.add(createStatsCard("Revenue", "$5,240", "+12% vs last month"), 3, 0);

        // Create recent bookings card
        StyledCard recentBookingsCard = new StyledCard("Recent Bookings");
        recentBookingsCard.setContent(bookingsTable);
        VBox.setVgrow(recentBookingsCard, Priority.ALWAYS);

        view.getChildren().addAll(statsGrid, recentBookingsCard);
        return view;
    }

    private VBox createBookingsView() {
        VBox view = new VBox(16);
        view.getChildren().add(new Label("Bookings View - Coming Soon"));
        return view;
    }

    private VBox createHallsView() {
        VBox view = new VBox(16);
        view.getChildren().add(new Label("Halls View - Coming Soon"));
        return view;
    }

    private VBox createUsersView() {
        VBox view = new VBox(16);
        view.getChildren().add(new Label("Users View - Coming Soon"));
        return view;
    }

    private VBox createReportsView() {
        VBox view = new VBox(16);
        view.getChildren().add(new Label("Reports View - Coming Soon"));
        return view;
    }

    private StyledCard createStatsCard(String title, String value, String subtitle) {
        StyledCard card = new StyledCard("");

        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 14px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 12px;");

        content.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        card.setContent(content);

        return card;
    }

    private void showBookingDialog() {
        StyledDialog dialog = new StyledDialog(getScene().getWindow(), "New Booking");
        dialog.setContent(new HallBookingForm());

        StyledButton saveButton = new StyledButton("Save");
        saveButton.setOnAction(e -> {
            dialog.hide();
            getToastNotification().showSuccess("Booking created successfully!");
        });

        StyledButton cancelButton = new StyledButton("Cancel", StyledButton.ButtonType.SECONDARY);
        cancelButton.setOnAction(e -> dialog.hide());

        dialog.addButton(cancelButton);
        dialog.addButton(saveButton);

        dialog.showAndWait();
    }

    private void showSettings() {
        StyledDialog dialog = new StyledDialog(getScene().getWindow(), "Settings");

        VBox content = new VBox(16);
        content.setPadding(new Insets(16));

        // Add profile section
        HBox profileSection = new HBox(16);
        profileSection.setAlignment(Pos.CENTER_LEFT);

        StyledAvatar avatar = new StyledAvatar(64);
        avatar.setInitials("AD");

        VBox profileInfo = new VBox(4);
        profileInfo.getChildren().addAll(
                new Label("Admin User"),
                new Label("admin@example.com"));

        profileSection.getChildren().addAll(avatar, profileInfo);

        // Add settings form
        ImageUploadField logoUpload = new ImageUploadField();
        logoUpload.setFieldEnabled(true);

        content.getChildren().addAll(
                profileSection,
                new Label("System Logo"),
                logoUpload);

        dialog.setContent(content);

        StyledButton closeButton = new StyledButton("Close");
        closeButton.setOnAction(e -> dialog.hide());

        dialog.addButton(closeButton);
        dialog.showAndWait();
    }

    /**
     * Sample booking entry class
     */
    private static class BookingEntry {
        private final String hall;
        private final LocalDate date;
        private final String customer;
        private final String status;

        public BookingEntry(String hall, LocalDate date, String customer, String status) {
            this.hall = hall;
            this.date = date;
            this.customer = customer;
            this.status = status;
        }
    }
}