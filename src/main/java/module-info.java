module com.group4.app {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires java.logging;
    requires jbcrypt;

    opens com.group4 to javafx.fxml;
    opens com.group4.controllers to javafx.fxml;
    opens com.group4.models to javafx.base;
    
    exports com.group4;
    exports com.group4.controllers;
    exports com.group4.models;
    exports com.group4.services;
    exports com.group4.utils;
}
