package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.controllers.manager.*;
import com.example.frontendquanlikhachsan.controllers.setting.KeyMapController;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class MainController {
    @FXML
    private TabPane tabPane;
    @FXML
    private Button adminButton;
    @FXML
    private ContextMenu adminMenu;
    @FXML
    private Button receptionistButton;
    @FXML
    private ContextMenu receptionistMenu;
    @FXML
    private Button managerButton;
    @FXML
    private ContextMenu managerMenu;
    @FXML
    private Button accountantButton;
    @FXML
    private ContextMenu accountantMenu;
    @FXML
    private VBox sidebarContainer;
    @FXML
    private Button toggleSidebarButton;
    @FXML
    private Pane overlayPane; // Overlay pane
    @FXML
    private HBox ADMIN, RECEPTIONIST, MANAGER, ACCOUNTANT;
    @FXML
    private ScrollPane dockScrollPane;
    @FXML
    private HBox dockItems;
    @FXML
    private Label dateDayLabel;
    @FXML
    private Label clockLabel;

    private final Map<String, Runnable> quickAccessViews = new HashMap<>();
    private final Map<String, Integer> tabCounters = new HashMap<>();

    private List<String> currentUserPermissions = new ArrayList<>();

    private boolean isSidebarVisible = false;

    @FXML
    public void initialize() {
        startClock();
        openHomeTab();
        adjustSidebarByPermission();
        setupDockSubMenus();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);

        Platform.runLater(() -> {
            if (tabPane.getScene() != null) {
                // Thêm sự kiện hover cho root pane
                tabPane.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
                    double mouseX = event.getSceneX();
                    if (mouseX <= 20 && !isSidebarVisible) { // Hiển thị khi chuột gần mép trái (20px)
                        showSidebar();
                    }
                });

                // Thêm sự kiện phím tắt Ctrl+T
                tabPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown()) {
                        String shortcutKey = KeyMapController.getSearchKey(); // Lấy từ Preferences
                        if (event.getCode().getName().equalsIgnoreCase(shortcutKey)) {
                            showSearchPopup();
                            event.consume();
                        }
                    }
                });
            }
        });

        // Ẩn sidebar khi chuột rời khỏi
        sidebarContainer.setOnMouseExited(event -> {
            if (isSidebarVisible && event.getSceneX() > 280) {
                hideSidebar();
            }
        });
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalTime currentTime = LocalTime.now();
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            String dayOfWeek = switch (currentDate.getDayOfWeek()) {
                case MONDAY -> "Thứ hai";
                case TUESDAY -> "Thứ ba";
                case WEDNESDAY -> "Thứ tư";
                case THURSDAY -> "Thứ năm";
                case FRIDAY -> "Thứ sáu";
                case SATURDAY -> "Thứ bảy";
                case SUNDAY -> "Chủ nhật";
            };

            clockLabel.setText(currentTime.format(timeFormatter));
            dateDayLabel.setText(currentDate.format(dateFormatter) + " – " + dayOfWeek);
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @FXML
    public void toggleSidebar() {
        isSidebarVisible = !isSidebarVisible;
        sidebarContainer.setVisible(isSidebarVisible);
        overlayPane.setVisible(isSidebarVisible);
        overlayPane.setMouseTransparent(!isSidebarVisible); // Enable/disable interaction

        if (isSidebarVisible) {
            sidebarContainer.setStyle("-fx-translate-x: 0;");
        } else {
            sidebarContainer.setStyle("-fx-translate-x: -280px;");
        }
    }

    private void showSidebar() {
        isSidebarVisible = true;
        sidebarContainer.setVisible(true);
        overlayPane.setVisible(true);
        overlayPane.setMouseTransparent(false);
        sidebarContainer.setStyle("-fx-translate-x: 0;");
    }

    @FXML
    public void handleOverlayClick() {
        if (isSidebarVisible) {
            isSidebarVisible = false;
            sidebarContainer.setVisible(false);
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
            sidebarContainer.setStyle("-fx-translate-x: -280px;");
        }
    }

    public void hideSidebar () {
        if (isSidebarVisible) {
            isSidebarVisible = false;
            sidebarContainer.setVisible(false);
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true);
            sidebarContainer.setStyle("-fx-translate-x: -280px;");
        }
    }

    private void showSearchPopup () {
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
        popup.show(tabPane.getScene().getWindow());
        popup.setX(tabPane.getScene().getWindow().getX() + tabPane.getWidth() / 2 - container.getPrefWidth() / 2);
        popup.setY(tabPane.getScene().getWindow().getY() + 100);

        Platform.runLater(() -> {
            input.requestFocus();
            listView.getSelectionModel().selectFirst(); // chọn sẵn
        });
    }

    private void setupDockSubMenus () {
        hookMenu(adminButton, adminMenu);
        hookMenu(managerButton, managerMenu);
        hookMenu(receptionistButton, receptionistMenu);
        hookMenu(accountantButton, accountantMenu);
    }

    private void hookMenu (Button btn, ContextMenu menu){
        btn.setOnMouseClicked(e -> {
            if (menu.isShowing()) {
                menu.hide();
                return;
            }

            Point2D screenPos = btn.localToScreen(0, btn.getHeight());
            menu.show(btn, screenPos.getX(), screenPos.getY());
        });
    }

    private <C > void openTab (String baseTitle, String fxmlPath, Consumer < C > controllerInitializer){
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

    public void openHomeTab () {
        openTab("Trang chủ", "/com/example/frontendquanlikhachsan/views/Home.fxml", null);
    }

    //---------- Receptionist ----------
    public void openRoomRentingTab () {
        openTab("Thuê phòng", "/com/example/frontendquanlikhachsan/views/receptionist/RoomRenting.fxml", null);
    }

    public void openEditTab () {
        openTab("Bổ sung phiếu thuê", "/com/example/frontendquanlikhachsan/views/receptionist/RentalFormEdit.fxml", null);
    }

    public void openRentalFormViewTab () {
        openTab("Lập hoá đơn", "/com/example/frontendquanlikhachsan/views/receptionist/RentalFormView.fxml", null);
    }

    // ---------- Manager ----------
    public void openGuestTab() {
        openTab("Khách hàng", "/com/example/frontendquanlikhachsan/views/manager/Guest.fxml",
                (GuestController c) -> c.setMainController(this));
    }

    public void openStaffTab () {
        openTab("Nhân viên", "/com/example/frontendquanlikhachsan/views/manager/Staff.fxml",
                (StaffController c) -> c.setMainController(this));
    }

    public void openPositionTab () {
        openTab("Chức vụ", "/com/example/frontendquanlikhachsan/views/manager/Position.fxml", null);
    }

    public void openRentalFormTab () {
        openTab("Phiếu thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml", null);
    }

    public void openRentalFormTab (List < Integer > ids) {
        openTab("Phiếu thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalForm.fxml",
                (RentalFormController c) -> c.selectRentalFormsByIds(ids));
    }

    public void openRentalExtensionFormTab () {
        openTab("Gia hạn thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml", null);
    }

    public void openRentalExtensionFormTab (List < Integer > ids) {
        openTab("Gia hạn thuê", "/com/example/frontendquanlikhachsan/views/manager/RentalExtensionForm.fxml",
                (RentalExtensionFormController c) -> c.selectExtensionsByIds(ids));
    }

    public void openInvoiceTab () {
        openTab("Hoá đơn", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml", null);
    }

    public void openInvoiceTab (List < Integer > ids) {
        openTab("Hoá đơn", "/com/example/frontendquanlikhachsan/views/manager/Invoice.fxml",
                (InvoiceController c) -> c.selectInvoicesByIds(ids));
    }

    public void openStructureTab () {
        openTab("Cấu trúc KS", "/com/example/frontendquanlikhachsan/views/manager/Structure.fxml", null);
    }

    public void openBookingConfirmationFormTab () {
        openTab("Phiếu đặt phòng", "/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml", null);
    }

    public void openBookingConfirmationFormTab (List < Integer > ids) {
        openTab("Phiếu đặt phòng", "/com/example/frontendquanlikhachsan/views/manager/BookingConfirmationForm.fxml",
                (BookingConfirmationFormController c) -> c.selectBookingConfirmationFormsByIds(ids));
    }

    public void openMapTab () {
        openTab("Sơ đồ khách sạn", "/com/example/frontendquanlikhachsan/views/manager/Map.fxml", null);
    }

    // ---------- Accountant ----------
    public void openReportTab() {
        openTab("Báo cáo tháng", "/com/example/frontendquanlikhachsan/views/accountant/RevenueReport.fxml", null);
    }

    public void openInvoiceAccountantTab () {
        openTab("Tra cứu hoá đơn", "/com/example/frontendquanlikhachsan/views/accountant/InvoiceAccountant.fxml", null);
    }

    public void openSalaryTab () {
        openTab("Lương nhân viên", "/com/example/frontendquanlikhachsan/views/accountant/StaffSalaryAccountant.fxml", null);
    }

    // ---------- Admin ----------
    public void openAccountManagement() {
        openTab("Tài khoản", "/com/example/frontendquanlikhachsan/views/admin/Account.fxml", null);
    }

    public void openUserRoleManagement () {
        openTab("Vai trò", "/com/example/frontendquanlikhachsan/views/admin/UserRole.fxml", null);
    }

    public void openHistoryManagement () {
        openTab("Lịch sử", "/com/example/frontendquanlikhachsan/views/admin/History.fxml", null);
    }

    public void openVariableManagement () {
        openTab("Tham số", "/com/example/frontendquanlikhachsan/views/admin/RuleVariable.fxml", null);
    }

    // ---------- Khác ----------
    public void openChatbot() {
        openTab("Trợ lý AI", "/com/example/frontendquanlikhachsan/views/Chatbot.fxml", null);
    }

    //this tab is opened on a new window besides the app
    public void openSettings () {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/frontendquanlikhachsan/views/setting/Setting.fxml"));
            Parent root = fxmlLoader.load();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Cài đặt");

            Scene scene = new Scene(root);
            settingsStage.setScene(scene);
            settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/frontendquanlikhachsan/assets/images/setting_icon.png")));

            settingsStage.initModality(Modality.NONE);
            settingsStage.initOwner(null);

            settingsStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void adjustSidebarByPermission () {
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

    private void removeButtonFromParent (Node button){
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

        // Thêm stylesheet cho DialogPane
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );

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
                tabPane.getScene().setRoot(loginRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Người dùng chọn Hủy => không làm gì cả
            System.out.println("Huỷ đăng xuất.");
        }
    }

    private boolean hasPermission (String role){
        return currentUserPermissions.contains(role);
    }

    private void setUpQuickAccess () {
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
        quickAccessViews.put("Cài đặt", this::openSettings);
    }
}