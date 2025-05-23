package com.group4.views.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A styled table component with selection support and responsive design
 * 
 * @param <T> the type of items in the table
 */
public class StyledTable<T> extends VBox {

    private final TableView<T> tableView;
    private final ObservableList<T> items;
    private final ObjectProperty<T> selectedItem;

    /**
     * Creates a new styled table
     */
    public StyledTable() {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Initialize table
        tableView = new TableView<>();
        items = FXCollections.observableArrayList();
        selectedItem = new SimpleObjectProperty<>();

        // Configure table
        tableView.setItems(items);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Bind selection
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedItem.set(newVal);
        });

        // Set table to grow
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Add table to container
        getChildren().add(tableView);
    }

    /**
     * Adds a column to the table
     * 
     * @param column the column to add
     */
    public void addColumn(TableColumn<T, ?> column) {
        tableView.getColumns().add(column);
    }

    /**
     * Gets the list of items in the table
     * 
     * @return the items list
     */
    public ObservableList<T> getItems() {
        return items;
    }

    /**
     * Gets the currently selected item
     * 
     * @return the selected item property
     */
    public ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    /**
     * Gets the currently selected item
     * 
     * @return the selected item
     */
    public T getSelectedItem() {
        return selectedItem.get();
    }

    /**
     * Sets whether multiple selection is allowed
     * 
     * @param allowMultiple whether to allow multiple selection
     */
    public void setAllowMultipleSelection(boolean allowMultiple) {
        tableView.getSelectionModel().setSelectionMode(
                allowMultiple ? javafx.scene.control.SelectionMode.MULTIPLE
                        : javafx.scene.control.SelectionMode.SINGLE);
    }

    /**
     * Sets the placeholder text to show when the table is empty
     * 
     * @param text the placeholder text
     */
    public void setPlaceholder(String text) {
        tableView.setPlaceholder(new javafx.scene.control.Label(text));
    }

    /**
     * Clears all items from the table
     */
    public void clear() {
        items.clear();
    }

    /**
     * Gets the underlying TableView
     * 
     * @return the TableView
     */
    public TableView<T> getTableView() {
        return tableView;
    }
}