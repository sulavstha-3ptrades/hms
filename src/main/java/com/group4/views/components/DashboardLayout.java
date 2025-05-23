package components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * A dashboard layout component that provides a modern, responsive layout
 */
public class DashboardLayout extends BorderPane {

    private final VBox sidebar;
    private final VBox contentArea;
    private final HBox header;
    private final Label titleLabel;
    private final HBox headerActions;
    private final ToastNotification toastNotification;

    /**
     * Creates a new dashboard layout
     * 
     * @param title the dashboard title
     */
    public DashboardLayout(String title) {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Create header
        header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        headerActions = new HBox(8);
        headerActions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(headerActions, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, headerActions);

        // Create sidebar
        sidebar = new VBox(8);
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #f5f5f5;");

        // Create content area with scroll
        contentArea = new VBox(16);
        contentArea.setPadding(new Insets(16));

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Create toast notification
        toastNotification = new ToastNotification();

        // Stack toast over content
        VBox contentStack = new VBox();
        contentStack.getChildren().addAll(scrollPane, toastNotification);

        // Set layout
        setTop(header);
        setLeft(sidebar);
        setCenter(contentStack);
    }

    /**
     * Adds an action to the header
     * 
     * @param node the action node to add
     */
    public void addHeaderAction(Node node) {
        headerActions.getChildren().add(node);
    }

    /**
     * Adds a navigation item to the sidebar
     * 
     * @param node the navigation node to add
     */
    public void addSidebarItem(Node node) {
        sidebar.getChildren().add(node);
    }

    /**
     * Adds content to the main area
     * 
     * @param node the content to add
     */
    public void addContent(Node node) {
        contentArea.getChildren().add(node);
    }

    /**
     * Sets the content of the main area
     * 
     * @param node the content node
     */
    public void setContent(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    /**
     * Gets the toast notification component
     * 
     * @return the toast notification
     */
    public ToastNotification getToastNotification() {
        return toastNotification;
    }

    /**
     * Sets the dashboard title
     * 
     * @param title the new title
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Clears all content from the main area
     */
    public void clearContent() {
        contentArea.getChildren().clear();
    }

    /**
     * Sets whether the sidebar is visible
     * 
     * @param visible whether the sidebar should be visible
     */
    public void setSidebarVisible(boolean visible) {
        sidebar.setVisible(visible);
        sidebar.setManaged(visible);
    }
}