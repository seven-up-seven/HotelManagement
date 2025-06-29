package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.List;

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
    private Label totalRoomLabel;
    @FXML
    private Label incomeLabel;
    @FXML
    private Label todayRentedRoomLabel;
    @FXML
    private Label totalStaffLabel;
    @FXML
    private Label totalGuestLabel;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        loadCurrentUserInfo();
        loadHomeData();
    }

    private void loadHomeData() {
        new Thread(() -> {
            try {
                // 1. Gọi API trong background thread
                String incomeJson = ApiHttpClientCaller.call("invoice/today-money-amount", ApiHttpClientCaller.Method.GET, null);
                double totalMoney = Double.parseDouble(incomeJson);

                String roomJson = ApiHttpClientCaller.call("room/ready", ApiHttpClientCaller.Method.GET, null);
                List<ResponseRoomDto> rooms = List.of(mapper.readValue(roomJson, ResponseRoomDto[].class));

                String rentalsJson = ApiHttpClientCaller.call("rental-form/today-rental-forms", ApiHttpClientCaller.Method.GET, null);
                int totalRentals = Integer.parseInt(rentalsJson);

                String staffJson = ApiHttpClientCaller.call("staff/staff-amount", ApiHttpClientCaller.Method.GET, null);
                int staffCount = Integer.parseInt(staffJson);

                String guestJson = ApiHttpClientCaller.call("guest/guest-stay", ApiHttpClientCaller.Method.GET, null);
                int guestCount = Integer.parseInt(guestJson);

                // 2. Cập nhật UI trong thread chính
                Platform.runLater(() -> {
                    incomeLabel.setText(totalMoney + " VNĐ");
                    totalRoomLabel.setText(String.valueOf(rooms.size()));
                    todayRentedRoomLabel.setText(String.valueOf(totalRentals));
                    totalStaffLabel.setText(String.valueOf(staffCount));
                    totalGuestLabel.setText(String.valueOf(guestCount));
                });

            } catch (Exception e) {
                e.printStackTrace(); // log lỗi để debug
                Platform.runLater(() -> {
                    incomeLabel.setText("Lỗi");
                    totalRoomLabel.setText("Lỗi");
                    todayRentedRoomLabel.setText("Lỗi");
                    totalStaffLabel.setText("Lỗi");
                    totalGuestLabel.setText("Lỗi");
                });
            }
        }).start();
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