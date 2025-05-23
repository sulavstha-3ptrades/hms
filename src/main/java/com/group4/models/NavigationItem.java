package com.group4.models;

import javafx.scene.Node;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents a navigation item in the sidebar
 */
public class NavigationItem {
    private final String id;
    private final String title;
    private final String icon;
    private final Set<UserRole> allowedRoles;
    private Node view;

    /**
     * Creates a new navigation item
     * 
     * @param id    unique identifier for the item
     * @param title display title
     * @param icon  icon name (e.g., "dashboard", "users")
     * @param view  the view to display when selected
     * @param roles roles that have access to this item
     */
    public NavigationItem(String id, String title, String icon, Node view, UserRole... roles) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.view = view;
        this.allowedRoles = new HashSet<>();
        if (roles != null) {
            for (UserRole role : roles) {
                allowedRoles.add(role);
            }
        }
    }

    /**
     * Checks if a user role has access to this item
     * 
     * @param role the role to check
     * @return true if the role has access
     */
    public boolean hasAccess(UserRole role) {
        return allowedRoles.contains(role);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public Node getView() {
        return view;
    }

    public void setView(Node view) {
        this.view = view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NavigationItem that = (NavigationItem) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}