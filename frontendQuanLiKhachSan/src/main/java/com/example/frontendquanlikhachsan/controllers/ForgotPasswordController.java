package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.ForgotPasswordDto;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;

@Component
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

    @FXML
    private void hoverIn(MouseEvent e) {
        Button b = (Button) e.getSource();
        b.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #89aef8);\n" +
                        "    -fx-background-radius: 20;\n" +
                        "    -fx-text-fill: white;\n" +
                        "    -fx-font-size: 16px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-cursor: hand;\n" +
                        "    -fx-padding: 12 0;\n" +
                        "    /* chuẩn bị cho effect (clip con ripple) */\n" +
                        "    -fx-clip: rect(0, 0, 0, 0);" +
                        "-fx-background-color: rgba(255,255,255,0.25);\n" +
                        "    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 15, 0.3, 0, 6);"+
                "-fx-border-color: rgba(255,255,255,0.3);"+
                        "-fx-border-radius: 20;"

        );
    }

    @FXML
    private void hoverOut(MouseEvent e) {
        Button b = (Button) e.getSource();
        b.setStyle("/* nền mờ trắng 15% */\n" +
                "    -fx-background-color: white;\n" +
                "    /* bo tròn và viền trắng mờ */\n" +
                "    -fx-background-radius: 20;\n" +
                "    -fx-border-radius: 20;\n" +
                "    -fx-border-color: rgba(255,255,255,0.3);\n" +
                "    -fx-border-width: 1;\n" +
                "    /* bóng đổ mềm cho chiều sâu */\n" +
                "    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0.2, 0, 4);\n" +
                "    /* padding cho đẹp */\n" +
                "    -fx-padding: 12 0;\n" +
                "    /* chữ trắng nổi bật */\n" +
                "    -fx-font-size: 15px;\n" +
                "    -fx-font-weight: bold;\n" +
                "    /* smooth chuyển màu */\n" +
                "    -fx-cursor: hand;");
    }

}
