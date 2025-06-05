package com.example.frontendquanlikhachsan.controllers.manager;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;

/**
 * Controller cho file StaffManager.fxml
 * Giả lập dữ liệu mẫu và binding vào TableView + Detail Pane.
 */
public class StaffController {

    // ==== LEFT: TableView và các cột ====
    @FXML
    private TableView<ResponseStaffDto> tableStaff;

    @FXML
    private TableColumn<ResponseStaffDto, Integer> colId;

    @FXML
    private TableColumn<ResponseStaffDto, String> colFullName;

    @FXML
    private TableColumn<ResponseStaffDto, Position> colPosition;

    @FXML
    private TableColumn<ResponseStaffDto, Sex> colSex;

    @FXML
    private TableColumn<ResponseStaffDto, Integer> colAge;

    @FXML
    private TableColumn<ResponseStaffDto, String> colAddress;

    @FXML
    private TableColumn<ResponseStaffDto, String> colUsername;

    // ==== RIGHT: Detail Pane (nơi sẽ thêm Label, Accordion, Button…) ====
    @FXML
    private VBox detailPane;

    // ObservableList chứa dữ liệu Staff (thực tế bạn có thể load từ service/database)
    private ObservableList<ResponseStaffDto> staffList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Thiết lập cellValueFactory cho các cột
        colId.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getId())
        );

        colFullName.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getFullName())
        );

        colPosition.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getPosition())
        );

        colSex.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getSex())
        );

        colAge.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getAge())
        );

        // colAddress: show short address (giả sử ResponseStaffDto có phương thức getShortAddress())
        colAddress.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getShortAddress())
        );

        // colUsername: nếu account null thì hiển thị "–"
        colUsername.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAccount() == null) {
                return new ReadOnlyObjectWrapper<>("–");
            } else {
                return new ReadOnlyObjectWrapper<>(cellData.getValue().getAccount().getUsername());
            }
        });

        // 2. Nạp dữ liệu mẫu vào staffList và setItems cho tableStaff
        staffList.addAll(getSampleData());
        tableStaff.setItems(staffList);

        // 3. Lắng nghe sự kiện chọn 1 dòng trong TableView
        tableStaff.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showStaffDetail(newSel);
            } else {
                clearDetailPane();
            }
        });
    }

    /**
     * Xoá sạch content của detailPane, hiển thị placeholder
     */
    private void clearDetailPane() {
        detailPane.getChildren().clear();
        Label placeholder = new Label("Chọn 1 nhân viên để xem chi tiết...");
        placeholder.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        detailPane.getChildren().add(placeholder);
    }

    /**
     * Khi user click vào 1 staff, hàm này build nội dung chi tiết lên detailPane.
     */
    private void showStaffDetail(ResponseStaffDto staff) {
        detailPane.getChildren().clear();

        // ===== SECTION 1: Thông tin cơ bản =====
        VBox basicBox = new VBox(6);
        basicBox.setStyle("-fx-border-color: #dedede; -fx-border-radius: 4px; -fx-border-width: 1;");
        basicBox.setPadding(new javafx.geometry.Insets(8));

        Label sec1 = new Label("» Thông tin cơ bản");
        sec1.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        basicBox.getChildren().add(sec1);

        basicBox.getChildren().add(new Label("ID: " + staff.getId()));
        basicBox.getChildren().add(new Label("Họ & Tên: " + staff.getFullName()));
        basicBox.getChildren().add(new Label("Tuổi: " + staff.getAge()));
        basicBox.getChildren().add(new Label("Giới tính: " + staff.getSex()));
        basicBox.getChildren().add(new Label("Địa chỉ: " + staff.getAddress()));
        basicBox.getChildren().add(new Label("Chức vụ: " + staff.getPosition()));
        basicBox.getChildren().add(new Label("Hệ số lương: " + staff.getSalaryMultiplier()));
        basicBox.getChildren().add(new Label("CMND/CCCD: " + staff.getIdentificationNumber()));

        // ===== SECTION 2: Thông tin Tài khoản =====
        VBox accountBox = new VBox(6);
        accountBox.setStyle("-fx-border-color: #dedede; -fx-border-radius: 4px; -fx-border-width: 1;");
        accountBox.setPadding(new javafx.geometry.Insets(8));

        Label sec2 = new Label("» Thông tin tài khoản");
        sec2.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        accountBox.getChildren().add(sec2);

        if (staff.getAccount() == null) {
            // Nếu chưa có account
            HBox row = new HBox(8);
            Label noAcc = new Label("Chưa có tài khoản");
            noAcc.setStyle("-fx-background-color: #e57373; -fx-text-fill: #fff; -fx-padding: 4 8; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            Button btnCreate = new Button("Tạo tài khoản");
            btnCreate.setOnAction(e -> {
                // TODO: chuyển đến form tạo account
                showInfoAlert("Tạo tài khoản", "Chuyển đến form tạo tài khoản...");
            });
            row.getChildren().addAll(noAcc, btnCreate);
            accountBox.getChildren().add(row);
        } else {
            // Nếu đã có account
            accountBox.getChildren().add(new Label("Username: " + staff.getAccount().getUsername()));
            accountBox.getChildren().add(new Label("Email: " + staff.getAccount().getEmail()));
            Button btnEditAcc = new Button("Chỉnh sửa tài khoản");
            btnEditAcc.setOnAction(e -> {
                // TODO: chuyển đến form chỉnh sửa account
                showInfoAlert("Chỉnh sửa tài khoản", "Chuyển đến form chỉnh sửa tài khoản...");
            });
            accountBox.getChildren().add(btnEditAcc);
        }

        // ===== SECTION 3: Accordion cho các list (invoiceIds, rentalExtensionFormIds, rentalFormIds) =====
        VBox relatedBox = new VBox(6);
        relatedBox.setStyle("-fx-border-color: #dedede; -fx-border-radius: 4px; -fx-border-width: 1;");
        relatedBox.setPadding(new javafx.geometry.Insets(8));

        Label sec3 = new Label("» Hoá đơn & Phiếu thuê");
        sec3.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        relatedBox.getChildren().add(sec3);

        Accordion accordion = new Accordion();

        // Hoá đơn
        TitledPane tpInvoices = createListTitledPane(
                "📄 Danh sách Hoá đơn",
                staff.getInvoiceIds(),
                "Invoice"
        );
        // Phiếu gia hạn
        TitledPane tpExtensions = createListTitledPane(
                "🔄 Danh sách Phiếu gia hạn",
                staff.getRentalExtensionFormIds(),
                "Rental Extension"
        );
        // Phiếu thuê
        TitledPane tpRentals = createListTitledPane(
                "🏠 Danh sách Phiếu thuê",
                staff.getRentalFormIds(),
                "Rental Form"
        );

        accordion.getPanes().addAll(tpInvoices, tpExtensions, tpRentals);
        // Mở mặc định pane đầu tiên
        if (!accordion.getPanes().isEmpty()) {
            accordion.setExpandedPane(accordion.getPanes().get(0));
        }

        relatedBox.getChildren().add(accordion);

        // ===== SECTION 4: Action Buttons (Quay lại, Chỉnh sửa nhân viên) =====
        HBox actionBox = new HBox(10);
        actionBox.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));

        Button btnBack = new Button("« Quay lại");
        btnBack.setOnAction(e -> tableStaff.getSelectionModel().clearSelection());

        Button btnEditStaff = new Button("Chỉnh sửa nhân viên");
        btnEditStaff.setOnAction(e -> {
            // TODO: chuyển đến form edit staff
            showInfoAlert("Chỉnh sửa nhân viên", "Chuyển đến form chỉnh sửa nhân viên...");
        });

        actionBox.getChildren().addAll(btnBack, btnEditStaff);

        // ==== Cuối cùng, thêm tất cả các section vào detailPane ====
        detailPane.getChildren().addAll(basicBox, accountBox, relatedBox, actionBox);
    }

    /**
     * Tạo 1 TitledPane với nội dung là danh sách các ID (List<Integer>).
     * @param title  Tiêu đề (ví dụ "📄 Danh sách Hoá đơn")
     * @param ids    List<Integer> ID tương ứng
     * @param prefix Tiền tố khi hiển thị (ví dụ "Invoice" -> link "Invoice #101")
     * @return TitledPane đã build sẵn nội dung
     */
    private TitledPane createListTitledPane(String title, List<Integer> ids, String prefix) {
        VBox box = new VBox(4);
        box.setPadding(new javafx.geometry.Insets(6));

        if (ids == null || ids.isEmpty()) {
            Label empty = new Label("Chưa có mục nào");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
            box.getChildren().add(empty);
        } else {
            for (Integer id : ids) {
                Hyperlink link = new Hyperlink(prefix + " #" + id);
                link.setOnAction(e -> {
                    // TODO: xử lý khi click vào link, ví dụ mở chi tiết Invoice/Rental...
                    showInfoAlert("Xem chi tiết", "Bạn chọn: " + prefix + " #" + id);
                });
                box.getChildren().add(link);
            }
        }

        TitledPane tp = new TitledPane(title + " (" + (ids == null ? 0 : ids.size()) + ")", box);
        tp.setAnimated(true);
        return tp;
    }

    /**
     * Hiển thị Alert thông tin (dùng để mock/nhiệm vụ TODO).
     */
    private void showInfoAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Tạo dữ liệu mẫu (chỉ demo). Thay thế bằng load thực tế khi triển khai.
     */
    private List<ResponseStaffDto> getSampleData() {
        // Giả lập Account
        Account accB = new Account("tranthib", "btran@example.com");
        Account accC = new Account("levanc",    "clev@example.com");

        return Arrays.asList(
                new ResponseStaffDto(
                        1,
                        "Nguyễn Văn A",
                        30,
                        "012345678",
                        "Số 123, Phường X, Quận Y, Hà Nội",
                        Sex.MALE,
                        1.5f,
                        Position.QUAN_LY,
                        null,  // chưa có account
                        Arrays.asList(101, 102),
                        Arrays.asList(201),
                        Arrays.asList(301, 302)
                ),
                new ResponseStaffDto(
                        2,
                        "Trần Thị B",
                        25,
                        "098765432",
                        "Số 456, Phường Z, Quận W, TP. Hồ Chí Minh",
                        Sex.FEMALE,
                        1.2f,
                        Position.NHAN_VIEN,
                        accB,
                        FXCollections.observableArrayList(), // no invoice
                        FXCollections.observableArrayList(), // no extension
                        Arrays.asList(401)                   // 1 rental
                ),
                new ResponseStaffDto(
                        3,
                        "Lê Văn C",
                        28,
                        "023456789",
                        "Số 789, Phường A, Quận B, Đà Nẵng",
                        Sex.MALE,
                        1.0f,
                        Position.KI_THUAT,
                        accC,
                        Arrays.asList(103),
                        Arrays.asList(202, 203),
                        FXCollections.observableArrayList() // no rental
                )
        );
    }
}
