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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class MainController {
    @FXML private HBox ADMIN, RECEPTIONIST, MANAGER, ACCOUNTANT;

    @FXML private VBox contentPane;
    @FXML private ScrollPane dockScrollPane;
    @FXML private HBox dockItems;

    @FXML private Button adminButton;
    @FXML private ContextMenu adminMenu;
    @FXML private Button receptionistButton;
    @FXML private ContextMenu receptionistMenu;
    @FXML private Button managerButton;
    @FXML private ContextMenu managerMenu;
    @FXML private Button accountantButton;
    @FXML private ContextMenu accountantMenu;

    private final Map<String, Runnable> quickAccessViews = new HashMap<>();

    private List<String> currentUserPermissions = new ArrayList<>();

    @FXML
    public void initialize() {
        openHomeTab();
        adjustSidebarByPermission();
        setupDockSubMenus();

        Platform.runLater(() -> {
            contentPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown() && event.getCode().toString().equals("T")) {
                    showSearchPopup();
                    event.consume();
                }
            });
        });
    }

    private void showSearchPopup() {
        Popup popup = new Popup();
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        container.setPrefWidth(360);

        TextField input = new TextField();
        input.setPromptText("Nhập tên view...");
        ListView<String> listView = new ListView<>();

        // Load ban đầu
        List<String> allViews = new ArrayList<>(quickAccessViews.keySet());
        listView.getItems().addAll(allViews);

        // ------ Hàm chuẩn hoá không dấu ------
        Function<String, String> normalize = text -> java.text.Normalizer
                .normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();

        // ------ Lọc khi nhập ------
        input.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = normalize.apply(newVal.trim());
            List<String> filtered = allViews.stream()
                    .filter(label -> normalize.apply(label).contains(query))
                    .toList();
            listView.getItems().setAll(filtered);

            if (!filtered.isEmpty()) {
                listView.getSelectionModel().selectFirst(); // luôn chọn dòng đầu tiên
            }
        });

        // ------ Click chuột ------
        listView.setOnMouseClicked(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                quickAccessViews.get(selected).run();
                popup.hide();
            }
        });

        // ------ Phím điều hướng ------
        input.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> {
                    String selected = listView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        quickAccessViews.get(selected).run();
                        popup.hide();
                    }
                }
                case ESCAPE -> popup.hide();
                case DOWN -> listView.getSelectionModel().selectNext();
                case UP -> listView.getSelectionModel().selectPrevious();
            }
        });

        container.getChildren().addAll(input, listView);
        popup.getContent().add(container);

        // hiển thị giữa màn hình
        popup.show(contentPane.getScene().getWindow());
        popup.setX(contentPane.getScene().getWindow().getX() + contentPane.getWidth() / 2 - container.getPrefWidth() / 2);
        popup.setY(contentPane.getScene().getWindow().getY() + 100);

        Platform.runLater(() -> {
            input.requestFocus();
            listView.getSelectionModel().selectFirst(); // chọn sẵn
        });
    }

    private void setupDockSubMenus() {
        hookMenu(adminButton, adminMenu);
        hookMenu(managerButton, managerMenu);
        hookMenu(receptionistButton, receptionistMenu);
        hookMenu(accountantButton, accountantMenu);
    }

    private void hookMenu(Button btn, ContextMenu menu) {
        btn.setOnMouseClicked(e -> {
            if (menu.isShowing()) {
                menu.hide();
                return;
            }

            Point2D screenPos = btn.localToScreen(0, btn.getHeight());
            menu.show(btn, screenPos.getX(), screenPos.getY());
        });
    }

    // ---------- Generic View Opening ----------
    private <C> void openView(String fxmlPath, Consumer<C> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            @SuppressWarnings("unchecked")
            C ctrl = (C) loader.getController();
            if (controllerInitializer != null) {
                controllerInitializer.accept(ctrl);
            }

            contentPane.getChildren().clear(); // Xóa nội dung cũ
            contentPane.getChildren().add(content); // Thêm nội dung mới
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- Receptionist ----------
    public void openHomeTab() {
        openView("/com/example/frontendquanlikhachsan/views/Home.fxml", null);
    }

    public void openRoomRentingTab() {
        openView("/com/example/frontendquanlikhachsan/views/receptionist/RoomRenting.fxml", null);
    }

    public void openEditTab() {
        openView("/com/example/frontendquanlikhachsan/views/receptionist/RentalFormEdit.fxml", null);
    }

    public void openRentalFormViewTab() {
        openView("/com/example/frontendquanlikhachsan/views/receptionist/RentalFormView.fxml", null);
    }

    // ---------- Manager ----------
    public void openGuestTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/Guest.fxml",
                (GuestController c) -> c.setMainController(this));
    }

    public void openStaffTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/Staff.fxml",
                (StaffController c) -> c.setMainController(this));
    }

    public void openPositionTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/Position.fxml", null);
    }

    public void openRentalFormTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml", null);
    }

    public void openRentalFormTab(List<Integer> ids) {
        openView("/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml",
                (RentalFormController c) -> c.selectRentalFormsByIds(ids));
    }

    public void openRentalExtensionFormTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml", null);
    }

    public void openRentalExtensionFormTab(List<Integer> ids) {
        openView("/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml",
                (RentalExtensionFormController c) -> c.selectExtensionsByIds(ids));
    }

    public void openInvoiceTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml", null);
    }

    public void openInvoiceTab(List<Integer> ids) {
        openView("/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml",
                (InvoiceController c) -> c.selectInvoicesByIds(ids));
    }

    public void openStructureTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/Structure.fxml", null);
    }

    public void openBookingConfirmationFormTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml", null);
    }

    public void openBookingConfirmationFormTab(List<Integer> ids) {
        openView("/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml",
                (BookingConfirmationFormController c) -> c.selectBookingConfirmationFormsByIds(ids));
    }

    public void openMapTab() {
        openView("/com/example/frontendquanlikhachsan/views/manager/Map.fxml", null);
    }

    // ---------- Accountant ----------
    public void openReportTab() {
        openView("/com/example/frontendquanlikhachsan/views/accountant/RevenueReport.fxml", null);
    }

    public void openInvoiceAccountantTab() {
        openView("/com/example/frontendquanlikhachsan/views/accountant/InvoiceAccountant.fxml", null);
    }

    public void openSalaryTab() {
        openView("/com/example/frontendquanlikhachsan/views/accountant/StaffSalaryAccountant.fxml", null);
    }

    // ---------- Admin ----------
    public void openAccountManagement() {
        openView("/com/example/frontendquanlikhachsan/views/admin/Account.fxml", null);
    }

    public void openUserRoleManagement() {
        openView("/com/example/frontendquanlikhachsan/views/admin/UserRole.fxml", null);
    }

    public void openHistoryManagement() {
        openView("/com/example/frontendquanlikhachsan/views/admin/History.fxml", null);
    }

    public void openVariableManagement() {
        openView("/com/example/frontendquanlikhachsan/views/admin/RuleVariable.fxml", null);
    }

    // ---------- Khác ----------
    public void openChatbot() {
        openView("/com/example/frontendquanlikhachsan/views/Chatbot.fxml", null);
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
                    if (!permissions.contains("ADMIN")) removeButtonFromParent(adminButton);
                    if (!permissions.contains("MANAGER")) removeButtonFromParent(managerButton);
                    if (!permissions.contains("RECEPTIONIST")) removeButtonFromParent(receptionistButton);
                    if (!permissions.contains("ACCOUNTANT")) removeButtonFromParent(accountantButton);

                    currentUserPermissions.clear();
                    currentUserPermissions.addAll(permissions);

                    setUpQuickAccess();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void removeButtonFromParent(Node button) {
        if (button != null && button.getParent() instanceof Pane) {
            ((Pane) button.getParent()).getChildren().remove(button);
        }
    }

    @FXML
    public void logout() {
        // Hộp thoại xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        alert.setContentText("Hành động này sẽ đưa bạn về màn hình đăng nhập.");

        // Tùy chọn xác nhận hoặc hủy
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Nếu người dùng xác nhận => thực hiện đăng xuất
            TokenHolder.getInstance().setAccessToken(null);
            TokenHolder.getInstance().setRefreshToken(null);
            TokenHolder.getInstance().setCurrentUserId(-1);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendquanlikhachsan/views/Login.fxml"));
                Parent loginRoot = loader.load();
                contentPane.getScene().setRoot(loginRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Người dùng chọn Hủy => không làm gì cả
            System.out.println("Huỷ đăng xuất.");
        }
    }

    private boolean hasPermission(String role) {
        return currentUserPermissions.contains(role);
    }

    private void setUpQuickAccess() {
        quickAccessViews.clear();

        if (hasPermission("RECEPTIONIST")) {
            quickAccessViews.put("Tạo phiếu thuê phòng", this::openRoomRentingTab);
            quickAccessViews.put("Bổ sung phiếu thuê", this::openEditTab);
            quickAccessViews.put("Lập hoá đơn", this::openRentalFormViewTab);
        }

        if (hasPermission("MANAGER")) {
            quickAccessViews.put("Quản lí khách hàng", this::openGuestTab);
            quickAccessViews.put("Quản lí nhân viên", this::openStaffTab);
            quickAccessViews.put("Quản lí chức vụ", this::openPositionTab);
            quickAccessViews.put("Quản lí phiếu thuê", this::openRentalFormTab);
            quickAccessViews.put("Gia hạn phiếu thuê", this::openRentalExtensionFormTab);
            quickAccessViews.put("Quản lí hoá đơn", this::openInvoiceTab);
            quickAccessViews.put("Cấu trúc khách sạn", this::openStructureTab);
            quickAccessViews.put("Quản lí phiếu đặt phòng", this::openBookingConfirmationFormTab);
            quickAccessViews.put("Sơ đồ khách sạn", this::openMapTab);
        }

        if (hasPermission("ACCOUNTANT")) {
            quickAccessViews.put("Báo cáo tháng", this::openReportTab);
            quickAccessViews.put("Tra cứu hoá đơn", this::openInvoiceAccountantTab);
            quickAccessViews.put("Lương nhân viên", this::openSalaryTab);
        }

        if (hasPermission("ADMIN")) {
            quickAccessViews.put("Quản lí tài khoản", this::openAccountManagement);
            quickAccessViews.put("Quản lí vai trò", this::openUserRoleManagement);
            quickAccessViews.put("Quản lí lịch sử", this::openHistoryManagement);
            quickAccessViews.put("Quản lí tham số", this::openVariableManagement);
        }

        // Khác: Ai cũng được dùng
        quickAccessViews.put("Trợ lý AI", this::openChatbot);
        quickAccessViews.put("Trang chủ", this::openHomeTab);
    }
}