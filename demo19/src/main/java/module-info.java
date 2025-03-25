module com.example.demo19 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.swing;


    opens com.example.demo19 to javafx.fxml;
    exports com.example.demo19;
}