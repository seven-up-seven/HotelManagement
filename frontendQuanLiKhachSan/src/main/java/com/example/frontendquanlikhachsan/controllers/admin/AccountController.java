package com.example.frontendquanlikhachsan.controllers.admin;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.account.AccountDto;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.userRole.ResponseUserRoleDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

public class AccountController {
    @FXML private TableView<ResponseAccountDto> tableAccount;
    @FXML private TableColumn<ResponseAccountDto, Integer> colId;
    @FXML private TableColumn<ResponseAccountDto, String> colUsername;
    @FXML private TableColumn<ResponseAccountDto, String> colPassword;
    @FXML private TableColumn<ResponseAccountDto, String> colRole;
    @FXML private VBox detailPane;

    private int currentPage = 0;
    private boolean isLastPage = false;
    private final int pageSize = 10;

    private final ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    private final ObservableList<ResponseAccountDto> accountList = FXCollections.observableArrayList();

    @FXML public void initialize() {
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colUsername.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getUsername()));
        colPassword.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPassword()));
        colRole.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getUserRoleName()));

        tableAccount.getColumns().setAll(List.of(colId, colUsername, colPassword, colRole));
        tableAccount.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableAccount.setItems(accountList);
        tableAccount.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) showAccountDetail(newV);
        });

        loadNextPage();

        tableAccount.setOnScroll(e -> {
            if (e.getDeltaY() < 0 && isScrollAtBottom()) {
                loadNextPage();
            }
        });
    }

    private boolean isScrollAtBottom() {
        ScrollBar scrollBar = (ScrollBar) tableAccount.lookup(".scroll-bar:vertical");
        return scrollBar != null && scrollBar.getValue() >= scrollBar.getMax();
    }

    private void loadNextPage() {
        if (isLastPage) return;
        try {
            String url = "account?page=" + currentPage + "&size=" + pageSize;
            String json = ApiHttpClientCaller.call(url, ApiHttpClientCaller.Method.GET, null);

            JsonNode root = mapper.readTree(json);
            List<ResponseAccountDto> list = mapper.readValue(root.get("content").toString(), new TypeReference<>() {});
            isLastPage = root.get("last").asBoolean();

            accountList.addAll(list);
            currentPage++;
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi tải trang", "Không thể tải thêm dữ liệu.");
        }
    }

    private void showAccountDetail(ResponseAccountDto acc) {
        detailPane.getChildren().clear();

        Label title = new Label("» Chi tiết Tài khoản ID: " + acc.getId());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(8);
        grid.setPadding(new Insets(8));

        grid.add(new Label("Tên đăng nhập:"), 0, 0); grid.add(new Label(acc.getUsername()), 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1); grid.add(new Label(acc.getPassword()), 1, 1);
        grid.add(new Label("Vai trò:"), 0, 2); grid.add(new Label(acc.getUserRoleName()), 1, 2);

        if (!acc.getUserRolePermissionNames().isEmpty()) {
            ListView<String> lv = new ListView<>(FXCollections.observableArrayList(acc.getUserRolePermissionNames()));
            lv.setPrefHeight(lv.getItems().size() * 24 + 2);
            grid.add(new Label("Quyền hạn:"), 0, 3);
            grid.add(lv, 1, 3);
        }

        // Tìm thông tin người dùng liên kết
        try {
            String staffJson = ApiHttpClientCaller.call("staff/account-id/" + acc.getId(), ApiHttpClientCaller.Method.GET, null);
            ResponseStaffDto staff = mapper.readValue(staffJson, ResponseStaffDto.class);
            grid.add(new Label("Tài khoản này của:"), 0, 4); grid.add(new Label("Nhân viên"), 1, 4);
            grid.add(new Label("ID nhân viên:"), 0, 5);
            grid.add(new Label(String.valueOf(staff.getId())), 1, 5);
            grid.add(new Label("Tên nhân viên:"), 0, 5); grid.add(new Label(staff.getFullName()), 1, 5);
        } catch (Exception e1) {
            try {
                String guestJson = ApiHttpClientCaller.call("guest/account-id/" + acc.getId(), ApiHttpClientCaller.Method.GET, null);
                ResponseGuestDto guest = mapper.readValue(guestJson, ResponseGuestDto.class);
                grid.add(new Label("Tài khoản này của:"), 0, 4); grid.add(new Label("Khách hàng"), 1, 4);
                grid.add(new Label("ID khách:"), 0, 5);
                grid.add(new Label(String.valueOf(guest.getId())), 1, 5);
                grid.add(new Label("Tên khách:"), 0, 5); grid.add(new Label(guest.getName()), 1, 5);
            } catch (Exception e2) {
                grid.add(new Label("Người dùng liên kết:"), 0, 4); grid.add(new Label("Không tìm thấy"), 1, 4);
            }
        }

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));
        Button btnEdit = new Button("✏️ Sửa"); btnEdit.setOnAction(e -> showEditForm(acc));
        Button btnDel = new Button("🗑️ Xóa"); btnDel.setOnAction(e -> deleteAccount(acc));
        actions.getChildren().addAll(btnEdit, btnDel);

        detailPane.getChildren().addAll(title, grid, actions);
    }

    private void showEditForm(ResponseAccountDto acc) {
        detailPane.getChildren().clear();

        Label title = new Label("» Chỉnh sửa tài khoản ID: " + acc.getId());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(8);
        grid.setPadding(new Insets(8));

        TextField tfUsername = new TextField(acc.getUsername());
        PasswordField tfPassword = new PasswordField();
        tfPassword.setText(acc.getPassword());

        List<ResponseUserRoleDto> allRoles;
        try {
            var allRolesJson = ApiHttpClientCaller.call("user-role", ApiHttpClientCaller.Method.GET, null);
            allRoles = mapper.readValue(allRolesJson, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải danh sách vai trò.");
            return;
        }

        ComboBox<ResponseUserRoleDto> roleComboBox = new ComboBox<>();
        roleComboBox.setItems(FXCollections.observableArrayList(allRoles));
        roleComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(ResponseUserRoleDto r) {
                return r == null ? "" : r.getName();
            }
            @Override public ResponseUserRoleDto fromString(String s) { return null; }
        });

        roleComboBox.getSelectionModel().select(
                allRoles.stream().filter(r -> r.getId().equals(acc.getUserRoleId())).findFirst().orElse(null)
        );

        VBox permissionBox = new VBox(4);
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            permissionBox.getChildren().clear();
            if (newVal != null && newVal.getPermissionNames() != null) {
                for (String perm : newVal.getPermissionNames()) {
                    permissionBox.getChildren().add(new Label("- " + perm));
                }
            }
        });

        roleComboBox.getSelectionModel().getSelectedItem();
        if (!allRoles.isEmpty()) {
            ResponseUserRoleDto selected = roleComboBox.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getPermissionNames() != null) {
                for (String perm : selected.getPermissionNames()) {
                    permissionBox.getChildren().add(new Label("- " + perm));
                }
            }
        }

        grid.add(new Label("Tên đăng nhập:"), 0, 0); grid.add(tfUsername, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1); grid.add(tfPassword, 1, 1);
        grid.add(new Label("Vai trò:"), 0, 2); grid.add(roleComboBox, 1, 2);
        grid.add(new Label("Quyền hạn:"), 0, 3); grid.add(permissionBox, 1, 3);

        // Hành động Lưu / Hủy
        HBox actions = new HBox(10);
        Button save = new Button("💾 Lưu"), cancel = new Button("❌ Hủy");
        cancel.setOnAction(e -> showAccountDetail(acc));
        save.setOnAction(e -> {
            try {
                ResponseUserRoleDto selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
                if (selectedRole == null) {
                    showError("Thiếu thông tin", "Vui lòng chọn vai trò.");
                    return;
                }

                AccountDto updateDto = AccountDto.builder()
                        .userName(tfUsername.getText().trim())
                        .passWord(tfPassword.getText().trim())
                        .userRoleId(selectedRole.getId())
                        .build();

                ApiHttpClientCaller.call("account/" + acc.getId(), ApiHttpClientCaller.Method.PUT, updateDto);

                showInfo("Cập nhật thành công", "Tài khoản đã được cập nhật.");
                detailPane.getChildren().clear();
                accountList.clear();
                currentPage = 0;
                isLastPage = false;
                loadNextPage();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi", "Không thể cập nhật tài khoản.");
            }
        });

        actions.getChildren().addAll(save, cancel);
        detailPane.getChildren().addAll(title, grid, actions);
    }

    private void deleteAccount(ResponseAccountDto acc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa tài khoản này?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try {
                ApiHttpClientCaller.call("account/" + acc.getId(), ApiHttpClientCaller.Method.DELETE, null);
                showInfo("Đã xóa", "Tài khoản đã bị xóa.");
                accountList.clear(); currentPage = 0; isLastPage = false; loadNextPage();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi", "Không thể xóa tài khoản.");
            }
        });
    }

    private void showInfo(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showError(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
