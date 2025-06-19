package com.example.frontendquanlikhachsan.controllers.admin;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.account.AccountDto;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.guest.GuestDto;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.staff.StaffDto;
import com.example.frontendquanlikhachsan.entity.userRole.ResponseUserRoleDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AccountController {
    @FXML private TableView<ResponseAccountDto> tableAccount;
    @FXML private TableColumn<ResponseAccountDto, Integer> colId;
    @FXML private TableColumn<ResponseAccountDto, String> colUsername;
    @FXML private TableColumn<ResponseAccountDto, String> colPassword;
    @FXML private TableColumn<ResponseAccountDto, String> colRole;
    @FXML private VBox accountDetailContainer;
    @FXML private VBox detailPane;
    @FXML private Button btnCreateAccount;

    private int currentPage = 0;
    private boolean isLastPage = false;
    private final int pageSize = 10;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
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

        btnCreateAccount.setOnAction(event -> showCreateAccountForm());
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
            showError("Lỗi tải tài khoản", "Không thể tải thêm dữ liệu.");
        }
    }

    private void showAccountDetail(ResponseAccountDto acc) {
        accountDetailContainer.getChildren().clear();

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
            grid.add(new Label("Tên nhân viên:"), 0, 6); grid.add(new Label(staff.getFullName()), 1, 6);
        } catch (Exception e1) {
            try {
                String guestJson = ApiHttpClientCaller.call("guest/account-id/" + acc.getId(), ApiHttpClientCaller.Method.GET, null);
                ResponseGuestDto guest = mapper.readValue(guestJson, ResponseGuestDto.class);
                grid.add(new Label("Tài khoản này của:"), 0, 4); grid.add(new Label("Khách hàng"), 1, 4);
                grid.add(new Label("ID khách:"), 0, 5);
                grid.add(new Label(String.valueOf(guest.getId())), 1, 5);
                grid.add(new Label("Tên khách:"), 0, 6); grid.add(new Label(guest.getName()), 1, 6);
            } catch (Exception e2) {
                grid.add(new Label("Người dùng liên kết:"), 0, 4); grid.add(new Label("Không tìm thấy"), 1, 4);
            }
        }

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));
        Button btnEdit = new Button("✏️ Sửa"); btnEdit.setOnAction(e -> showEditForm(acc));
        Button btnDel = new Button("🗑️ Xóa"); btnDel.setOnAction(e -> deleteAccount(acc));
        actions.getChildren().addAll(btnEdit, btnDel);

        accountDetailContainer.getChildren().addAll(title, grid, actions);
    }

    private void showEditForm(ResponseAccountDto acc) {
        accountDetailContainer.getChildren().clear();

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
                        .username(tfUsername.getText().trim())
                        .password(tfPassword.getText().trim())
                        .userRoleId(selectedRole.getId())
                        .build();

                ApiHttpClientCaller.call("account/" + acc.getId(), ApiHttpClientCaller.Method.PUT, updateDto);

                showInfo("Cập nhật thành công", "Tài khoản đã được cập nhật.");
                accountDetailContainer.getChildren().clear();
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
        accountDetailContainer.getChildren().addAll(title, grid, actions);
    }

    private void deleteAccount(ResponseAccountDto acc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa tài khoản này?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try {
                ApiHttpClientCaller.call("account/" + acc.getId(), ApiHttpClientCaller.Method.DELETE, null);
                showInfo("Đã xóa", "Tài khoản đã bị xóa.");
                accountList.clear();
                accountDetailContainer.getChildren().clear();
                currentPage = 0;
                isLastPage = false; loadNextPage();
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

    private int userPage = 0;
    private boolean userHasMore = true;
    private boolean userLoading = false;
    private String currentUserRole = null;
    ObservableList<Object> baseList = FXCollections.observableArrayList();

    private void showCreateAccountForm() {
        accountDetailContainer.getChildren().clear();

        // Username
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        // Password
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        // UserRole
        Label userRoleLabel = new Label("Chọn vai trò tài khoản:");
        ComboBox<ResponseUserRoleDto> userRoleCombo = new ComboBox<>();
        userRoleCombo.setPromptText("Chọn vai trò...");
        try {
            String json = ApiHttpClientCaller.call("user-role", ApiHttpClientCaller.Method.GET, null);
            List<ResponseUserRoleDto> roles = Arrays.asList(mapper.readValue(json, ResponseUserRoleDto[].class));
            userRoleCombo.setItems(FXCollections.observableArrayList(roles));
            userRoleCombo.setConverter(new StringConverter<>() {
                @Override public String toString(ResponseUserRoleDto role) {
                    return role == null ? "" : role.getName();
                }
                @Override public ResponseUserRoleDto fromString(String s) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi", "Không thể tải danh sách vai trò.");
        }

        // Loại user: Staff / Guest
        Label roleLabel = new Label("Loại người dùng:");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Staff", "Guest");

        Label userLabel = new Label("Danh sách người dùng chưa có tài khoản:");

        HBox searchBox = new HBox(8);
        TextField searchIdField = new TextField(); searchIdField.setPromptText("Tìm theo ID");
        TextField searchNameField = new TextField(); searchNameField.setPromptText("Tìm theo tên");
        searchBox.getChildren().addAll(searchIdField, searchNameField);

        TableView<Object> userTable = new TableView<>();

        TableColumn<Object, Integer> colUserId = new TableColumn<>("ID");
        TableColumn<Object, String> colUserName = new TableColumn<>("Tên");
        TableColumn<Object, String> colCccd = new TableColumn<>("CCCD");

        colUserId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                (data.getValue() instanceof ResponseStaffDto staff)
                        ? staff.getId()
                        : ((ResponseGuestDto) data.getValue()).getId()));

        colUserName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                (data.getValue() instanceof ResponseStaffDto staff)
                        ? staff.getFullName()
                        : ((ResponseGuestDto) data.getValue()).getName()));

        colCccd.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof ResponseStaffDto staff) {
                return new SimpleStringProperty(staff.getIdentificationNumber());
            } else if (cellData.getValue() instanceof ResponseGuestDto guest) {
                return new SimpleStringProperty(guest.getIdentificationNumber());
            } else {
                return new SimpleStringProperty("");
            }
        });

        userTable.getColumns().setAll(colUserId, colUserName, colCccd);
        tableAccount.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        FilteredList<Object> filteredList = new FilteredList<>(baseList);
        userTable.setItems(filteredList);

        userTable.setOnScroll(e -> {
            if (e.getDeltaY() < 0 && isScrollAtBottom(userTable) && !userLoading && userHasMore) {
                userLoading = true;
                userPage++;
                if ("Staff".equals(currentUserRole)) {
                    fetchPagedStaffWithoutAccount(userPage);
                } else if ("Guest".equals(currentUserRole)) {
                    fetchPagedGuestWithoutAccount(userPage);
                }
            }
        });

        searchIdField.textProperty().addListener((obs, oldVal, newVal) -> applyUserFilter(filteredList, searchIdField, searchNameField));
        searchNameField.textProperty().addListener((obs, oldVal, newVal) -> applyUserFilter(filteredList, searchIdField, searchNameField));

        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            baseList.clear();
            userPage = 0;
            userHasMore = true;
            userLoading = false;
            currentUserRole = newVal;

            if ("Staff".equals(newVal)) {
                fetchPagedStaffWithoutAccount(userPage);
            } else if ("Guest".equals(newVal)) {
                fetchPagedGuestWithoutAccount(userPage);
            }
        });

        Label selectedUserLabel = new Label();
        selectedUserLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #444;");
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String type;
                if (newVal instanceof ResponseStaffDto) {
                    type = "nhân viên";
                    selectedUserLabel.setText("Bạn đã chọn " + type + " với ID là " + ((ResponseStaffDto) newVal).getId());
                } else if (newVal instanceof ResponseGuestDto) {
                    type = "khách hàng";
                    selectedUserLabel.setText("Bạn đã chọn " + type + " với ID là " + ((ResponseGuestDto) newVal).getId());
                } else {
                    type = "người dùng";
                    selectedUserLabel.setText("Bạn đã chọn " + type + " " + newVal.getClass());
                }
            } else {
                selectedUserLabel.setText("");
            }
        });

        Button btnSave = new Button("Lưu tài khoản");
        btnSave.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String type = roleCombo.getValue();
            Object selectedUser = userTable.getSelectionModel().getSelectedItem();
            ResponseUserRoleDto selectedRole = userRoleCombo.getValue();

            if (username.isEmpty() || password.isEmpty() || type == null || selectedUser == null || selectedRole == null) {
                showError("Thiếu thông tin", "Vui lòng điền đầy đủ và chọn người dùng + vai trò.");
                return;
            }

            int userId = (type.equals("Staff"))
                    ? ((ResponseStaffDto) selectedUser).getId()
                    : ((ResponseGuestDto) selectedUser).getId();



            AccountDto newAccount = AccountDto.builder()
                    .username(username)
                    .password(password)
                    .userRoleId(selectedRole.getId())
                    .build();

            try {
                String endpoint = "account";
                var json=ApiHttpClientCaller.call(endpoint, ApiHttpClientCaller.Method.POST, newAccount);
                ResponseAccountDto createdAccount = mapper.readValue(json, ResponseAccountDto.class);
                int accountId = createdAccount.getId();
                int accountUserId = (type.equals("Staff"))
                        ? ((ResponseStaffDto) selectedUser).getId()
                        : ((ResponseGuestDto) selectedUser).getId();

                String updatePath = (type.equals("Staff") ? "staff/" : "guest/") + accountUserId;

                if (type.equals("Staff")) {
                    StaffDto staffDto = StaffDto.builder()
                            .fullName(((ResponseStaffDto) selectedUser).getFullName())
                            .age(((ResponseStaffDto) selectedUser).getAge())
                            .identificationNumber(((ResponseStaffDto) selectedUser).getIdentificationNumber())
                            .address(((ResponseStaffDto) selectedUser).getAddress())
                            .sex(((ResponseStaffDto) selectedUser).getSex())
                            .salaryMultiplier(((ResponseStaffDto) selectedUser).getSalaryMultiplier())
                            .positionId(((ResponseStaffDto) selectedUser).getPositionId())
                            .accountId(accountId)
                            .build();
                    ApiHttpClientCaller.call(updatePath, ApiHttpClientCaller.Method.PUT, staffDto);
                } else {
                    GuestDto guestDto = GuestDto.builder()
                            .age(((ResponseGuestDto) selectedUser).getAge())
                            .name(((ResponseGuestDto) selectedUser).getName())
                            .identificationNumber(((ResponseGuestDto) selectedUser).getIdentificationNumber())
                            .sex(((ResponseGuestDto) selectedUser).getSex())
                            .phoneNumber(((ResponseGuestDto) selectedUser).getPhoneNumber())
                            .email(((ResponseGuestDto) selectedUser).getEmail())
                            .accountId(accountId)
                            .build();
                    ApiHttpClientCaller.call(updatePath, ApiHttpClientCaller.Method.PUT, guestDto);
                }
                showInfo("Thành công", "Tài khoản đã được tạo.");
                accountList.clear();
                currentPage = 0;
                isLastPage = false;
                loadNextPage();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Lỗi", "Không thể tạo tài khoản: " + ex.getMessage());
            }
        });

        accountDetailContainer.getChildren().addAll(
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                userRoleLabel, userRoleCombo,
                roleLabel, roleCombo,
                userLabel, searchBox,
                userTable, selectedUserLabel,
                btnSave
        );

        roleCombo.setValue("Staff");
    }

    private void applyUserFilter(FilteredList<Object> list, TextField idField, TextField nameField) {
        String id = idField.getText().trim().toLowerCase();
        String name = nameField.getText().trim().toLowerCase();

        list.setPredicate(obj -> {
            int objId = (obj instanceof ResponseStaffDto s) ? s.getId() : ((ResponseGuestDto) obj).getId();
            String objName = (obj instanceof ResponseStaffDto s) ? s.getFullName() : ((ResponseGuestDto) obj).getName();
            return (id.isEmpty() || String.valueOf(objId).contains(id)) &&
                    (name.isEmpty() || objName.toLowerCase().contains(name));
        });
    }

    private void fetchPagedStaffWithoutAccount(int page) {
        try {
            String json = ApiHttpClientCaller.call("staff/without-account?page=" + page + "&size=20", ApiHttpClientCaller.Method.GET, null);
            JsonNode root = mapper.readTree(json);
            List<ResponseStaffDto> result = Arrays.asList(mapper.readValue(root.get("content").toString(), ResponseStaffDto[].class));
            baseList.addAll(result);
            userHasMore = !root.get("last").asBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            userHasMore = false;
        } finally {
            userLoading = false;
        }
    }

    private void fetchPagedGuestWithoutAccount(int page) {
        try {
            String json = ApiHttpClientCaller.call("guest/without-account?page=" + page + "&size=20", ApiHttpClientCaller.Method.GET, null);
            JsonNode root = mapper.readTree(json);
            List<ResponseGuestDto> result = Arrays.asList(mapper.readValue(root.get("content").toString(), ResponseGuestDto[].class));
            baseList.addAll(result);
            userHasMore = !root.get("last").asBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            userHasMore = false;
        } finally {
            userLoading = false;
        }
    }

    private boolean isScrollAtBottom(TableView<?> table) {
        VirtualFlow<?> virtualFlow = (VirtualFlow<?>) table.lookup(".virtual-flow");
        return virtualFlow != null && virtualFlow.getLastVisibleCell().getIndex() >= table.getItems().size() - 1;
    }
}
