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
import java.util.function.Consumer;

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

        tableUserRole.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xoá");
            confirm.setHeaderText(null);
            confirm.setContentText("Bạn có chắc chắn muốn xoá vai trò này không?");

            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        String url = "user-role/" + role.getId(); // role là ResponseUserRoleDto
                        ApiHttpClientCaller.call(url, ApiHttpClientCaller.Method.DELETE, null);
                        showInfo("Xoá vai trò thành công!");
                        loadRoles(); // reload danh sách
                        roleDetailContainer.getChildren().clear(); // clear chi tiết
                    } catch (Exception ex) {
                        showError("Lỗi khi xoá vai trò: " + ex.getMessage());
                    }
                }
            });
        });

        updateBtn.setOnAction(actionEvent->showUpdateRoleForm(role));
    }

    private void showUpdateRoleForm(ResponseUserRoleDto role) {
        labelRoleDetailTitle.setText("Cập nhật Vai trò: " + role.getName());
        roleDetailContainer.getChildren().clear();

        Label nameLabel = new Label("Tên vai trò:");
        TextField nameField = new TextField(role.getName());

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

        // Hàm tạo 1 ComboBox + X button
        Consumer<String> addComboBoxWithValue = (String selectedName) -> {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().setAll(Arrays.stream(allPermissions).map(ResponsePermissionDto::getName).toList());
            comboBox.setValue(selectedName);

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

            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
            removeBtn.setOnAction(e -> permissionBox.getChildren().remove(comboBox.getParent()));

            HBox row = new HBox(8, comboBox, removeBtn);
            permissionBox.getChildren().add(row);
        };

        // Thêm các quyền đã có sẵn
        for (String permName : role.getPermissionNames()) {
            addComboBoxWithValue.accept(permName);
        }

        // Nút thêm quyền
        Button addPermissionBtn = new Button("➕ Thêm quyền");
        addPermissionBtn.setOnAction(e -> addComboBoxWithValue.accept(null));

        // Nút lưu cập nhật (logic viết sau)
        Button saveBtn = new Button("💾 Lưu");
        saveBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");

        // Giao diện
        roleDetailContainer.getChildren().addAll(nameLabel, nameField, permissionLabel, permissionBox, addPermissionBtn, saveBtn);

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

            if (roleName.isEmpty()) {
                showError("Tên vai trò không được để trống.");
                return;
            }

            List<String> permissions = permissionBox.getChildren().stream()
                    .filter(node -> node instanceof HBox hBox && hBox.getChildren().get(0) instanceof ComboBox<?>)
                    .map(node -> ((ComboBox<?>) ((HBox) node).getChildren().get(0)).getValue())
                    .filter(val -> val != null && !val.toString().isBlank())
                    .map(Object::toString)
                    .toList();

            if (permissions.isEmpty()) {
                showError("Phải chọn ít nhất một quyền.");
                return;
            }

            try {
                List<UserRolePermissionDto> listPerm = selectedPermissions.stream()
                        .map(name -> Arrays.stream(allPermissions)
                                .filter(p -> p.getName().equals(name))
                                .map(p -> UserRolePermissionDto.builder()
                                        .permissionId(p.getId())
                                        .build())
                                .findFirst()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .toList();

                UserRoleDto roleDto = UserRoleDto.builder()
                        .name(roleName)
                        .listPermissions(listPerm)
                        .build();

                String url = "user-role/" + role.getId();
                ApiHttpClientCaller.call(url, ApiHttpClientCaller.Method.PUT, roleDto);

                showInfo("Cập nhật vai trò thành công!");
                loadRoles();
            } catch (Exception ex) {
                showError("Lỗi khi cập nhật vai trò: " + ex.getMessage());
            }
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
                // B1. Gửi tạo user role mới
                UserRoleDto roleDto = UserRoleDto.builder().name(roleName).build();
                String json = ApiHttpClientCaller.call("user-role", ApiHttpClientCaller.Method.POST, roleDto);

                ResponseUserRoleDto createdRole = mapper.readValue(json, ResponseUserRoleDto.class);
                int newRoleId = createdRole.getId();

                // B2. Gửi danh sách permission tương ứng
                for (String permName : selectedPermissions) {
                    Integer permId = Arrays.stream(allPermissions)
                            .filter(p -> p.getName().equals(permName))
                            .map(ResponsePermissionDto::getId)
                            .findFirst()
                            .orElse(null);

                    if (permId != null) {
                        UserRolePermissionDto dto = UserRolePermissionDto.builder()
                                .userRoleId(newRoleId)
                                .permissionId(permId)
                                .build();

                        ApiHttpClientCaller.call("user-role-permission", ApiHttpClientCaller.Method.POST, dto);
                    }
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
        alert.setHeaderText("Lỗi");
        alert.setContentText(message);

        // Thêm stylesheet cho DialogPane
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        alert.showAndWait();
    }

    private void showInfo(String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);

        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }
}
