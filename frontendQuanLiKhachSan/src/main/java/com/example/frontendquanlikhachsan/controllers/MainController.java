package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.controllers.manager.*;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class MainController {

    public void openEditTab() {
        openTab("Bổ sung phiếu thuê", "/com/example/frontendquanlikhachsan/views/receptionist/RentalFormEdit.fxml", null);
    }

    @FXML private TabPane tabPane;
    public TabPane getTabPane() {
        return tabPane;
    }
    @FXML private AnchorPane sidebarContainer;

    @FXML
    private TitledPane ADMIN;
    @FXML
    private TitledPane MANAGER;
    @FXML
    private TitledPane RECEPTIONIST;
    @FXML
    private TitledPane ACCOUNTANT;

    @FXML
    private Button toggleButton; // khai báo thêm nếu chưa có, và gán fx:id="toggleButton" trong FXML

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

        adjustSidebarByPermission();
    }

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
        openTab("Danh sách nhân viên", "/com/example/frontendquanlikhachsan/views/manager/Staff.fxml",
                (StaffController s) -> s.setMainController(this));
    }

    public void openRoomRentingTab() {
        openTab("Thuê phòng", "/com/example/frontendquanlikhachsan/views/receptionist/RoomRenting.fxml", null);
    }
    
//    public void openGuestTab() {
//        openTab("qlkh", "/com/example/frontendquanlikhachsan/views/manager/Guest.fxml", null);
//    }

    public void openGuestTab() {
        openTab("Danh sách khách hàng", "/com/example/frontendquanlikhachsan/views/manager/Guest.fxml",
                (GuestController gc) -> gc.setMainController(this)
        );
    }


    public void openPositionTab() {
        openTab("Danh sách chức vụ", "/com/example/frontendquanlikhachsan/views/manager/Position.fxml", null);
    }

    public void openRentalExtensionFormTab() {
        openTab("Danh sách phiếu gia hạn", "/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml", null);
    }

    public void openRentalExtensionFormTab(List<Integer> ids) {
        openTab("Danh sách phiếu gia hạn", "/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml",
                (RentalExtensionFormController re) -> re.selectExtensionsByIds(ids)
        );
    }

    public void openRentalFormTab() {
        openTab("Danh sách phiếu thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml", null);
    }

    public void openRentalFormTab(List<Integer> ids) {
        openTab("Danh sách phiếu thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml",
                (RentalFormController rc) -> rc.selectRentalFormsByIds(ids)
        );
    }

    public void openInvoiceTab() {
        openTab("Danh sách hoá đơn", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml", null);
    }

    public void openInvoiceTab(List<Integer> ids) {
        openTab("Danh sách hoá đơn", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml",
                (InvoiceController ic) -> ic.selectInvoicesByIds(ids)
        );
    }

    public void openStructureTab() {
        openTab("Cấu trúc khách sạn", "/com/example/frontendquanlikhachsan/views/manager/Structure.fxml", null);
    }

    public void openBookingConfirmationFormTab() {
        openTab("Danh sách phiếu đặt phòng", "/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml", null);
    }

    public void openBookingConfirmationFormTab(List<Integer> ids) {
        openTab("Danh sách phiếu đặt phòng", "/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml",
                (BookingConfirmationFormController bc) -> bc.selectBookingConfirmationFormsByIds(ids)
        );
    }

    public void openReportTab() {
        openTab("Báo cáo tháng", "/com/example/frontendquanlikhachsan/views/accountant/RevenueReport.fxml", null);
    }

    public void openInvoiceAccountantTab() {
        openTab("Tra cứu hoá đơn", "/com/example/frontendquanlikhachsan/views/accountant/InvoiceAccountant.fxml", null);
    }

    public void openSalaryTab() {
        openTab("Lương nhân viên", "/com/example/frontendquanlikhachsan/views/accountant/StaffSalaryAccountant.fxml", null);
    }

    public void openAccountManagement() {
        openTab("qltk", "/com/example/frontendquanlikhachsan/views/admin/Account.fxml", null);
    }

    public void openRentalFormViewTab() {
        openTab("qltk", "/com/example/frontendquanlikhachsan/views/receptionist/RentalFormView.fxml", null);
    }
    public void openUserRoleManagement()
    {
        openTab("Quản lí vai trò", "/com/example/frontendquanlikhachsan/views/admin/UserRole.fxml", null);
    }

    public void openHistoryManagement() {
        openTab("Quản lí lịch sử", "/com/example/frontendquanlikhachsan/views/admin/History.fxml", null);
    }

    public void openVariableManagement() {
        openTab("Quản lí tham số", "/com/example/frontendquanlikhachsan/views/admin/RuleVariable.fxml", null);
    }

    public void openChatbot() {
        openTab("Trợ lí AI", "/com/example/frontendquanlikhachsan/views/Chatbot.fxml", null);
    }

    @FXML
    public void toggleSidebar() {
        isPinned = !isPinned;
        if (!isPinned && sidebarVisible) {
            hideSidebar();
        } else if (isPinned && !sidebarVisible) {
            showSidebar();
        }
        toggleButton.setText(isPinned ? "📌 Ghim" : "Ghim");
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

    public void adjustSidebarByPermission() {
        new Thread(() -> {
            try {
                int currentUserId = TokenHolder.getInstance().getCurrentUserId();

                String staffJson = ApiHttpClientCaller.call("staff/" + currentUserId,
                        ApiHttpClientCaller.Method.GET, null);
                ResponseStaffDto staffDto = ApiHttpClientCaller.mapper.readValue(staffJson, ResponseStaffDto.class);

                String accountJson = ApiHttpClientCaller.call("account/" + staffDto.getAccountId(),
                        ApiHttpClientCaller.Method.GET, null);
                ResponseAccountDto accountDto = ApiHttpClientCaller.mapper.readValue(accountJson, ResponseAccountDto.class);

                List<String> permissions = accountDto.getUserRolePermissionNames();

                Platform.runLater(() -> {
                    // Lưu reference đến parent container
                    Parent sidebarContainer = ADMIN.getParent();

                    // Remove các elements không có permission
                    if (!permissions.contains("ADMIN") && sidebarContainer instanceof Pane) {
                        ((Pane) sidebarContainer).getChildren().remove(ADMIN);
                    }
                    if (!permissions.contains("MANAGER") && sidebarContainer instanceof Pane) {
                        ((Pane) sidebarContainer).getChildren().remove(MANAGER);
                    }
                    if (!permissions.contains("RECEPTIONIST") && sidebarContainer instanceof Pane) {
                        ((Pane) sidebarContainer).getChildren().remove(RECEPTIONIST);
                    }
                    if (!permissions.contains("ACCOUNTANT") && sidebarContainer instanceof Pane) {
                        ((Pane) sidebarContainer).getChildren().remove(ACCOUNTANT);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void logout() {
        // Xoá token
        TokenHolder.getInstance().setAccessToken(null);
        TokenHolder.getInstance().setRefreshToken(null);
        TokenHolder.getInstance().setCurrentUserId(-1);

        // Quay về màn hình đăng nhập
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendquanlikhachsan/views/Login.fxml"));
            Parent loginRoot = loader.load();
            tabPane.getScene().setRoot(loginRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
