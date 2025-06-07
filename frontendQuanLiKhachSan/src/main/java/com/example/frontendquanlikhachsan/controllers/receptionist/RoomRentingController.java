package com.example.frontendquanlikhachsan.controllers.receptionist;

import com.example.frontendquanlikhachsan.entity.enums.RoomState;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.floor.Floor;
import com.example.frontendquanlikhachsan.entity.floor.FloorDto;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.example.frontendquanlikhachsan.entity.roomType.ResponseRoomTypeDto;
import com.example.frontendquanlikhachsan.entity.roomType.RoomType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;

public class RoomRentingController {
    @FXML
    private Button addCustomerButton;

    @FXML
    private TableColumn<ResponseGuestDto, Integer> colCustomerId;

    @FXML
    private TableColumn<ResponseGuestDto, String> colCustomerName;

    @FXML
    private TableColumn<ResponseGuestDto, Sex> colCustomerGender;

    @FXML
    private TableView<ResponseGuestDto> customerTable;

    @FXML
    private DatePicker creationDatePicker;

    @FXML
    private VBox detailPane;

    @FXML
    private CheckBox immediateReturnCheckbox;

    @FXML
    private TextArea noteArea;

    @FXML
    private TextField rentalDaysField;

    @FXML
    private ComboBox<ResponseRoomDto> roomPicker;

