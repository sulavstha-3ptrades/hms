module com.group4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.logging;

    // External libraries
    requires jbcrypt;

    opens com.group4 to javafx.fxml;

    exports com.group4;
    exports com.group4.controllers;

    opens com.group4.controllers to javafx.fxml;
}
