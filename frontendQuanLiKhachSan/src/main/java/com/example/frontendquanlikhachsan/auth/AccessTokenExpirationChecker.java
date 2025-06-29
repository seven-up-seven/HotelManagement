package com.example.frontendquanlikhachsan.auth;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class AccessTokenExpirationChecker {
    private static final ObjectMapper mapper = new ObjectMapper();

    private long extractExpiration(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Invalid JWT token");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payload = mapper.readTree(payloadJson);
            long expSeconds = payload.get("exp").asLong();
            return expSeconds * 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public void scheduleTokenExpiryChecker(String accessToken) {
        long expTime = extractExpiration(accessToken);
        long currentTime = System.currentTimeMillis();
        long delay = expTime - currentTime - 30_000;

        if (delay <= 0) {
            showSessionExpirePopup();
            return;
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> showSessionExpirePopup());
            }
        }, delay);
    }

    private void showSessionExpirePopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText("Phiên làm việc sắp hết hạn");
        alert.setContentText("Bạn có muốn mở rộng phiên làm việc không?");

        // Gắn stylesheet vào DialogPane
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );

        ButtonType yesBtn = new ButtonType("Có");
        ButtonType noBtn = new ButtonType("Không", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesBtn, noBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesBtn) {
            boolean success = refreshAccessToken();
            if (success) {
                scheduleTokenExpiryChecker(TokenHolder.getInstance().getAccessToken());
            } else {
                logoutUser();
            }
        } else {
            logoutUser();
        }
    }

    public static boolean refreshAccessToken() {
        try {
            String refreshToken = TokenHolder.getInstance().getRefreshToken();
            RefreshDto refreshDto = new RefreshDto(refreshToken);
            String jsonResponse = ApiHttpClientCaller.call(
                    "authentication/refresh",
                    ApiHttpClientCaller.Method.POST,
                    refreshDto
            );
            ResponseRefreshDto response = mapper.readValue(jsonResponse, ResponseRefreshDto.class);
            TokenHolder.getInstance().setAccessToken(response.accessToken());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void logoutUser() {
        TokenHolder.getInstance().setAccessToken(null);
        TokenHolder.getInstance().setRefreshToken(null);

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontendquanlikhachsan/views/Login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) Stage.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .orElse(null);

                if (stage != null) {
                    stage.setScene(new Scene(root));
                    stage.show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
