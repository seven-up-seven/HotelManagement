package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.entity.account.Account;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.position.Position;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;

/**
 * Controller cho file StaffManager.fxml
 * Giữ nguyên cách seeding mẫu và thêm chức năng “Tạo Nhân viên” (Create) vào detailPane.
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

    // ==== RIGHT: Detail Pane (nơi sẽ hiển thị detail hoặc form tạo mới) ====
    @FXML
    private VBox detailPane;

    // ObservableList chứa dữ liệu Staff (giữ cách seeding hiện tại)
    private ObservableList<ResponseStaffDto> staffList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Thiết lập cellValueFactory cho các cột
        colId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        colFullName.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getFullName()));
        colPosition.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPosition()));
        colSex.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSex()));
        colAge.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAge()));
        // colAddress: show full address (theo seeding)
        colAddress.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getAddress()));
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

        // 4. Khi lần đầu load, show placeholder
        clearDetailPane();
    }

    /**
     * Được gọi khi user bấm nút “Tạo nhân viên mới” (trên danh sách).
     * Hiển thị form tạo _nhân viên_ (tương tự showCreateStaffForm cũ).
     */
    @FXML
    public void onCreateStaff() {
        showCreateStaffForm();
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
        basicBox.setPadding(new Insets(8));

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
        accountBox.setPadding(new Insets(8));

        Label sec2 = new Label("» Thông tin tài khoản");
        sec2.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        accountBox.getChildren().add(sec2);

        if (staff.getAccount() == null) {
            // Nếu chưa có account → hiển thị nút “Tạo tài khoản”
            HBox row = new HBox(8);
            Label noAcc = new Label("Chưa có tài khoản");
            noAcc.setStyle("-fx-background-color: #e57373; -fx-text-fill: #fff; -fx-padding: 4 8; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            Button btnCreate = new Button("Tạo tài khoản");
            btnCreate.setOnAction(e -> showCreateAccountForm(staff));
            row.getChildren().addAll(noAcc, btnCreate);
            accountBox.getChildren().add(row);
        } else {
            // Nếu đã có account
            accountBox.getChildren().add(new Label("Username: " + staff.getAccount().getUsername()));
            accountBox.getChildren().add(new Label("Password: " + staff.getAccount().getPassword()));
            //không cho sửa, admin mới cho sửa xoá tài khoản
//            Button btnEditAcc = new Button("Chỉnh sửa tài khoản");
//            btnEditAcc.setOnAction(e -> showInfoAlert("Chỉnh sửa tài khoản", "Chức năng chỉnh sửa tài khoản sẽ triển khai sau..."));
//            accountBox.getChildren().add(btnEditAcc);
        }

        // ===== SECTION 3: Accordion cho các list (invoiceIds, rentalExtensionFormIds, rentalFormIds) =====
        VBox relatedBox = new VBox(6);
        relatedBox.setStyle("-fx-border-color: #dedede; -fx-border-radius: 4px; -fx-border-width: 1;");
        relatedBox.setPadding(new Insets(8));

        Label sec3 = new Label("» Hoá đơn & Phiếu thuê");
        sec3.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        relatedBox.getChildren().add(sec3);

        Accordion accordion = new Accordion();
        TitledPane tpInvoices = createListTitledPane("📄 Danh sách Hoá đơn", staff.getInvoiceIds(), "Invoice");
        TitledPane tpExtensions = createListTitledPane("🔄 Danh sách Phiếu gia hạn", staff.getRentalExtensionFormIds(), "Rental Extension");
        TitledPane tpRentals = createListTitledPane("🏠 Danh sách Phiếu thuê", staff.getRentalFormIds(), "Rental Form");
        accordion.getPanes().addAll(tpInvoices, tpExtensions, tpRentals);
        if (!accordion.getPanes().isEmpty()) {
            accordion.setExpandedPane(accordion.getPanes().get(0));
        }
        relatedBox.getChildren().add(accordion);

        // ===== SECTION 4: Action Buttons (Quay lại, Chỉnh sửa nhân viên) =====
        HBox actionBox = new HBox(10);
        actionBox.setPadding(new Insets(8, 0, 0, 0));
        Button btnBack = new Button("« Quay lại");
        btnBack.setOnAction(e -> tableStaff.getSelectionModel().clearSelection());
        Button btnEditStaff = new Button("Chỉnh sửa nhân viên");
        btnEditStaff.setOnAction(e -> showEditForm(staff));
        actionBox.getChildren().addAll(btnBack, btnEditStaff);

        // Thêm vào detailPane
        detailPane.getChildren().addAll(basicBox, accountBox, relatedBox, actionBox);
    }

    /**
     * Hiển thị form Chỉnh sửa nhân viên (prefill data từ staff đã chọn).
     */
    private void showEditForm(ResponseStaffDto staff) {
        detailPane.getChildren().clear();

        Label title = new Label("» Chỉnh sửa nhân viên - ID: " + staff.getId());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(8));

        // 1. Họ & Tên
        Label lblFullName = new Label("Họ & Tên:");
        TextField tfFullName = new TextField(staff.getFullName());
        grid.add(lblFullName, 0, 0);
        grid.add(tfFullName,  1, 0);

        // 2. Tuổi
        Label lblAge = new Label("Tuổi:");
        TextField tfAge = new TextField(String.valueOf(staff.getAge()));
        grid.add(lblAge,  0, 1);
        grid.add(tfAge,   1, 1);

        // 3. CMND/CCCD
        Label lblIdNum = new Label("CMND/CCCD:");
        TextField tfIdNum = new TextField(staff.getIdentificationNumber());
        grid.add(lblIdNum, 0, 2);
        grid.add(tfIdNum,  1, 2);

        // 4. Địa chỉ
        Label lblAddress = new Label("Địa chỉ:");
        TextField tfAddress = new TextField(staff.getAddress());
        grid.add(lblAddress, 0, 3);
        grid.add(tfAddress,  1, 3);

        // 5. Giới tính
        Label lblSex = new Label("Giới tính:");
        ComboBox<Sex> cbSex = new ComboBox<>(FXCollections.observableArrayList(Sex.values()));
        cbSex.setValue(staff.getSex());
        grid.add(lblSex, 0, 4);
        grid.add(cbSex,  1, 4);

        // 6. Hệ số lương
        Label lblSalaryMul = new Label("Hệ số lương:");
        TextField tfSalaryMul = new TextField(String.valueOf(staff.getSalaryMultiplier()));
        grid.add(lblSalaryMul, 0, 5);
        grid.add(tfSalaryMul,  1, 5);

        // 7. Chức vụ
        Label lblPosition = new Label("Chức vụ:");
        ComboBox<Position> cbPosition = new ComboBox<>(FXCollections.observableArrayList(getAllPositions()));
        cbPosition.setValue(staff.getPosition());
        grid.add(lblPosition, 0, 6);
        grid.add(cbPosition,  1, 6);

        // 8. Account ID
        Label lblAccountId = new Label("Account ID:");
        TextField tfAccountId = new TextField();
        if (staff.getAccount() != null) {
            tfAccountId.setText(String.valueOf(staff.getAccount().getId()));
        }
        tfAccountId.setPromptText("VD: 10 (thả trống nếu không)");
        grid.add(lblAccountId, 0, 7);
        grid.add(tfAccountId,  1, 7);

        // Nút “Lưu” + “Hủy”
        HBox btnBox = new HBox(10);
        btnBox.setPadding(new Insets(8, 0, 0, 0));
        Button btnSave = new Button("Lưu");
        Button btnCancel = new Button("Hủy");

        // Hủy → quay về hiển thị detail cũ
        btnCancel.setOnAction(e -> showStaffDetail(staff));

        // Lưu → validate + cập nhật staff (trong ObservableList)
        btnSave.setOnAction(e -> {
            // 1. Lấy dữ liệu từ form
            String fullName = tfFullName.getText().trim();
            String ageStr   = tfAge.getText().trim();
            String idNum    = tfIdNum.getText().trim();
            String address  = tfAddress.getText().trim();
            Sex sex         = cbSex.getValue();
            String salaryMulStr = tfSalaryMul.getText().trim();
            Position position   = cbPosition.getValue();
            String accountIdStr = tfAccountId.getText().trim();

            // 2. Validate
            if (fullName.isEmpty() || ageStr.isEmpty() || idNum.isEmpty() ||
                    address.isEmpty()  || sex == null   || salaryMulStr.isEmpty() || position == null) {
                showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập đầy đủ các trường bắt buộc!");
                return;
            }

            // 3. Chặn trùng fullName (nếu tên mới đã có ở staff khác)
            boolean exists = staffList.stream()
                    .filter(s -> s.getId() != staff.getId())
                    .anyMatch(s -> s.getFullName().equalsIgnoreCase(fullName));
            if (exists) {
                showErrorAlert("Trùng tên", "Đã có nhân viên tên \"" + fullName + "\" rồi!");
                return;
            }

            Integer age;
            Float salaryMul;
            Integer accountId = null;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                showErrorAlert("Sai định dạng", "Tuổi phải là số nguyên.");
                return;
            }
            try {
                salaryMul = Float.parseFloat(salaryMulStr);
            } catch (NumberFormatException ex) {
                showErrorAlert("Sai định dạng", "Hệ số lương phải là số (float).");
                return;
            }
            if (!accountIdStr.isEmpty()) {
                try {
                    accountId = Integer.parseInt(accountIdStr);
                } catch (NumberFormatException ex) {
                    showErrorAlert("Sai định dạng", "Account ID phải là số nguyên hoặc để trống.");
                    return;
                }
            }

            // 4. Cập nhật lại staff
            staff.setFullName(fullName);
            staff.setAge(age);
            staff.setIdentificationNumber(idNum);
            staff.setAddress(address);
            staff.setSex(sex);
            staff.setSalaryMultiplier(salaryMul);
            staff.setPosition(position);

            if (accountId != null) {
                Account acc = staff.getAccount();
                if (acc == null) {
                    acc = new Account();
                    acc.setId(accountId);
                    acc.setUsername("user" + accountId);
                    acc.setPassword("pass" + accountId);
                    staff.setAccount(acc);
                } else {
                    acc.setId(accountId);
                    acc.setUsername("user" + accountId);
                    acc.setPassword("pass" + accountId);
                }
            } else {
                staff.setAccount(null);
            }

            // 5. Refresh table và show detail mới
            tableStaff.refresh();
            showStaffDetail(staff);
            showInfoAlert("Cập nhật thành công", "Thông tin nhân viên đã được cập nhật.");
        });

        btnBox.getChildren().addAll(btnSave, btnCancel);
        detailPane.getChildren().addAll(title, grid, btnBox);
    }


    /**
     * Khi nhấn “Tạo tài khoản” → hiển thị form Create ngay trong detailPane.
     * Form này chứa input cho các trường:
     *   - fullName, age, identificationNumber, address, sex, salaryMultiplier, position, accountId
     */
    private void showCreateStaffForm() {
        detailPane.getChildren().clear();

        Label title = new Label("» Tạo mới nhân viên");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // GridPane để sắp xếp label + input
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(8));

        // 1. Họ & Tên
        Label lblFullName = new Label("Họ & Tên:");
        TextField tfFullName = new TextField();
        grid.add(lblFullName, 0, 0);
        grid.add(tfFullName, 1, 0);

        // 2. Tuổi
        Label lblAge = new Label("Tuổi:");
        TextField tfAge = new TextField();
        tfAge.setPromptText("VD: 30");
        grid.add(lblAge, 0, 1);
        grid.add(tfAge, 1, 1);

        // 3. CMND/CCCD
        Label lblIdNum = new Label("CMND/CCCD:");
        TextField tfIdNum = new TextField();
        grid.add(lblIdNum, 0, 2);
        grid.add(tfIdNum, 1, 2);

        // 4. Địa chỉ
        Label lblAddress = new Label("Địa chỉ:");
        TextField tfAddress = new TextField();
        tfAddress.setPromptText("Ví dụ: Số 123, Phường X, Quận Y, Hà Nội");
        grid.add(lblAddress, 0, 3);
        grid.add(tfAddress, 1, 3);

        // 5. Giới tính (ComboBox)
        Label lblSex = new Label("Giới tính:");
        ComboBox<Sex> cbSex = new ComboBox<>(FXCollections.observableArrayList(Sex.values()));
        cbSex.setPromptText("Chọn giới tính");
        grid.add(lblSex, 0, 4);
        grid.add(cbSex, 1, 4);

        // 6. Hệ số lương
        Label lblSalaryMul = new Label("Hệ số lương:");
        TextField tfSalaryMul = new TextField();
        tfSalaryMul.setPromptText("VD: 1.2");
        grid.add(lblSalaryMul, 0, 5);
        grid.add(tfSalaryMul, 1, 5);

        // 7. Chức vụ (ComboBox Position)
        Label lblPosition = new Label("Chức vụ:");
        ComboBox<Position> cbPosition = new ComboBox<>(FXCollections.observableArrayList(getAllPositions()));
        cbPosition.setPromptText("Chọn chức vụ");
        grid.add(lblPosition, 0, 6);
        grid.add(cbPosition, 1, 6);

        // 8. Account ID (nếu muốn gán account đã có)
        Label lblAccountId = new Label("Account ID:");
        TextField tfAccountId = new TextField();
        tfAccountId.setPromptText("VD: 10 (hoặc để trống)");
        grid.add(lblAccountId, 0, 7);
        grid.add(tfAccountId, 1, 7);

        // Nút “Lưu” và “Hủy”
        HBox btnBox = new HBox(10);
        btnBox.setPadding(new Insets(8, 0, 0, 0));
        Button btnSave = new Button("Lưu");
        Button btnCancel = new Button("Hủy");

        // Hủy → quay về placeholder
        btnCancel.setOnAction(e -> clearDetailPane());

        // Lưu → validate + thêm staff mới vào staffList
        btnSave.setOnAction(e -> {
            // 1. Lấy dữ liệu từ form
            String fullName = tfFullName.getText().trim();
            String ageStr = tfAge.getText().trim();
            String idNum = tfIdNum.getText().trim();
            String address = tfAddress.getText().trim();
            Sex sex = cbSex.getValue();
            String salaryMulStr = tfSalaryMul.getText().trim();
            Position position = cbPosition.getValue();
            String accountIdStr = tfAccountId.getText().trim();

            // 2. Validate đơn giản
            if (fullName.isEmpty() || ageStr.isEmpty() || idNum.isEmpty() ||
                    address.isEmpty() || sex == null || salaryMulStr.isEmpty() || position == null) {
                showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập đầy đủ các trường bắt buộc!");
                return;
            }

            Integer age;
            Float salaryMul;
            Integer accountId = null;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                showErrorAlert("Sai định dạng", "Tuổi phải là số nguyên.");
                return;
            }
            try {
                salaryMul = Float.parseFloat(salaryMulStr);
            } catch (NumberFormatException ex) {
                showErrorAlert("Sai định dạng", "Hệ số lương phải là số (float).");
                return;
            }
            if (!accountIdStr.isEmpty()) {
                try {
                    accountId = Integer.parseInt(accountIdStr);
                } catch (NumberFormatException ex) {
                    showErrorAlert("Sai định dạng", "Account ID phải là số nguyên hoặc để trống.");
                    return;
                }
            }

            // 3. Tạo Account nếu có accountId (mock)
            Account acc = null;
            if (accountId != null) {
                // Bạn có thể load Account thực từ DB theo accountId
                acc = new Account();
                acc.setId(accountId);
                acc.setUsername("user" + accountId);
                acc.setPassword("pass" + accountId);
            }

            // 4. Tạo các danh sách rỗng cho invoice, extension, rental
            List<Integer> invs = FXCollections.observableArrayList();
            List<Integer> exts = FXCollections.observableArrayList();
            List<Integer> rents = FXCollections.observableArrayList();

            // 5. Lấy ID mới = max(ID) + 1
            int newId = staffList.stream().mapToInt(ResponseStaffDto::getId).max().orElse(0) + 1;
            ResponseStaffDto newStaff = new ResponseStaffDto(
                    newId,
                    fullName,
                    age,
                    idNum,
                    address,
                    sex,
                    salaryMul,
                    position,
                    acc,
                    invs,
                    exts,
                    rents
            );

            // 6. Thêm vào staffList → TableView tự refresh
            staffList.add(newStaff);

            // 7. Chọn luôn staff mới để show detail
            tableStaff.getSelectionModel().select(newStaff);

            // 8. Thông báo thành công
            showInfoAlert("Tạo thành công", "Đã thêm nhân viên mới: " + fullName);
        });

        btnBox.getChildren().addAll(btnSave, btnCancel);

        // Đổ title + grid + btnBox vào detailPane
        detailPane.getChildren().addAll(title, grid, btnBox);
    }

    /**
     * Tạo 1 TitledPane với nội dung là danh sách các ID (List<Integer>).
     */
    private TitledPane createListTitledPane(String title, List<Integer> ids, String prefix) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(6));

        if (ids == null || ids.isEmpty()) {
            Label empty = new Label("Chưa có mục nào");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
            box.getChildren().add(empty);
        } else {
            for (Integer id : ids) {
                Hyperlink link = new Hyperlink(prefix + " #" + id);
                link.setOnAction(e -> showInfoAlert("Xem chi tiết", "Bạn chọn: " + prefix + " #" + id));
                box.getChildren().add(link);
            }
        }

        TitledPane tp = new TitledPane(title + " (" + (ids == null ? 0 : ids.size()) + ")", box);
        tp.setAnimated(true);
        return tp;
    }

    /**
     * Hiển thị Alert thông tin (dùng để mock).
     */
    private void showInfoAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Hiển thị Alert lỗi (dùng để mock).
     */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Dữ liệu mẫu để demo (giữ nguyên cách seeding hiện tại).
     */
    private List<ResponseStaffDto> getSampleData() {
        // Giả lập Account
        Account accB = new Account();
        accB.setId(10);
        accB.setUsername("accB");
        accB.setPassword("passwordB");
        Account accC = new Account();
        accC.setId(11);
        accC.setUsername("accC");
        accC.setPassword("passwordC");

        // Giả lập Position
        Position positionNhanVien = new Position();
        positionNhanVien.setId(1);
        positionNhanVien.setName("Nhân viên");
        Position positionKyThuat = new Position();
        positionKyThuat.setId(2);
        positionKyThuat.setName("Kỹ thuật");

        return Arrays.asList(
                new ResponseStaffDto(
                        1,
                        "Nguyễn Văn A",
                        30,
                        "012345678",
                        "Số 123, Phường X, Quận Y, Hà Nội",
                        Sex.MALE,
                        1.5f,
                        positionNhanVien,
                        null,              // chưa có account
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
                        positionKyThuat,
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
                        positionNhanVien,
                        accC,
                        Arrays.asList(103),
                        Arrays.asList(202, 203),
                        FXCollections.observableArrayList()  // no rental
                )
        );
    }

    /**
     * Trả về tất cả Position (giả lập).
     * Nếu bạn có service thực, hãy thay thế bằng call service.
     */
    private List<Position> getAllPositions() {
        Position positionQL = new Position();
        positionQL.setId(3);
        positionQL.setName("Quản lý");
        Position positionNV = new Position();
        positionNV.setId(1);
        positionNV.setName("Nhân viên");
        Position positionKT = new Position();
        positionKT.setId(2);
        positionKT.setName("Kỹ thuật");
        return Arrays.asList(positionQL, positionNV, positionKT);
    }

    /**
     * Hiển thị form Tạo tài khoản (userName, passWord, userRoleId) ngay trong detailPane.
     * staff đã chọn sẽ được gán Account mới sau khi lưu.
     */
    private void showCreateAccountForm(ResponseStaffDto staff) {
        detailPane.getChildren().clear();

        Label title = new Label("» Tạo tài khoản cho: " + staff.getFullName());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(8));

        // 1. userName
        Label lblUserName = new Label("Username:");
        TextField tfUserName = new TextField();
        tfUserName.setPromptText("Nhập username");
        grid.add(lblUserName, 0, 0);
        grid.add(tfUserName, 1, 0);

        // 2. passWord
        Label lblPassword = new Label("Password:");
        PasswordField pfPassword = new PasswordField();
        pfPassword.setPromptText("Nhập password");
        grid.add(lblPassword, 0, 1);
        grid.add(pfPassword, 1, 1);

        // 3. userRoleId (ComboBox nếu bạn có danh sách role)
        Label lblRoleId = new Label("Role ID:");
        ComboBox<Integer> cbRoleId = new ComboBox<>(FXCollections.observableArrayList(
                1, 2, 3  // giả định: 1=Admin, 2=Manager, 3=Staff (bạn điều chỉnh theo model thực)
        ));
        cbRoleId.setPromptText("Chọn Role ID");
        grid.add(lblRoleId, 0, 2);
        grid.add(cbRoleId, 1, 2);

        // Nút “Lưu” và “Hủy”
        HBox btnBox = new HBox(10);
        btnBox.setPadding(new Insets(8, 0, 0, 0));
        Button btnSave = new Button("Lưu");
        Button btnCancel = new Button("Hủy");

        // Hủy → quay lại detail cũ (chưa có account)
        btnCancel.setOnAction(e -> showStaffDetail(staff));

        // Lưu → validate + gán Account cho staff
        btnSave.setOnAction(e -> {
            String userName = tfUserName.getText().trim();
            String passWord = pfPassword.getText().trim();
            Integer roleId   = cbRoleId.getValue();

            if (userName.isEmpty() || passWord.isEmpty() || roleId == null) {
                showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập đầy đủ Username, Password và Role ID!");
                return;
            }

            // 1. Kiểm tra username trùng nếu cần (nếu bạn muốn)
            boolean exists = staffList.stream()
                    .map(ResponseStaffDto::getAccount)
                    .filter(acc -> acc != null)
                    .anyMatch(acc -> acc.getUsername().equalsIgnoreCase(userName));
            if (exists) {
                showErrorAlert("Trùng tên tài khoản", "Username \"" + userName + "\" đã tồn tại, vui lòng chọn khác.");
                return;
            }

            // 2. Tạo Account và gán vào staff
            Account newAcc = new Account();
            newAcc.setUsername(userName);
            newAcc.setPassword(passWord);
            newAcc.setUserRoleId(roleId);
            // Giả định bạn có setter setId() hoặc hệ thống auto gán ID
            // newAcc.setId(autoGeneratedId);

            staff.setAccount(newAcc);

            // 3. Refresh TableView để hiển thị cột Username mới
            tableStaff.refresh();

            // 4. Quay về show detail (giờ đã có account)
            showStaffDetail(staff);

            showInfoAlert("Tạo tài khoản thành công", "Đã gán tài khoản cho " + staff.getFullName());
        });

        btnBox.getChildren().addAll(btnSave, btnCancel);

        detailPane.getChildren().addAll(title, grid, btnBox);
    }

}

