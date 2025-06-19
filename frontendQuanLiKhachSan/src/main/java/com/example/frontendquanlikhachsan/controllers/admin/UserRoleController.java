package com.example.frontendquanlikhachsan.controllers.admin;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.permission.Permission;
import com.example.frontendquanlikhachsan.entity.permission.ResponsePermissionDto;
import com.example.frontendquanlikhachsan.entity.userRole.ResponseUserRoleDto;
import com.example.frontendquanlikhachsan.entity.userRole.UserRoleDto;
import com.example.frontendquanlikhachsan.entity.userRolePermission.UserRolePermissionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserRoleController {
    @FXML
    private TableView<ResponseUserRoleDto> tableUserRole;
    @FXML
    private TableColumn<ResponseUserRoleDto, Integer> colRoleId;
    @FXML
    private TableColumn<ResponseUserRoleDto, String> colRoleName;
    @FXML
    private TableColumn<ResponseUserRoleDto, String> colPermissions;

    @FXML
    private VBox roleDetailContainer;
    @FXML
    private Label labelRoleDetailTitle;
    @FXML
    private Button btnCreateRole;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        colRoleId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colRoleName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colPermissions.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.join(", ", data.getValue().getPermissionNames())));

        loadRoles();

        tableUserRole.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showRoleDetails(newSel);
            }
        });

        btnCreateRole.setOnAction(e -> showCreateRoleForm());
    }

    private void loadRoles() {
        try {
            String json = ApiHttpClientCaller.call("user-role", ApiHttpClientCaller.Method.GET, null);
            List<ResponseUserRoleDto> roles = Arrays.asList(ApiHttpClientCaller.mapper.readValue(json, ResponseUserRoleDto[].class));
            tableUserRole.getItems().setAll(roles);
        } catch (Exception e) {
            showError("Không thể tải vai trò: " + e.getMessage());
        }
    }

    private void showRoleDetails(ResponseUserRoleDto role) {
        labelRoleDetailTitle.setText("Chi tiết Vai trò: " + role.getName());
        roleDetailContainer.getChildren().clear();
        // Tên vai trò
        Label nameLabel = new Label("Tên vai trò:");
        TextField nameField = new TextField(role.getName());

        // Danh sách quyền (hiển thị bằng nhiều ComboBox)
        Label permissionLabel = new Label("Quyền:");
        VBox permissionBox = new VBox(5);
        for (String perm : role.getPermissionNames()) {
            ComboBox<String> comboBox = new ComboBox<>();
            try {
                var json=ApiHttpClientCaller.call("permission", ApiHttpClientCaller.Method.GET, null);

                ResponsePermissionDto[] permissions = mapper.readValue(json, ResponsePermissionDto[].class);

                for (ResponsePermissionDto p : permissions) {
                    comboBox.getItems().add(p.getName());
                }

                comboBox.setValue(perm); // gán giá trị hiện tại
                permissionBox.getChildren().add(comboBox);
            }
            catch (Exception e) {
                showError("Không thể lấy danh sách quyền");
            }
        }

        // Nút cập nhật và xoá
        Button updateBtn = new Button("✔ Cập nhật");
        Button deleteBtn = new Button("🗑 Xoá");
        updateBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        HBox btnBox = new HBox(10, updateBtn, deleteBtn);

        // Thêm tất cả vào form
        roleDetailContainer.getChildren().addAll(nameLabel, nameField, permissionLabel, permissionBox, btnBox);

        deleteBtn.setOnAction(actionEvent->{

        });
    }

    private void showCreateRoleForm() {
        labelRoleDetailTitle.setText("Tạo Vai trò mới");
        roleDetailContainer.getChildren().clear();

        Label nameLabel = new Label("Tên vai trò:");
        TextField nameField = new TextField();
        nameField.setPromptText("Nhập tên vai trò");

        Label permissionLabel = new Label("Quyền:");
        VBox permissionBox = new VBox(5);

        // Tải toàn bộ quyền 1 lần
        ResponsePermissionDto[] allPermissions;
        try {
            String json = ApiHttpClientCaller.call("permission", ApiHttpClientCaller.Method.GET, null);
            allPermissions = ApiHttpClientCaller.mapper.readValue(json, ResponsePermissionDto[].class);
        } catch (Exception ex) {
            showError("Không thể tải danh sách quyền: " + ex.getMessage());
            return;
        }

        // Thêm mới một ComboBox và gắn listener
        Runnable addPermissionComboBox = () -> {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().setAll(Arrays.stream(allPermissions).map(ResponsePermissionDto::getName).toList());

            comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) return;

                boolean isDuplicate = permissionBox.getChildren().stream()
                        .filter(node -> node instanceof HBox hBox && hBox.getChildren().get(0) instanceof ComboBox<?> && hBox.getChildren().get(0) != comboBox)
                        .map(node -> ((ComboBox<?>) ((HBox) node).getChildren().get(0)).getValue())
                        .anyMatch(val -> newVal.equals(val));

                if (isDuplicate) {
                    showError("Không thể chọn trùng quyền đã có.");
                    comboBox.setValue(null);
                }
            });

            // Nút xóa quyền
            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
            removeBtn.setOnAction(e -> permissionBox.getChildren().remove(comboBox.getParent()));

            // Gói ComboBox + Button vào HBox
            HBox comboRow = new HBox(8, comboBox, removeBtn);
            permissionBox.getChildren().add(comboRow);
        };

        Button addPermissionBtn = new Button("➕ Thêm quyền");
        addPermissionBtn.setOnAction(e -> addPermissionComboBox.run());

        // Gọi một lần đầu tiên
        addPermissionComboBox.run();

        Button saveBtn = new Button("💾 Lưu");
        saveBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");

        roleDetailContainer.getChildren().addAll(nameLabel, nameField, permissionLabel, permissionBox, addPermissionBtn, saveBtn);

        //create new user role
        saveBtn.setOnAction(evt -> {
            String roleName = nameField.getText().trim();

            if (roleName.isEmpty()) {
                showError("Tên vai trò không được để trống.");
                return;
            }

            List<String> selectedPermissions = permissionBox.getChildren().stream()
                    .filter(node -> node instanceof HBox hBox && hBox.getChildren().get(0) instanceof ComboBox<?>)
                    .map(node -> ((ComboBox<?>) ((HBox) node).getChildren().get(0)).getValue())
                    .filter(val -> val != null && !val.toString().isBlank())
                    .map(Object::toString)
                    .toList();

            if (selectedPermissions.isEmpty()) {
                showError("Phải chọn ít nhất một quyền.");
                return;
            }

            try {
                // Tìm danh sách permissionId tương ứng
                List<Integer> permissionIds = selectedPermissions.stream()
                        .map(name -> Arrays.stream(allPermissions)
                                .filter(p -> p.getName().equals(name))
                                .map(ResponsePermissionDto::getId)
                                .findFirst()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .toList();

                // Tạo role + permission trong 1 lần
                UserRoleDto roleDto = UserRoleDto.builder()
                        .name(roleName)
                        .build();

                var json=ApiHttpClientCaller.call("user-role", ApiHttpClientCaller.Method.POST, roleDto);
                ResponseUserRoleDto createdRole = mapper.readValue(json, ResponseUserRoleDto.class);
                Integer newRoleId = createdRole.getId();
                for (Integer permissionId : permissionIds) {
                    UserRolePermissionDto dto = UserRolePermissionDto.builder()
                            .userRoleId(newRoleId)
                            .permissionId(permissionId)
                            .build();

                    ApiHttpClientCaller.call("user-role-permission", ApiHttpClientCaller.Method.POST, dto);
                }
                showInfo("Tạo vai trò mới thành công!");
                loadRoles();
            } catch (Exception ex) {
                showError("Lỗi khi tạo vai trò: " + ex.getMessage());
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
