package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class HomeController {
    @FXML
    private Label helloLabel, clockLabel;
    @FXML
    private Label staffIdLabel,
            staffNameLabel,
            staffEmailLabel,
            staffRoleLabel,
            staffIdentifyNumberLabel,
            staffAddressLabel,
            staffAgeLabel,
            staffPositionLabel,
            staffGenderLabel;

    @FXML
    private Label dateDayLabel;
    @FXML
    private ImageView avatarImageView;

    @FXML
    public void initialize() {
        startClock();
        loadCurrentUserInfo();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalTime currentTime = LocalTime.now();
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            String dayOfWeek = switch (currentDate.getDayOfWeek()) {
                case MONDAY -> "Thứ hai";
                case TUESDAY -> "Thứ ba";
                case WEDNESDAY -> "Thứ tư";
                case THURSDAY -> "Thứ năm";
                case FRIDAY -> "Thứ sáu";
                case SATURDAY -> "Thứ bảy";
                case SUNDAY -> "Chủ nhật";
            };

            clockLabel.setText(currentTime.format(timeFormatter));
            dateDayLabel.setText(currentDate.format(dateFormatter) + " – " + dayOfWeek);
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

                if (staffDto.getSex()== Sex.MALE) {
                    Image avatar = new Image(getClass().getResourceAsStream("/com/example/frontendquanlikhachsan/assets/images/male_staff_icon.png"));
                    avatarImageView.setImage(avatar);
                    staffGenderLabel.setText("Nam");
                }
                else {
                    Image avatar = new Image(getClass().getResourceAsStream("/com/example/frontendquanlikhachsan/assets/images/female_staff_icon.png"));
                    avatarImageView.setImage(avatar);
                    staffGenderLabel.setText("Nữ");
                }

                String accountJson = ApiHttpClientCaller.call("account/" + staffDto.getAccountId(), ApiHttpClientCaller.Method.GET, null);
                ResponseAccountDto accountDto = ApiHttpClientCaller.mapper.readValue(accountJson, ResponseAccountDto.class);

                String role = accountDto.getUserRoleName();

                Platform.runLater(() -> {
                    helloLabel.setText("Xin chào, " + role + " " + staffDto.getFullName());
                    staffIdLabel.setText(String.valueOf(staffDto.getId()));
                    staffNameLabel.setText(staffDto.getFullName());
                    staffEmailLabel.setText(staffDto.getEmail());
                    staffIdentifyNumberLabel.setText(staffDto.getIdentificationNumber());
                    staffRoleLabel.setText(role);
                    staffAddressLabel.setText(staffDto.getAddress());
                    staffAgeLabel.setText(String.valueOf(staffDto.getAge()));
                    staffPositionLabel.setText(staffDto.getPositionName());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}