package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.AccessTokenExpirationChecker;
import com.example.frontendquanlikhachsan.auth.LoginDto;
import com.example.frontendquanlikhachsan.auth.ResponseLoginDto;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loginButton.setOnAction(this::handleLogin);
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
            //store the token
            TokenHolder.getInstance().setAccessToken(response.accessToken());
            TokenHolder.getInstance().setRefreshToken(response.refreshToken());
            //check when the access token will expire
            AccessTokenExpirationChecker checker = new AccessTokenExpirationChecker();
            checker.scheduleTokenExpiryChecker(response.accessToken());
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
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải được màn hình chính");
        }
    }
}
