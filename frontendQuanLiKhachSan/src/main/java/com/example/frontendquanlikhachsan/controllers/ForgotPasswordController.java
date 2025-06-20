package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.ForgotPasswordDto;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class ForgotPasswordController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private Button submitButton;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        messageLabel.setVisible(false);

        submitButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();

            if (username.isEmpty() || email.isEmpty()) {
                showMessage("Vui lòng nhập đầy đủ thông tin", Color.RED);
                return;
            }

            try {
                ForgotPasswordDto forgotPasswordDto=ForgotPasswordDto.builder().username(username).email(email).build();

                String response = ApiHttpClientCaller.call(
                        "authentication/forgot-password",
                        ApiHttpClientCaller.Method.POST,
                        forgotPasswordDto
                );

                showMessage("Mật khẩu mới đã gửi về email.", Color.GREEN);
                // Đóng cửa sổ sau 2 giây
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        submitButton.getScene().getWindow().hide();
                    } catch (InterruptedException ignored) {}
                }).start();

            } catch (Exception e) {
                showMessage("Không thể gửi mật khẩu. Kiểm tra lại thông tin.", Color.RED);
                e.printStackTrace();
            }
        });
    }

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setTextFill(color);
        messageLabel.setVisible(true);
    }
}
