package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.AccessTokenExpirationChecker;
import com.example.frontendquanlikhachsan.auth.LoginDto;
import com.example.frontendquanlikhachsan.auth.ResponseLoginDto;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.prefs.Preferences;

@Component
public class LoginController {
    @FXML
    private Label titleLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Hyperlink forgotPasswordButton;

    @FXML
    private ImageView backgroundImageView;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private final List<String> backgroundImages = List.of(
            "/com/example/frontendquanlikhachsan/assets/images/login_background_1.jpg",
            "/com/example/frontendquanlikhachsan/assets/images/login_background_2.jpg",
            "/com/example/frontendquanlikhachsan/assets/images/login_background_3.jpg"
    );

    private int currentImageIndex = 0;

    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    public void initialize() {
        playTypingEffect(titleLabel, 150, 1000);

        errorLabel.setVisible(false);
        loginButton.setOnAction(this::handleLogin);
        startBackgroundRotation();
        String savedUsername = prefs.get("savedUsername", "");
        String savedPassword = prefs.get("savedPassword", "");

        if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
            usernameField.setText(savedUsername);
            passwordField.setText(savedPassword);
            rememberMeCheckbox.setSelected(true);
        }

        forgotPasswordButton.setOnAction(e -> openForgotPasswordDialog());
    }

    private void playTypingEffect(Label label, int charDelayMillis, int pauseMillis) {
        String fullText = label.getText();
        Timeline tl = new Timeline();

        // 1) Frame 0: clear text
        tl.getKeyFrames().add(new KeyFrame(Duration.ZERO,
                e -> label.setText("")
        ));

        // 2) Mỗi ký tự
        for (int i = 0; i < fullText.length(); i++) {
            final int idx = i;
            tl.getKeyFrames().add(new KeyFrame(
                    Duration.millis(charDelayMillis * (idx + 1)),
                    e -> {
                        if (idx < fullText.length() - 1) {
                            label.setText(fullText.substring(0, idx + 1) + "...");
                        } else {
                            // đến cuối thì show fullText, no '...'
                            label.setText(fullText);
                        }
                    }
            ));
        }

        // 3) Extra Pause: giữ nguyên chữ cuối thêm pauseMillis ms
        tl.getKeyFrames().add(new KeyFrame(
                Duration.millis(charDelayMillis * fullText.length() + pauseMillis),
                e -> { /* ko làm gì, chỉ để kéo dài timeline */ }
        ));

        // 4) Loop vô hạn
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
    }

    private void startBackgroundRotation() {
        // Gán ảnh đầu tiên
        setBackgroundWithFade(backgroundImages.get(currentImageIndex));

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            currentImageIndex = (currentImageIndex + 1) % backgroundImages.size();
            setBackgroundWithFade(backgroundImages.get(currentImageIndex));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void setBackgroundWithFade(String imagePath) {
        try {
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());

                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), backgroundImageView);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                fadeOut.setOnFinished(event -> {
                    backgroundImageView.setImage(image);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(500), backgroundImageView);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });

                fadeOut.play();
            } else {
                System.err.println("Không tìm thấy ảnh: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }
        try {
            LoginDto loginDto = new LoginDto(username, password);
            String jsonResponse = ApiHttpClientCaller.call(
                    "authentication/login",
                    ApiHttpClientCaller.Method.POST,
                    loginDto
            );
            ResponseLoginDto response = mapper.readValue(jsonResponse, new TypeReference<>() {});
            TokenHolder.getInstance().setAccessToken(response.accessToken());
            TokenHolder.getInstance().setRefreshToken(response.refreshToken());

            int accountId = extractAccountIdFromToken(response.accessToken());
            String json = ApiHttpClientCaller.call(
                    "staff/account-id/" + accountId,
                    ApiHttpClientCaller.Method.GET,
                    null
            );
            ResponseStaffDto staff = mapper.readValue(json, ResponseStaffDto.class);
            TokenHolder.getInstance().setCurrentUserId(staff.getId());

            AccessTokenExpirationChecker checker = new AccessTokenExpirationChecker();
            checker.scheduleTokenExpiryChecker(response.accessToken());

            if (rememberMeCheckbox.isSelected()) {
                prefs.put("savedUsername", username);
                prefs.put("savedPassword", password);
            } else {
                prefs.remove("savedUsername");
                prefs.remove("savedPassword");
            }

            goToMainScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Đăng nhập thất bại");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void goToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendquanlikhachsan/views/Main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.hide();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải được màn hình chính");
        }
    }

    private int extractAccountIdFromToken(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);
        return Integer.parseInt(jsonNode.get("sub").asText());
    }

    private void openForgotPasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/example/frontendquanlikhachsan/views/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initOwner(loginButton.getScene().getWindow());
            dialog.setTitle("Quên mật khẩu");

            // TẠO Scene và gắn CSS
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
            showError("Không mở được form quên mật khẩu");
        }
    }

}
