package com.example.frontendquanlikhachsan.controllers.setting;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.staff.StaffDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Component;

@Component
public class UserInfoController {
    @FXML
    private Label staffIdLabel;
    @FXML
    private TextField staffNameField;
    @FXML
    private TextField identifyField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField ageField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private Label staffRoleLabel;
    @FXML
    private Label staffPositionLabel;
    @FXML
    private ImageView avatarImageView;

    @FXML
    private Button editButton;
    @FXML
    private Button saveButton;

    private boolean isEditMode = false;

    private int positionId;
    private int accountId;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        loadCurrentUserInfo();
    }

    private void loadCurrentUserInfo() {
        new Thread(() -> {
            try {
                int currentUserId = TokenHolder.getInstance().getCurrentUserId();
                String staffJson = ApiHttpClientCaller.call("staff/" + currentUserId, ApiHttpClientCaller.Method.GET, null);
                ResponseStaffDto staffDto = mapper.readValue(staffJson, ResponseStaffDto.class);

                positionId=staffDto.getPositionId();
                accountId=staffDto.getAccountId();

                String accountJson = ApiHttpClientCaller.call("account/" + staffDto.getAccountId(), ApiHttpClientCaller.Method.GET, null);
                ResponseAccountDto accountDto = mapper.readValue(accountJson, ResponseAccountDto.class);

                Image avatar;
                String genderText;
                if (staffDto.getSex() == Sex.MALE) {
                    avatar = new Image(getClass().getResourceAsStream("/com/example/frontendquanlikhachsan/assets/images/male_staff_icon.png"));
                    genderText = "Nam";
                } else {
                    avatar = new Image(getClass().getResourceAsStream("/com/example/frontendquanlikhachsan/assets/images/female_staff_icon.png"));
                    genderText = "Nữ";
                }

                String role = accountDto.getUserRoleName();

                Platform.runLater(() -> {
                    avatarImageView.setImage(avatar);

                    staffIdLabel.setText(String.valueOf(staffDto.getId()));
                    staffRoleLabel.setText(role);
                    staffPositionLabel.setText(staffDto.getPositionName());

                    staffNameField.setText(staffDto.getFullName());
                    emailField.setText(staffDto.getEmail());
                    identifyField.setText(staffDto.getIdentificationNumber());
                    addressField.setText(staffDto.getAddress());
                    ageField.setText(String.valueOf(staffDto.getAge()));

                    genderComboBox.setValue(genderText);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void handleEdit() {
        if (editButton.getText().equals("Hủy")) {
            setEditMode(false);
            loadCurrentUserInfo();
            return;
        }
        setEditMode(true);
    }

    @FXML
    public void handleSave() {
        try {
            var newName = staffNameField.getText();
            var newEmail = emailField.getText();
            var newIdentificationNumber = identifyField.getText();
            var newAddress = addressField.getText();
            var newAge = Integer.parseInt(ageField.getText());
            var newGender = genderComboBox.getValue();

            Sex sex = newGender.equals("Nam") ? Sex.MALE : Sex.FEMALE;

            StaffDto updateStaffDto = StaffDto.builder()
                    .fullName(newName)
                    .email(newEmail)
                    .identificationNumber(newIdentificationNumber)
                    .address(newAddress)
                    .age(newAge)
                    .sex(sex)
                    .positionId(positionId)
                    .accountId(accountId)
                    .build();

            ApiHttpClientCaller.call("staff/"+staffIdLabel.getText(), ApiHttpClientCaller.Method.PUT, updateStaffDto);
            loadCurrentUserInfo();

            setEditMode(false);

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Tuổi phải là một số hợp lệ!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Đã xảy ra lỗi khi lưu thông tin.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setEditMode(boolean enable) {
        staffNameField.setEditable(enable);
        identifyField.setEditable(enable);
        emailField.setEditable(enable);
        addressField.setEditable(enable);
        ageField.setEditable(enable);
        genderComboBox.setDisable(!enable);

        if (enable) editButton.setText("Hủy");
        else editButton.setText("Sửa");
        saveButton.setVisible(enable);
        isEditMode = enable;
    }
}