    private ObservableList<ResponseGuestDto> guestList = FXCollections.observableArrayList();
    private ObservableList<ResponseRoomDto> roomList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Setup Table Columns
        colCustomerId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        colCustomerName.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        colCustomerGender.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSex()));

        // 2. Load Sample Data
        guestList.addAll(getSampleGuests());

        customerTable.setItems(guestList);
        roomPicker.setItems(roomList);
        roomPicker.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ResponseRoomDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getRoomType().getName() + ")");
                }
            }
        });
        roomPicker.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ResponseRoomDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getRoomType().getName() + ")");
                }
            }
        });

        // 3. Add Listener
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showGuestDetail(newSel);
            } else {
                clearDetailPane();
            }
        });

        // 4. Button action
        addCustomerButton.setOnAction(e -> showCreateGuestForm());

        // 5. Set Default Placeholder
        clearDetailPane();
    }

    private void clearDetailPane() {
        detailPane.getChildren().clear();
        Label placeholder = new Label("Chọn khách hàng để xem chi tiết...");
        placeholder.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        detailPane.getChildren().add(placeholder);
    }

    private void showGuestDetail(ResponseGuestDto guest) {
        detailPane.getChildren().clear();

        VBox infoBox = new VBox(6);
        infoBox.setPadding(new Insets(8));
        infoBox.setStyle("-fx-border-color: #dedede; -fx-border-radius: 4px; -fx-border-width: 1;");

        Label title = new Label("» Thông tin khách hàng");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        infoBox.getChildren().addAll(
                title,
                new Label("ID: " + guest.getId()),
                new Label("Họ và Tên: " + guest.getName()),
                new Label("Tuổi: " + guest.getAge()),
                new Label("Giới tính: " + guest.getSex()),
                new Label("Số điện thoại: " + guest.getPhoneNumber()),
                new Label("CMND/CCCD: " + guest.getIdentificationNumber()),
                new Label("Email: " + guest.getEmail())
        );

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(
                createListTitledPane("📄 Hóa đơn", guest.getInvoiceIds(), "Invoice"),
                createListTitledPane("🏠 Phiếu thuê", guest.getRentalFormDetailIds(), "RentalFormDetail"),
                createListTitledPane("📝 Phiếu đặt phòng", guest.getBookingConfirmationFormIds(), "BookingConfirmation")
        );

        HBox actionBox = new HBox(10);
        Button btnBack = new Button("« Quay lại");
        btnBack.setOnAction(e -> customerTable.getSelectionModel().clearSelection());
        actionBox.getChildren().add(btnBack);

        detailPane.getChildren().addAll(infoBox, accordion, actionBox);
    }

    private void showCreateGuestForm() {
        detailPane.getChildren().clear();

        Label title = new Label("» Thêm mới khách thuê");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(8));

        TextField tfName = new TextField();
        TextField tfAge = new TextField();
        TextField tfPhoneNumber = new TextField();
        TextField tfIdentificationNumber = new TextField();
        TextField tfEmail = new TextField();
        ComboBox<Sex> cbSex = new ComboBox<>(FXCollections.observableArrayList(Sex.values()));
        cbSex.setPromptText("Chọn giới tính");

        grid.add(new Label("Họ và tên:"), 0, 0);
        grid.add(tfName, 1, 0);
        grid.add(new Label("Tuổi:"), 0, 1);
        grid.add(tfAge, 1, 1);
        grid.add(new Label("Số điện thoại:"), 0, 2);
        grid.add(tfPhoneNumber, 1, 2);
        grid.add(new Label("CMND/CCCD:"), 0, 3);
        grid.add(tfIdentificationNumber, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(tfEmail, 1, 4);
        grid.add(new Label("Giới tính:"), 0, 5);
        grid.add(cbSex, 1, 5);

        HBox btnBox = new HBox(10);
        Button btnSave = new Button("Lưu");
        Button btnCancel = new Button("Hủy");

        btnCancel.setOnAction(e -> clearDetailPane());

        btnSave.setOnAction(e -> {
            String name = tfName.getText().trim();
            String ageStr = tfAge.getText().trim();
            String phoneNumber = tfPhoneNumber.getText().trim();
            String idNumber = tfIdentificationNumber.getText().trim();
            String email = tfEmail.getText().trim();
            Sex sex = cbSex.getValue();

            if (name.isEmpty() || ageStr.isEmpty() || phoneNumber.isEmpty() || idNumber.isEmpty() || email.isEmpty() || sex == null) {
                showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            short age;
            try {
                age = Short.parseShort(ageStr);
            } catch (NumberFormatException ex) {
                showErrorAlert("Sai định dạng", "Tuổi phải là số nguyên nhỏ.");
                return;
            }

            int newId = guestList.stream().mapToInt(ResponseGuestDto::getId).max().orElse(0) + 1;
            ResponseGuestDto newGuest = new ResponseGuestDto();
            newGuest.setId(newId);
            newGuest.setName(name);
            newGuest.setAge(age);
            newGuest.setPhoneNumber(phoneNumber);
            newGuest.setIdentificationNumber(idNumber);
            newGuest.setEmail(email);
            newGuest.setSex(sex);
            newGuest.setInvoiceIds(FXCollections.observableArrayList());
            newGuest.setRentalFormDetailIds(FXCollections.observableArrayList());
            newGuest.setBookingConfirmationFormIds(FXCollections.observableArrayList());

            guestList.add(newGuest);
            customerTable.getSelectionModel().select(newGuest);
            showInfoAlert("Thêm thành công", "Khách hàng đã được thêm vào danh sách.");
        });

        btnBox.getChildren().addAll(btnSave, btnCancel);

        detailPane.getChildren().addAll(title, grid, btnBox);
    }

    private TitledPane createListTitledPane(String title, List<Integer> ids, String prefix) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(6));

        if (ids == null || ids.isEmpty()) {
            Label empty = new Label("Chưa có mục nào");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
            box.getChildren().add(empty);
        } else {
            for (Integer id : ids) {
                Hyperlink link = new Hyperlink(prefix + " #" + id);
                link.setOnAction(e -> showInfoAlert("Chi tiết", "Bạn chọn: " + prefix + " #" + id));
                box.getChildren().add(link);
            }
        }

        TitledPane tp = new TitledPane(title + " (" + (ids == null ? 0 : ids.size()) + ")", box);
        tp.setAnimated(true);
        return tp;
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private List<ResponseGuestDto> getSampleGuests() {
        return Arrays.asList(
                new ResponseGuestDto(1, "Nguyễn Văn A", Sex.MALE, (short)30, "123456789", "0912345678", "a@gmail.com", Arrays.asList(101, 102), Arrays.asList(201), Arrays.asList(301)),
                new ResponseGuestDto(2, "Trần Thị B", Sex.FEMALE, (short)25, "987654321", "0987654321", "b@gmail.com", Arrays.asList(103), Arrays.asList(202, 203), Arrays.asList(302, 303))
        );
    }
}
