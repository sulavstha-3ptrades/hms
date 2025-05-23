package com.group4.views.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.group4.models.NavigationItem;
import com.group4.models.UserRole;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A styled sidebar component for navigation with role-based access
 */
public class StyledSidebar extends VBox {

    private final List<NavigationItem> items;
    private final VBox navContainer;
    private UserRole currentRole;
    private String selectedId;
    private Consumer<NavigationItem> onItemSelected;

    /**
     * Creates a new styled sidebar
     */
    public StyledSidebar() {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Initialize
        items = new ArrayList<>();
        currentRole = UserRole.USER; // Default role

        // Configure layout
        setPrefWidth(250);
        setStyle("-fx-background-color: #f5f5f5;");

        // Create header
        Label headerLabel = new Label("Navigation");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        headerLabel.setPadding(new Insets(16));
        headerLabel.setStyle("-fx-text-fill: #757575;");

        // Create navigation container
        navContainer = new VBox(4);
        navContainer.setPadding(new Insets(0, 16, 16, 16));

        // Add components
        getChildren().addAll(headerLabel, navContainer);
    }

    /**
     * Adds a navigation item
     * 
     * @param item the item to add
     */
    public void addItem(NavigationItem item) {
        items.add(item);
        refreshNavigation();
    }

    /**
     * Sets the current user role
     * 
     * @param role the user role
     */
    public void setUserRole(UserRole role) {
        this.currentRole = role;
        refreshNavigation();
    }

    /**
     * Sets the callback for when an item is selected
     * 
     * @param callback the callback to invoke
     */
    public void setOnItemSelected(Consumer<NavigationItem> callback) {
        this.onItemSelected = callback;
    }

    /**
     * Sets the selected item by ID
     * 
     * @param id the item ID to select
     */
    public void setSelectedItem(String id) {
        this.selectedId = id;
        refreshNavigation();
    }

    private void refreshNavigation() {
        navContainer.getChildren().clear();

        for (NavigationItem item : items) {
            if (item.hasAccess(currentRole)) {
                StyledButton navButton = createNavButton(item);
                navContainer.getChildren().add(navButton);
            }
        }
    }

    private StyledButton createNavButton(NavigationItem item) {
        boolean isSelected = item.getId().equals(selectedId);

        StyledButton button = new StyledButton(item.getTitle(),
                isSelected ? StyledButton.ButtonType.PRIMARY : StyledButton.ButtonType.SECONDARY);

        // Configure button
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        // Add icon if available
        if (item.getIcon() != null && !item.getIcon().isEmpty()) {
            Label iconLabel = new Label(item.getIcon());
            iconLabel.setStyle("-fx-font-family: 'FontAwesome'; -fx-font-size: 14px; -fx-padding: 0 8 0 0;");
            button.setGraphic(iconLabel);
        }

        // Add click handler
        button.setOnAction(e -> {
            if (onItemSelected != null) {
                selectedId = item.getId();
                refreshNavigation();
                onItemSelected.accept(item);
            }
        });

        return button;
    }

    /**
     * Gets the currently selected item
     * 
     * @return the selected item, or null if none is selected
     */
    public NavigationItem getSelectedItem() {
        return items.stream()
                .filter(item -> item.getId().equals(selectedId))
                .findFirst()
                .orElse(null);
    }
}