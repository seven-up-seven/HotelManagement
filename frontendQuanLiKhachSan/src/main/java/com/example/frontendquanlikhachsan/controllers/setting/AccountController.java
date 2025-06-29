package com.example.frontendquanlikhachsan.controllers.setting;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.account.AccountDto;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class AccountController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button editButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button forgotPasswordButton;
    @FXML
    private Button checkPasswordButton;

    private boolean passwordVerified = false;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private int currentAccountId;
    private int userRoleId;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        loadAccount();
    }

    private void loadAccount() {
        new Thread(()->{
            try {
                var currentUserId= TokenHolder.getInstance().getCurrentUserId();
                String staffJson = ApiHttpClientCaller.call("staff/" + currentUserId, ApiHttpClientCaller.Method.GET, null);
                ResponseStaffDto staffDto = mapper.readValue(staffJson, ResponseStaffDto.class);
                currentAccountId= staffDto.getAccountId();
                String accountJson=ApiHttpClientCaller.call("account/" + staffDto.getAccountId(), ApiHttpClientCaller.Method.GET, null);
                ResponseAccountDto accountDto = mapper.readValue(accountJson, ResponseAccountDto.class);
                userRoleId= accountDto.getUserRoleId();
                Platform.runLater(() -> {
                    usernameField.setText(accountDto.getUsername());
                    currentPasswordField.clear();
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                showAlert("Không thể tải thông tin tài khoản");
            }
        }).start();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleEdit() {
        isEditMode = !isEditMode;

        usernameField.setEditable(isEditMode);
        currentPasswordField.setEditable(isEditMode);
        checkPasswordButton.setVisible(isEditMode);
        saveButton.setVisible(false);

        newPasswordField.setEditable(false);
        confirmPasswordField.setEditable(false);
        newPasswordField.setVisible(false);
        confirmPasswordField.setVisible(false);
        passwordVerified = false;

        if (isEditMode) {
            editButton.setText("Hủy");
            currentPasswordField.clear();
        } else {
            editButton.setText("Sửa");
            loadAccount();
        }
    }

    @FXML
    public void handleSave() {
        if (!passwordVerified) {
            showAlert("Vui lòng xác minh mật khẩu hiện tại trước.");
            return;
        }

        String username = usernameField.getText();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Mật khẩu xác nhận không khớp.");
            return;
        }

        new Thread(() -> {
            try {
                var currentUserId= TokenHolder.getInstance().getCurrentUserId();
                String staffJson = ApiHttpClientCaller.call("staff/" + currentUserId, ApiHttpClientCaller.Method.GET, null);
                ResponseStaffDto staffDto = mapper.readValue(staffJson, ResponseStaffDto.class);
                currentAccountId= staffDto.getAccountId();
                String accountJson=ApiHttpClientCaller.call("account/" + staffDto.getAccountId(), ApiHttpClientCaller.Method.GET, null);
                ResponseAccountDto accountDto = mapper.readValue(accountJson, ResponseAccountDto.class);

                boolean isPasswordCorrect= accountDto.getPassword().equals(currentPassword);

                Platform.runLater(() -> {
                    if (!isPasswordCorrect) {
                        showAlert("Mật khẩu hiện tại không chính xác.");
                        return;
                    }

                    if (!newPasswordField.isVisible()) {
                        newPasswordField.setVisible(true);
                        confirmPasswordField.setVisible(true);
                        newPasswordField.setEditable(true);
                        confirmPasswordField.setEditable(true);
                        showInfo("Vui lòng nhập mật khẩu mới và xác nhận.");
                        return;
                    }

                    // Kiểm tra xác nhận mật khẩu
                    if (!newPassword.equals(confirmPassword)) {
                        showAlert("Mật khẩu xác nhận không khớp.");
                        return;
                    }

                    // Tạo DTO gửi lên
                    AccountDto dto = AccountDto.builder()
                            .username(username)
                            .password(newPassword.isEmpty() ? currentPassword : newPassword)
                            .userRoleId(userRoleId)
                            .build();

                    new Thread(() -> {
                        try {
                            ApiHttpClientCaller.call("account/"+currentAccountId, ApiHttpClientCaller.Method.PUT, dto);

                            Platform.runLater(() -> {
                                showInfo("Cập nhật tài khoản thành công.");
                                handleEdit();
                                loadAccount();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> showAlert("Lỗi khi cập nhật tài khoản."));
                        }
                    }).start();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Lỗi xác minh mật khẩu."));
            }
        }).start();
    }

    @FXML
    public void handleForgotPassword() {
        openForgotPasswordDialog();
    }

    private void openForgotPasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/example/frontendquanlikhachsan/views/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initOwner(saveButton.getScene().getWindow());
            dialog.setTitle("Quên mật khẩu");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/login.css")
                            .toExternalForm()
            );
            dialog.setScene(scene);

            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không mở được form quên mật khẩu");
        }
    }

    @FXML
    public void handleCheckPassword() {
        String currentPassword = currentPasswordField.getText();

        new Thread(() -> {
            try {
                String accountJson = ApiHttpClientCaller.call("account/" + currentAccountId, ApiHttpClientCaller.Method.GET, null);
                ResponseAccountDto accountDto = mapper.readValue(accountJson, ResponseAccountDto.class);

                boolean isPasswordCorrect = accountDto.getPassword().equals(currentPassword);
                Platform.runLater(() -> {
                    if (isPasswordCorrect) {
                        passwordVerified = true;
                        showInfo("Mật khẩu đúng. Bạn có thể đổi mật khẩu.");
                        saveButton.setVisible(true);

                        newPasswordField.setVisible(true);
                        confirmPasswordField.setVisible(true);
                        newPasswordField.setEditable(true);
                        confirmPasswordField.setEditable(true);
                    } else {
                        passwordVerified = false;
                        showAlert("Mật khẩu không chính xác.");
                        saveButton.setVisible(false);
                        newPasswordField.setVisible(false);
                        confirmPasswordField.setVisible(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Lỗi xác minh mật khẩu."));
            }
        }).start();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
