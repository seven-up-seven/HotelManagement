module com.example.frontendquanlikhachsan {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires static lombok;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires java.management;
    requires jakarta.validation;
    requires spring.context;
    requires java.validation;
    requires org.apache.tomcat.embed.el;

    opens com.example.frontendquanlikhachsan to javafx.fxml;
    opens com.example.frontendquanlikhachsan.controllers to javafx.fxml;
    opens com.example.frontendquanlikhachsan.controllers.manager to javafx.fxml;
    opens com.example.frontendquanlikhachsan.controllers.receptionist to javafx.fxml;
    opens com.example.frontendquanlikhachsan.controllers.accountant to javafx.fxml;
    opens com.example.frontendquanlikhachsan.entity.account to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.staff to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.guest to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.room to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.position to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.enums to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.rentalExtensionForm to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.rentalForm to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.rentalFormDetail to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.page to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.invoice to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.invoicedetail to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.block to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.floor to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.bookingconfirmationform to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.revenueReport to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.entity.revenueReportDetail to com.fasterxml.jackson.databind;
    opens com.example.frontendquanlikhachsan.auth to com.fasterxml.jackson.databind;

    exports com.example.frontendquanlikhachsan;
}
