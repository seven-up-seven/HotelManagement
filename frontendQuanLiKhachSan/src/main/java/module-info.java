module com.example.frontendquanlikhachsan {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires static lombok;
    requires com.fasterxml.jackson.annotation;

    opens com.example.frontendquanlikhachsan to javafx.fxml;
    opens com.example.frontendquanlikhachsan.controllers to javafx.fxml;
    opens com.example.frontendquanlikhachsan.controllers.manager to javafx.fxml;
    exports com.example.frontendquanlikhachsan;
}