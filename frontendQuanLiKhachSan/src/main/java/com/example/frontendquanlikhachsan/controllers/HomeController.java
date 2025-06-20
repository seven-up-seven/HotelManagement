package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HomeController {
    @FXML
    private Label helloLabel, clockLabel;
    @FXML
    private Label staffIdLabel, staffNameLabel, staffEmailLabel, staffRoleLabel;

    @FXML
    public void initialize() {
        startClock();
        loadCurrentUserInfo();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalTime currentTime = LocalTime.now();
            clockLabel.setText(currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void loadCurrentUserInfo() {
        new Thread(() -> {
            try {
                int currentUserId = TokenHolder.getInstance().getCurrentUserId();
                String staffJson = ApiHttpClientCaller.call("staff/" + currentUserId, ApiHttpClientCaller.Method.GET, null);
                ResponseStaffDto staffDto = ApiHttpClientCaller.mapper.readValue(staffJson, ResponseStaffDto.class);

                String accountJson = ApiHttpClientCaller.call("account/" + staffDto.getAccountId(), ApiHttpClientCaller.Method.GET, null);
                ResponseAccountDto accountDto = ApiHttpClientCaller.mapper.readValue(accountJson, ResponseAccountDto.class);

                String role = accountDto.getUserRoleName();

                Platform.runLater(() -> {
                    helloLabel.setText("Xin chào, " + role + " " + staffDto.getFullName());
                    staffIdLabel.setText(String.valueOf(staffDto.getId()));
                    staffNameLabel.setText(staffDto.getFullName());
                    staffEmailLabel.setText(staffDto.getIdentificationNumber());
                    staffRoleLabel.setText(role);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
