package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.position.PositionDropdownChoice;
import com.example.frontendquanlikhachsan.entity.position.ResponsePositionDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.staff.StaffDto;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.position.Position;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class StaffController {

    @FXML
    private TableView<ResponseStaffDto> tableStaff;
    @FXML
    private TableColumn<ResponseStaffDto, Integer> colId;
    @FXML
    private TableColumn<ResponseStaffDto, String> colFullName;
    @FXML
    private TableColumn<ResponseStaffDto, String> colPosition;
    @FXML
    private TableColumn<ResponseStaffDto, Sex> colSex;
    @FXML
    private TableColumn<ResponseStaffDto, Integer> colAge;
    @FXML
    private TableColumn<ResponseStaffDto, String> colUsername;

    @FXML
    private VBox detailPane;

    private ObservableList<ResponseStaffDto> staffList = FXCollections.observableArrayList();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String token = ""; // TODO: gán token thật ở đây

    @FXML
    public void initialize() {
        // --- Thiết lập TableView ---
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colFullName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getFullName()));
        colPosition.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPositionName()));
        colSex.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getSex()));
        colAge.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getAge()));
        colUsername.setCellValueFactory(cd -> {
            String uname = cd.getValue().getAccountUsername();
            return new ReadOnlyObjectWrapper<>( (uname == null || uname.isBlank()) ? "–" : uname );
        });

        loadStaffs();
        tableStaff.setItems(staffList);

        tableStaff.getSelectionModel().selectedItemProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                showStaffDetail(newS);
            }
        });
    }

    @FXML
    private void onCreateStaff(javafx.event.ActionEvent event) {
        showCreateForm();
    }

    // --- Load dữ liệu from API ---
    private void loadStaffs() {
        try {
            String json = ApiHttpClientCaller.call("staff", ApiHttpClientCaller.Method.GET, null, token);
            List<ResponseStaffDto> list = mapper.readValue(json, new TypeReference<>() {});
            staffList.clear();
            staffList.addAll(list);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải dữ liệu", "Không thể tải danh sách nhân viên từ server.");
        }
    }

    private void showStaffDetail(ResponseStaffDto staff) {
        detailPane.getChildren().clear();

        // Tiêu đề
        Label title = new Label("» Thông tin nhân viên – ID: " + staff.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        // GridPane hiển thị info cơ bản
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8));

        Function<String,Label> makeLabel = txt -> {
            Label lb = new Label(txt);
            lb.setStyle("-fx-font-weight:bold;");
            return lb;
        };

        grid.add(makeLabel.apply("Họ & Tên:"), 0, 0);
        grid.add(new Label(staff.getFullName()), 1, 0);

        grid.add(makeLabel.apply("Tuổi:"), 0, 1);
        grid.add(new Label(staff.getAge().toString()), 1, 1);

        grid.add(makeLabel.apply("CMND/CCCD:"), 0, 2);
        grid.add(new Label(Optional.ofNullable(staff.getIdentificationNumber()).orElse("–")), 1, 2);

        grid.add(makeLabel.apply("Địa chỉ:"), 0, 3);
        grid.add(new Label(Optional.ofNullable(staff.getAddress()).orElse("–")), 1, 3);

        grid.add(makeLabel.apply("Giới tính:"), 0, 4);
        grid.add(new Label(Optional.ofNullable(staff.getSex()).map(Sex::toString).orElse("–")), 1, 4);

        grid.add(makeLabel.apply("Hệ số lương:"), 0, 5);
        grid.add(new Label(Optional.ofNullable(staff.getSalaryMultiplier()).map(Object::toString).orElse("–")), 1, 5);

        grid.add(makeLabel.apply("Chức vụ:"), 0, 6);
        grid.add(new Label(Optional.ofNullable(staff.getPositionName()).orElse("–")), 1, 6);

        // Badge tài khoản
        grid.add(makeLabel.apply("Tài khoản:"), 0, 7);
        Label badge = new Label();
        badge.setStyle("-fx-padding:4 8; -fx-background-radius:4; -fx-text-fill:white;");
        if (staff.getAccountId() == null) {
            badge.setText("Chưa có tài khoản");
            badge.setStyle(badge.getStyle() + "-fx-background-color:#9e9e9e;");
        } else {
            badge.setText("Đã có tài khoản");
            badge.setStyle(badge.getStyle() + "-fx-background-color:#4caf50;");
        }
        grid.add(badge, 1, 7);

        // Accordion cho các list liên quan
        Accordion accordion = new Accordion();

        // Hóa đơn
        if (!staff.getInvoiceIds().isEmpty()) {
            ListView<String> lvInv = new ListView<>();
            lvInv.setFixedCellSize(24);
            for (Integer id : staff.getInvoiceIds()) {
                lvInv.getItems().add("Hóa đơn #" + id);
            }
            lvInv.setPrefHeight(lvInv.getItems().size() * lvInv.getFixedCellSize() + 2);
            TitledPane tpInv = new TitledPane("Hóa đơn", lvInv);
            accordion.getPanes().add(tpInv);
        }

        // Phiếu gia hạn thuê
        if (!staff.getRentalExtensionFormIds().isEmpty()) {
            ListView<String> lvExt = new ListView<>();
            lvExt.setFixedCellSize(24);
            for (Integer id : staff.getRentalExtensionFormIds()) {
                lvExt.getItems().add("Gia hạn thuê #" + id);
            }
            lvExt.setPrefHeight(lvExt.getItems().size() * lvExt.getFixedCellSize() + 2);
            TitledPane tpExt = new TitledPane("Phiếu gia hạn", lvExt);
            accordion.getPanes().add(tpExt);
        }

        // Phiếu thuê gốc
        if (!staff.getRentalFormIds().isEmpty()) {
            ListView<String> lvRent = new ListView<>();
            lvRent.setFixedCellSize(24);
            for (Integer id : staff.getRentalFormIds()) {
                lvRent.getItems().add("Phiếu thuê #" + id);
            }
            lvRent.setPrefHeight(lvRent.getItems().size() * lvRent.getFixedCellSize() + 2);
            TitledPane tpRent = new TitledPane("Phiếu thuê", lvRent);
            accordion.getPanes().add(tpRent);
        }

        // Thêm title, grid, accordion vào detailPane
        detailPane.getChildren().addAll(title, grid, accordion);

        // Nút Sửa / Xóa
        HBox actionBox = new HBox(12);
        actionBox.setPadding(new Insets(12, 0, 0, 0));
        Button btnEdit = new Button("✏️ Chỉnh sửa");
        btnEdit.setOnAction(evt -> showEditForm(staff));
        Button btnDelete = new Button("🗑️ Xóa");
        btnDelete.setOnAction(evt -> deleteStaff(staff));
        actionBox.getChildren().addAll(btnEdit, btnDelete);
        detailPane.getChildren().add(actionBox);
    }


    // --- Xóa nhân viên ---
    private void deleteStaff(ResponseStaffDto staff) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Xác nhận xóa");
        confirm.setContentText("Bạn có chắc muốn xóa nhân viên ID " + staff.getId() + " không?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                ApiHttpClientCaller.call("staff/" + staff.getId(), ApiHttpClientCaller.Method.DELETE, null, token);
                showInfoAlert("Xóa thành công", "Đã xóa nhân viên ID: " + staff.getId());
                loadStaffs();
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Lỗi xóa", "Không thể xóa nhân viên.");
            }
        }
    }

    // --- Form Chỉnh sửa nhân viên ---
    private void showEditForm(ResponseStaffDto staff) {
        detailPane.getChildren().clear();

        Label title = new Label("» Chỉnh sửa nhân viên – ID: " + staff.getId());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 8 0;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8));

        java.util.function.Function<String, Label> makeLabel = txt -> {
            Label lb = new Label(txt);
            lb.setStyle("-fx-font-weight: bold;");
            return lb;
        };

        // 1. Họ & Tên
        grid.add(makeLabel.apply("Họ & Tên:"), 0, 0);
        TextField tfFullName = new TextField(staff.getFullName());
        grid.add(tfFullName, 1, 0);

        // 2. Tuổi
        grid.add(makeLabel.apply("Tuổi:"), 0, 1);
        TextField tfAge = new TextField(String.valueOf(staff.getAge()));
        grid.add(tfAge, 1, 1);

        // 3. CMND/CCCD
        grid.add(makeLabel.apply("CMND/CCCD:"), 0, 2);
        TextField tfIdNum = new TextField(Optional.ofNullable(staff.getIdentificationNumber()).orElse(""));
        tfIdNum.setPromptText("12 số, không dấu cách");
        grid.add(tfIdNum, 1, 2);

        // 4. Địa chỉ
        grid.add(makeLabel.apply("Địa chỉ:"), 0, 3);
        TextField tfAddress = new TextField(Optional.ofNullable(staff.getAddress()).orElse(""));
        tfAddress.setPromptText("Ví dụ: Số 123, Phường X, Quận Y, Hà Nội");
        grid.add(tfAddress, 1, 3);

        // 5. Giới tính
        grid.add(makeLabel.apply("Giới tính:"), 0, 4);
        ComboBox<Sex> cbSex = new ComboBox<>(FXCollections.observableArrayList(Sex.values()));
        cbSex.setValue(Optional.ofNullable(staff.getSex()).orElse(null));
        grid.add(cbSex, 1, 4);

        // 6. Hệ số lương
        grid.add(makeLabel.apply("Hệ số lương:"), 0, 5);
        TextField tfSalaryMul = new TextField(Optional.ofNullable(staff.getSalaryMultiplier()).map(Object::toString).orElse(""));
        tfSalaryMul.setPromptText("Ví dụ: 1.2");
        grid.add(tfSalaryMul, 1, 5);

        // 7. Chức vụ
        grid.add(makeLabel.apply("Chức vụ:"), 0, 6);
        ComboBox<PositionDropdownChoice> cbPosition = new ComboBox<>(FXCollections.observableArrayList(getAllPositions()));
        for (PositionDropdownChoice p : cbPosition.getItems()) {
            if (p.getId() == staff.getPositionId()) {
                cbPosition.setValue(p);
                break;
            }
        }
        grid.add(cbPosition, 1, 6);

        HBox btnBox = new HBox(12);
        btnBox.setPadding(new Insets(12, 0, 0, 0));
        Button btnSave = new Button("💾 Lưu");
        Button btnCancel = new Button("❌ Hủy");
        btnCancel.setOnAction(evt -> showStaffDetail(staff));
        btnSave.setOnAction(evt -> {
            try {
                String fullName = tfFullName.getText().trim();
                Integer age = Integer.parseInt(tfAge.getText().trim());
                String idNum = tfIdNum.getText().trim();
                String address = tfAddress.getText().trim();
                Sex sex = cbSex.getValue();
                Float salaryMul = Float.parseFloat(tfSalaryMul.getText().trim());
                PositionDropdownChoice selPos = cbPosition.getValue();
                Integer positionId = (selPos != null ? selPos.getId() : null);

                StaffDto dto = StaffDto.builder()
                        .fullName(fullName)
                        .age(age)
                        .identificationNumber(idNum)
                        .address(address)
                        .sex(sex)
                        .salaryMultiplier(salaryMul)
                        .positionId(positionId)
                        .build();

                ApiHttpClientCaller.call("staff/" + staff.getId(), ApiHttpClientCaller.Method.PUT, dto, token);
                showInfoAlert("Cập nhật thành công", "Đã cập nhật ID: " + staff.getId());
                loadStaffs();
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Lỗi cập nhật", "Không thể cập nhật nhân viên.");
            }
        });

        btnBox.getChildren().addAll(btnSave, btnCancel);
        detailPane.getChildren().addAll(title, grid, btnBox);
    }

    // --- Form Tạo mới nhân viên ---
    private void showCreateForm() {
        detailPane.getChildren().clear();

        Label title = new Label("» Tạo mới nhân viên");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 8 0;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8));

        java.util.function.Function<String, Label> makeLabel = txt -> {
            Label lb = new Label(txt);
            lb.setStyle("-fx-font-weight: bold;");
            return lb;
        };

        // 1. Họ & Tên
        grid.add(makeLabel.apply("Họ & Tên:"), 0, 0);
        TextField tfFullName = new TextField();
        grid.add(tfFullName, 1, 0);

        // 2. Tuổi
        grid.add(makeLabel.apply("Tuổi:"), 0, 1);
        TextField tfAge = new TextField();
        grid.add(tfAge, 1, 1);

        // 3. CMND/CCCD
        grid.add(makeLabel.apply("CMND/CCCD:"), 0, 2);
        TextField tfIdNum = new TextField();
        tfIdNum.setPromptText("12 số, không dấu cách");
        grid.add(tfIdNum, 1, 2);

        // 4. Địa chỉ
        grid.add(makeLabel.apply("Địa chỉ:"), 0, 3);
        TextField tfAddress = new TextField();
        tfAddress.setPromptText("Ví dụ: Số 123, Phường X, Quận Y, Hà Nội");
        grid.add(tfAddress, 1, 3);

        // 5. Giới tính
        grid.add(makeLabel.apply("Giới tính:"), 0, 4);
        ComboBox<Sex> cbSex = new ComboBox<>(FXCollections.observableArrayList(Sex.values()));
        grid.add(cbSex, 1, 4);

