package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.controllers.manager.GuestController;
import com.example.frontendquanlikhachsan.controllers.manager.InvoiceController;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class MainController {

    @FXML private TabPane tabPane;
    public TabPane getTabPane() {
        return tabPane;
    }
    @FXML private AnchorPane sidebarContainer;

    private final Map<String, Integer> tabCounters = new HashMap<>();
    private boolean isPinned = true;
    private boolean sidebarVisible = true;

    public void initialize() {
        tabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupAutoHideBehavior();
            }
        });

        // Set initial state
        if (!isPinned) {
            sidebarContainer.setTranslateX(-sidebarContainer.getPrefWidth());
            sidebarContainer.setMouseTransparent(true);
            sidebarContainer.setManaged(false);
            sidebarContainer.setVisible(true);
        } else {
            sidebarContainer.setTranslateX(0);
            sidebarContainer.setMouseTransparent(false);
            sidebarContainer.setManaged(true);
            sidebarContainer.setVisible(true);
        }
    }

//    private void openTab(String title, String fxmlPath) {
//        try {
//            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fxmlPath));
//            Parent content = loader.load();
//
//            int count = tabCounters.getOrDefault(title, 0) + 1;
//            tabCounters.put(title, count);
//
//            String tabTitle = title + " #" + count;
//            Tab tab = new Tab(tabTitle, content);
//            tab.setClosable(true);
//
//            tabPane.getTabs().add(tab);
//            tabPane.getSelectionModel().select(tab);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
private <C> void openTab(String baseTitle,
                         String fxmlPath,
                         Consumer<C> controllerInitializer) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent content = loader.load();

        int cnt = tabCounters.getOrDefault(baseTitle, 0) + 1;
        tabCounters.put(baseTitle, cnt);
        String title = baseTitle + " #" + cnt;

        // Lấy controller, gọi initializer nếu có
        @SuppressWarnings("unchecked")
        C ctrl = (C)loader.getController();
        if (controllerInitializer != null) {
            controllerInitializer.accept(ctrl);
        }

        Tab tab = new Tab(title, content);
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    } catch(IOException e) {
        e.printStackTrace();
        // TODO: show alert lỗi
    }
}

    public void openHomeTab() {
        openTab("Trang chủ", "/com/example/frontendquanlikhachsan/views/Home.fxml", null);
    }

    public void openBookingTab() {
        openTab("Đặt phòng", "/com/example/frontendquanlikhachsan/views/receptionist/Booking.fxml", null);
    }

    public void openStaffTab() {
        openTab("qlnv", "/com/example/frontendquanlikhachsan/views/manager/Staff.fxml", null);
    }

    public void openRoomRentingTab() {
        openTab("Thuê phòng", "/com/example/frontendquanlikhachsan/views/receptionist/RoomRenting.fxml", null);
    }
    
//    public void openGuestTab() {
//        openTab("qlkh", "/com/example/frontendquanlikhachsan/views/manager/Guest.fxml", null);
//    }

    public void openGuestTab() {
        openTab("QL Khách", "/com/example/frontendquanlikhachsan/views/manager/Guest.fxml",
                (GuestController gc) -> gc.setMainController(this)
        );
    }


    public void openPositionTab() {
        openTab("qlcv", "/com/example/frontendquanlikhachsan/views/manager/Position.fxml", null);
    }

    public void openRentalExtensionFormTab() {
        openTab("qlcv", "/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml", null);
    }

    public void openRentalFormTab() {
        openTab("qlpt", "/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml", null);
    }

    public void openInvoiceTab() {
        openTab("qlhđ", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml", null);
    }

    public void openInvoiceTab(int invoiceId) {
        openTab("qlhđ", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml",
                (InvoiceController ic) -> ic.selectInvoiceById(invoiceId)
        );
    }

    public void openStructureTab() {
        openTab("qlhđ", "/com/example/frontendquanlikhachsan/views/manager/Structure.fxml", null);
    }

    public void openBookingConfirmationFormTab() {
        openTab("qlhđ", "/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml", null);
    }

    public void openReportTab() {
        openTab("qlhđ", "/com/example/frontendquanlikhachsan/views/accountant/RevenueReport.fxml", null);
    }

    public void openInvoiceAccountantTab() {
        openTab("qlhđ", "/com/example/frontendquanlikhachsan/views/accountant/InvoiceAccountant.fxml", null);
    }

    public void openSalaryTab() {
        openTab("qlhđ", "/com/example/frontendquanlikhachsan/views/accountant/StaffSalaryAccountant.fxml", null);
    }

    @FXML
    public void toggleSidebar() {
        isPinned = !isPinned;
        if (!isPinned && sidebarVisible) {
            hideSidebar();
        } else if (isPinned && !sidebarVisible) {
            showSidebar();
        }
    }

    private void showSidebar() {
        // 1. First shrink the content area (make room for sidebar)
        // We'll use constraints to properly adjust layout
        double sidebarWidth = sidebarContainer.getPrefWidth();
        tabPane.setPrefWidth(tabPane.getWidth() - sidebarWidth);

        // 2. After delay, show sidebar
        PauseTransition delay = new PauseTransition(Duration.millis(50));
        delay.setOnFinished(e -> {
            // Make sidebar part of the layout
            sidebarContainer.setManaged(true);
            sidebarContainer.setMouseTransparent(false);

            // Animate it coming in
            TranslateTransition sidebarShow = new TranslateTransition(Duration.millis(220), sidebarContainer);
            sidebarShow.setFromX(-sidebarWidth);
            sidebarShow.setToX(0);
            sidebarShow.setInterpolator(Interpolator.EASE_OUT);
            sidebarShow.play();
        });
        delay.play();

        sidebarVisible = true;
    }

    private void hideSidebar() {
        // 1. First hide the sidebar
        double sidebarWidth = sidebarContainer.getPrefWidth();
        TranslateTransition sidebarHide = new TranslateTransition(Duration.millis(300), sidebarContainer);
        sidebarHide.setToX(-sidebarWidth);
        sidebarHide.setInterpolator(Interpolator.EASE_IN);
        sidebarHide.play();

        // 2. After delay, expand content
        PauseTransition delay = new PauseTransition(Duration.millis(310));
        delay.setOnFinished(e -> {
            // Remove sidebar from layout calculations
            sidebarContainer.setManaged(false);
            sidebarContainer.setMouseTransparent(true);
            sidebarContainer.setVisible(true);

            // Clear preferred width to let the tabPane fill available space
            tabPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        });
        delay.play();

        sidebarVisible = false;
    }

    private void setupAutoHideBehavior() {
        tabPane.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (!isPinned) {
                if (event.getScreenX() < 5 && !sidebarVisible) {
                    showSidebar();
                } else if (event.getScreenX() > sidebarContainer.getPrefWidth() + 10 && sidebarVisible) {
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.3));
                    delay.setOnFinished(e -> {
                        if (!isPinned) hideSidebar();
                    });
                    delay.play();
                }
            }
        });
    }
}
