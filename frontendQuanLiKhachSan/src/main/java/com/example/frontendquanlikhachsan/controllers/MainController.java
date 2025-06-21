package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.controllers.manager.*;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.stage.Popup;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class MainController {
    @FXML private HBox ADMIN, RECEPTIONIST, MANAGER, ACCOUNTANT;

    @FXML private TabPane tabPane;
    @FXML private javafx.scene.control.ScrollPane dockScrollPane;
    @FXML private HBox dockItems;

    @FXML private Button adminButton;
    @FXML private ContextMenu adminMenu;
    @FXML private Button receptionistButton;
    @FXML private ContextMenu receptionistMenu;
    @FXML private Button managerButton;
    @FXML private ContextMenu managerMenu;
    @FXML private Button accountantButton;
    @FXML private ContextMenu accountantMenu;

    private final Map<String, Integer> tabCounters = new HashMap<>();

    @FXML
    public void initialize() {
        openHomeTab();
        adjustSidebarByPermission();
        setupDockSubMenus();
    }

    private void setupDockSubMenus() {
        hookMenu(adminButton, adminMenu);
        hookMenu(managerButton, managerMenu);
        hookMenu(receptionistButton, receptionistMenu);
        hookMenu(accountantButton, accountantMenu);
    }

    private void hookMenu(Button btn, ContextMenu menu) {
        btn.setOnMouseClicked(e -> {
            if (!menu.isShowing()) {
                menu.show(btn, Side.TOP, 0, -5);
            } else {
                menu.hide();
            }
        });
    }

    // ---------- Generic Tab Opening ----------
    private <C> void openTab(String baseTitle, String fxmlPath, Consumer<C> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            int cnt = tabCounters.getOrDefault(baseTitle, 0) + 1;
            tabCounters.put(baseTitle, cnt);
            String title = baseTitle + " #" + cnt;

            @SuppressWarnings("unchecked")
            C ctrl = (C) loader.getController();
            if (controllerInitializer != null) {
                controllerInitializer.accept(ctrl);
            }

            Tab tab = new Tab(title, content);
            tab.setClosable(true);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- Receptionist ----------
    public void openHomeTab() {
        openTab("Trang chủ", "/com/example/frontendquanlikhachsan/views/Home.fxml", null);
    }

    public void openRoomRentingTab() {
        openTab("Thuê phòng", "/com/example/frontendquanlikhachsan/views/receptionist/RoomRenting.fxml", null);
    }

    public void openEditTab() {
        openTab("Bổ sung phiếu thuê", "/com/example/frontendquanlikhachsan/views/receptionist/RentalFormEdit.fxml", null);
    }

    public void openRentalFormViewTab() {
        openTab("Lập hoá đơn", "/com/example/frontendquanlikhachsan/views/receptionist/RentalFormView.fxml", null);
    }

    // ---------- Manager ----------
    public void openGuestTab() {
        openTab("Khách hàng", "/com/example/frontendquanlikhachsan/views/manager/Guest.fxml",
                (GuestController c) -> c.setMainController(this));
    }

    public void openStaffTab() {
        openTab("Nhân viên", "/com/example/frontendquanlikhachsan/views/manager/Staff.fxml",
                (StaffController c) -> c.setMainController(this));
    }

    public void openPositionTab() {
        openTab("Chức vụ", "/com/example/frontendquanlikhachsan/views/manager/Position.fxml", null);
    }

    public void openRentalFormTab() {
        openTab("Phiếu thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml", null);
    }

    public void openRentalFormTab(List<Integer> ids) {
        openTab("Phiếu thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml",
                (RentalFormController c) -> c.selectRentalFormsByIds(ids));
    }

    public void openRentalExtensionFormTab() {
        openTab("Gia hạn thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml", null);
    }

    public void openRentalExtensionFormTab(List<Integer> ids) {
        openTab("Gia hạn thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml",
                (RentalExtensionFormController c) -> c.selectExtensionsByIds(ids));
    }

    public void openInvoiceTab() {
        openTab("Hoá đơn", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml", null);
    }

    public void openInvoiceTab(List<Integer> ids) {
        openTab("Hoá đơn", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml",
                (InvoiceController c) -> c.selectInvoicesByIds(ids));
    }

    public void openStructureTab() {
        openTab("Cấu trúc KS", "/com/example/frontendquanlikhachsan/views/manager/Structure.fxml", null);
    }

    public void openBookingConfirmationFormTab() {
        openTab("Phiếu đặt phòng", "/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml", null);
    }

    public void openBookingConfirmationFormTab(List<Integer> ids) {
        openTab("Phiếu đặt phòng", "/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml",
                (BookingConfirmationFormController c) -> c.selectBookingConfirmationFormsByIds(ids));
    }

    // ---------- Accountant ----------
    public void openReportTab() {
        openTab("Báo cáo tháng", "/com/example/frontendquanlikhachsan/views/accountant/RevenueReport.fxml", null);
    }

    public void openInvoiceAccountantTab() {
        openTab("Tra cứu hoá đơn", "/com/example/frontendquanlikhachsan/views/accountant/InvoiceAccountant.fxml", null);
    }

    public void openSalaryTab() {
        openTab("Lương nhân viên", "/com/example/frontendquanlikhachsan/views/accountant/StaffSalaryAccountant.fxml", null);
    }

    // ---------- Admin ----------
    public void openAccountManagement() {
        openTab("Tài khoản", "/com/example/frontendquanlikhachsan/views/admin/Account.fxml", null);
    }

    public void openUserRoleManagement() {
        openTab("Vai trò", "/com/example/frontendquanlikhachsan/views/admin/UserRole.fxml", null);
    }

    public void openHistoryManagement() {
        openTab("Lịch sử", "/com/example/frontendquanlikhachsan/views/admin/History.fxml", null);
    }

    public void openVariableManagement() {
        openTab("Tham số", "/com/example/frontendquanlikhachsan/views/admin/RuleVariable.fxml", null);
    }

    // ---------- Khác ----------
    public void openChatbot() {
        openTab("Trợ lý AI", "/com/example/frontendquanlikhachsan/views/Chatbot.fxml", null);
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
                    if (!permissions.contains("ADMIN")) adminButton.setVisible(false);
                    if (!permissions.contains("MANAGER")) managerButton.setVisible(false);
                    if (!permissions.contains("RECEPTIONIST")) receptionistButton.setVisible(false);
                    if (!permissions.contains("ACCOUNTANT")) accountantButton.setVisible(false);
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