//        // 6. Hệ số lương
//        grid.add(makeLabel.apply("Hệ số lương:"), 0, 5);
//        TextField tfSalaryMul = new TextField();
//        tfSalaryMul.setPromptText("VD: 1.2");
//        grid.add(tfSalaryMul, 1, 5);

        // 7. Chức vụ
        grid.add(makeLabel.apply("Chức vụ:"), 0, 5);
        ComboBox<PositionDropdownChoice> cbPosition = new ComboBox<>(FXCollections.observableArrayList(getAllPositions()));
        grid.add(cbPosition, 1, 5);

        HBox btnBox = new HBox(12);
        btnBox.setPadding(new Insets(12, 0, 0, 0));
        Button btnSave = new Button("💾 Lưu");
        Button btnCancel = new Button("❌ Hủy");
        btnCancel.setOnAction(evt -> detailPane.getChildren().clear());
        btnSave.setOnAction(evt -> {
            try {
                String fullName = tfFullName.getText().trim();
                Integer age = Integer.parseInt(tfAge.getText().trim());
                String idNum = tfIdNum.getText().trim();
                String address = tfAddress.getText().trim();
                Sex sex = cbSex.getValue();
//                Float salaryMul = Float.parseFloat(tfSalaryMul.getText().trim());
                PositionDropdownChoice selPos = cbPosition.getValue();
                Integer positionId = (selPos != null ? selPos.getId() : null);

                StaffDto dto = StaffDto.builder()
                        .fullName(fullName)
                        .age(age)
                        .identificationNumber(idNum)
                        .address(address)
                        .sex(sex)
//                        .salaryMultiplier(salaryMul) default 1.0
                        .positionId(positionId)
                        .accountId(null)
                        .build();

                ApiHttpClientCaller.call("staff", ApiHttpClientCaller.Method.POST, dto, token);
                showInfoAlert("Tạo thành công", "Đã thêm nhân viên mới.");
                loadStaffs();
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Lỗi tạo mới", "Không thể tạo nhân viên.");
            }
        });

        btnBox.getChildren().addAll(btnSave, btnCancel);
        detailPane.getChildren().addAll(title, grid, btnBox);
    }

    // --- Cung cấp danh sách Position (có thể đổi thành gọi API nếu có endpoint) ---
    private List<PositionDropdownChoice> getAllPositions() {
        try {
            // Gọi API GET /positions để lấy về List<ResponsePositionDto>
            String json = ApiHttpClientCaller.call("position", ApiHttpClientCaller.Method.GET, null, token);
            List<ResponsePositionDto> posList = mapper.readValue(json, new TypeReference<List<ResponsePositionDto>>() {});
            // Chuyển mỗi ResponsePositionDto thành đối tượng Position (chỉ map id + name)
            return posList.stream()
                    .map(rpd -> new PositionDropdownChoice(rpd.getId(), rpd.getName(), rpd.getBaseSalary()))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải Position", "Không thể lấy danh sách chức vụ từ server.");
            return List.of(); // trả về list rỗng nếu lỗi
        }
    }

    // --- Alert tiện ích ---
    private void showInfoAlert(String header, String content) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText(header);
        info.setContentText(content);
        info.showAndWait();
    }

    private void showErrorAlert(String header, String content) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText(header);
        error.setContentText(content);
        error.showAndWait();
    }
}
