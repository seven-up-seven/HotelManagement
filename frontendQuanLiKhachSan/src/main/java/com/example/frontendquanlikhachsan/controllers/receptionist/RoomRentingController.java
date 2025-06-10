package com.example.frontendquanlikhachsan.controllers.receptionist;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.enums.RoomState;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.page.PageResponse;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ObservableList<ResponseGuestDto> allGuests=FXCollections.observableArrayList();
    private ObservableList<ResponseRoomDto> roomList = FXCollections.observableArrayList();

    private int currentPage = 0;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private final Map<RoomState, Integer> statePageMap = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);;

    @FXML
    public void initialize() {
        initRoomPicker();
        loadFirstRoomPage();
        loadGuest();

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showGuestDetail(newSel);
            } else {
                clearDetailPane();
            }
        });

        addCustomerButton.setOnAction(e -> showCreateGuestForm());
        clearDetailPane();
    }

    private void loadRoom() {
        try {
            roomList.clear();
            RoomState[] states = {RoomState.READY_TO_SERVE, RoomState.BEING_RENTED, RoomState.BOOKED};
            for (RoomState state : states) {
                int page = 0;
                while (true) {
                    String path = "room/state/" + state.name() + "?page=" + page + "&size=10";
                    String json = ApiHttpClientCaller.call(path, ApiHttpClientCaller.Method.GET, null);
                    PageResponse<ResponseRoomDto> pageResponse = mapper.readValue(json, new TypeReference<>() {});
                    List<ResponseRoomDto> rooms = pageResponse.getContent();
                    if (rooms == null || rooms.isEmpty()) {
                        break;
                    }
                    roomList.addAll(rooms);
                    page++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải dữ liệu", "Không thể lấy dữ liệu danh sách phòng");
        }
    }

    private void initRoomPicker() {
        roomPicker.setEditable(true);

        FilteredList<ResponseRoomDto> filteredRooms = new FilteredList<>(roomList, p -> true);
        roomPicker.setItems(filteredRooms);

        roomPicker.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            final String filter = newText == null ? "" : newText.toLowerCase();
            filteredRooms.setPredicate(room -> {
                if (filter.isEmpty()) return true;
                return room.getName().toLowerCase().contains(filter);
            });
        });

        roomPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(ResponseRoomDto room) {
                if (room == null) {
                    return "";
                }
                return room.getName() + " (" + room.getRoomTypeName() + ")";
            }

            @Override
            public ResponseRoomDto fromString(String string) {
                return roomList.stream()
                        .filter(r -> (r.getName() + " (" + r.getRoomTypeName() + ")").equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        roomPicker.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ResponseRoomDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getRoomTypeName() + ")");
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
                    setText(item.getName() + " (" + item.getRoomTypeName() + ")");
                }
            }
        });

        roomPicker.setOnShown(e -> {
            ScrollBar scrollBar = (ScrollBar) roomPicker.lookup(".scroll-bar:vertical");
            if (scrollBar != null) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0) { // Kéo tới cuối
                        loadNextRoomPage();
                    }
                });
            }
        });
    }

    private void loadFirstRoomPage() {
        roomList.clear();
        hasMoreData = true;
        isLoading = false;
        statePageMap.clear();
        // Khởi tạo page 0 cho mỗi RoomState
        for (RoomState state : RoomState.values()) {
            statePageMap.put(state, 0);
        }
        loadNextRoomPage();
    }

    private void loadNextRoomPage() {
        if (isLoading || !hasMoreData) {
            return;
        }

        isLoading = true;

        new Thread(() -> {
            try {
                boolean loadedAny = false; // Kiểm tra xem có load được dữ liệu nào không
                RoomState[] states = {RoomState.READY_TO_SERVE, RoomState.BEING_RENTED, RoomState.BOOKED};

                for (RoomState state : states) {
                    int page = statePageMap.getOrDefault(state, 0); // Lấy page hiện tại của trạng thái này
                    String path = "room/state/" + state.name() + "?page=" + page + "&size=" + PAGE_SIZE;
                    String json = ApiHttpClientCaller.call(path, ApiHttpClientCaller.Method.GET, null);

                    PageResponse<ResponseRoomDto> pageResponse = mapper.readValue(json, new TypeReference<>() {});
                    List<ResponseRoomDto> rooms = pageResponse.getContent();

                    if (rooms == null || rooms.isEmpty()) {
                        // Không load được nữa cho state này, remove nó khỏi map
                        statePageMap.remove(state);
                    } else {
                        Platform.runLater(() -> roomList.addAll(rooms));
                        // Tăng page cho trạng thái này
                        statePageMap.put(state, page + 1);
                        loadedAny = true;
                    }
                }

                if (!loadedAny) {
                    hasMoreData = false; // Không load được thêm phòng nào -> hết data
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Lỗi tải dữ liệu", "Không thể lấy dữ liệu danh sách phòng"));
            } finally {
                isLoading = false;
            }
        }).start();
    }


    private void loadGuest() {
        try {
            String json=ApiHttpClientCaller.call("guest", ApiHttpClientCaller.Method.GET, null);
            List<ResponseGuestDto> guests=mapper.readValue(json, new TypeReference<>() {});
            guestList.clear();
            guestList.setAll(guests);
        }
        catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải dữ liệu", "Không thể lấy dữ liệu danh sách khách hàng");
        }
    }

    private void clearDetailPane() {
        detailPane.getChildren().clear();
        Label placeholder = new Label("Chọn thực thể để xem chi tiết...");
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
}
