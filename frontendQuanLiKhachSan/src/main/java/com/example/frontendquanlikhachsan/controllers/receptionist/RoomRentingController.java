package com.example.frontendquanlikhachsan.controllers.receptionist;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.auth.TokenHolder;
import com.example.frontendquanlikhachsan.entity.bookingconfirmationform.ResponseBookingConfirmationFormDto;
import com.example.frontendquanlikhachsan.entity.enums.RoomState;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.guest.GuestDto;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.page.PageResponse;
import com.example.frontendquanlikhachsan.entity.rentalForm.RentalFormDto;
import com.example.frontendquanlikhachsan.entity.rentalForm.ResponseRentalFormDto;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.RentalFormDetailDto;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private TableColumn<ResponseGuestDto, Integer> colCustomerAge;

    @FXML
    private TableColumn<ResponseGuestDto, String> colCustomerIdCard;

    @FXML
    private TableColumn<ResponseGuestDto, Boolean> colCustomerSelect;

    @FXML
    private Button removeSelectedGuestsButton;

    private final Map<ResponseGuestDto, BooleanProperty> selectionMap = new HashMap<>();

    @FXML
    private TableView<ResponseGuestDto> customerTable;

    private FilteredList<ResponseGuestDto> filteredGuests;
    private Accordion guestAccordion;

    @FXML
    private DatePicker creationDatePicker;

    @FXML
    private VBox detailPane;

    @FXML
    private TextArea noteArea;

    @FXML
    private TextField rentalDaysField;

    @FXML
    private ComboBox<ResponseRoomDto> roomPicker;

    @FXML
    private Button createRentalButton;

    private ObservableList<ResponseGuestDto> guestList = FXCollections.observableArrayList();
    private ObservableList<ResponseGuestDto> allGuests=FXCollections.observableArrayList();
    private ObservableList<ResponseRoomDto> roomList = FXCollections.observableArrayList();

    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private final Map<RoomState, Integer> statePageMap = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);;

    @FXML
    private void removeSelectedGuests() {
        List<ResponseGuestDto> selectedGuests = new ArrayList<>();

        for (Map.Entry<ResponseGuestDto, BooleanProperty> entry : selectionMap.entrySet()) {
            if (entry.getValue().get()) {
                selectedGuests.add(entry.getKey());
            }
        }

        if (selectedGuests.isEmpty()) {
            showInfoAlert("Thông báo", "Không có khách hàng nào được chọn để xóa.");
            return;
        }

        guestList.removeAll(selectedGuests);
        allGuests.addAll(selectedGuests);

        for (ResponseGuestDto guest : selectedGuests) {
            selectionMap.remove(guest);
        }

        customerTable.refresh();
        refreshGuestAccordion(guestAccordion, filteredGuests);
    }

    @FXML
    public void initialize() {
        initRoomPicker();
        loadFirstRoomPage();
        loadGuest();
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        customerTable.setItems(guestList);
        colCustomerId.setMaxWidth(1f * Integer.MAX_VALUE * 1);
        colCustomerName.setMaxWidth(1f * Integer.MAX_VALUE * 2);
        colCustomerGender.setMaxWidth(1f * Integer.MAX_VALUE * 1);
        colCustomerAge.setMaxWidth(1f * Integer.MAX_VALUE * 1);
        colCustomerIdCard.setMaxWidth(1f * Integer.MAX_VALUE * 2);
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showGuestDetail(newSel);
            } else {
                clearDetailPane();
            }
        });
        customerTable.setEditable(true);
        colCustomerSelect.setEditable(true);
        colCustomerSelect.setCellValueFactory(param -> {
            ResponseGuestDto guest = param.getValue();
            selectionMap.putIfAbsent(guest, new SimpleBooleanProperty(false));
            return selectionMap.get(guest);
        });
        colCustomerSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colCustomerSelect));
        addCustomerButton.setOnAction(e -> showCustomerChoiceMenu());
        colCustomerId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colCustomerName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCustomerGender.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSex()));
        colCustomerAge.setCellValueFactory(data -> new SimpleObjectProperty<>(Integer.valueOf(data.getValue().getAge())));
        colCustomerIdCard.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdentificationNumber()));
        clearDetailPane();
        removeSelectedGuestsButton.setOnAction(e -> removeSelectedGuests());
        createRentalButton.setOnAction(e->createNewRentalForm());
    }

    private void createNewRentalForm() {
        if (noteArea.getText().isEmpty()
                || rentalDaysField.getText().isEmpty()
                || roomPicker.getSelectionModel().getSelectedItem() == null
                || creationDatePicker.getValue() == null) {
            showErrorAlert("Thông báo", "Vui lòng nhập đầy đủ các trường.");
            return;
        }
        if (guestList.isEmpty()) {
            showErrorAlert("Thông báo", "Không có khách hàng thuê phòng nào được chọn.");
            return;
        }
        short rentalDays;
        try {
            rentalDays = Short.parseShort(rentalDaysField.getText().trim());
            if (rentalDays <= 0) {
                showErrorAlert("Lỗi", "Số ngày thuê phải là số nguyên dương.");
                return;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Lỗi", "Số ngày thuê không hợp lệ.");
            return;
        }
        Integer roomId = roomPicker.getSelectionModel().getSelectedItem().getId();
        LocalDateTime rentalDate = creationDatePicker.getValue().atStartOfDay();
        String note = noteArea.getText().trim();
        new Thread(() -> {
            try {
                RentalFormDto rentalFormDto = RentalFormDto.builder()
                        .roomId(roomId)
                        .staffId(TokenHolder.getInstance().getCurrentUserId())
                        .rentalDate(rentalDate)
                        .numberOfRentalDays(rentalDays)
                        .note(note)
                        .isPaidAt(null)
                        .build();
                String responseJson = ApiHttpClientCaller.call(
                        "rental-form",
                        ApiHttpClientCaller.Method.POST,
                        rentalFormDto
                );
                ResponseRentalFormDto createdRentalForm = mapper.readValue(responseJson, ResponseRentalFormDto.class);
                List<Integer> detailList = new ArrayList<>();
                for (ResponseGuestDto guest : guestList) {
                    detailList.add(guest.getId());
                }
                ApiHttpClientCaller.call(
                        "rental-form-detail/rental-form/"+createdRentalForm.getId(),
                        ApiHttpClientCaller.Method.POST,
                        detailList
                );
                Platform.runLater(() -> {
                    showInfoAlert("Thành công", "Đã tạo phiếu thuê phòng thành công!");
                    guestList.clear();
                    loadGuest();
                    rentalDaysField.clear();
                    noteArea.clear();
                    roomPicker.getSelectionModel().clearSelection();
                    creationDatePicker.setValue(null);
                    clearDetailPane();
                    customerTable.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Lỗi", "Không thể tạo phiếu thuê phòng: " + e.getMessage()));
            }
        }).start();
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
                    if (newVal.doubleValue() == 1.0) {
                        loadNextRoomPage();
                    }
                });
            }
        });
        roomPicker.valueProperty().addListener((obs, oldRoom, selectedRoom) -> {
            if (selectedRoom != null) {
                showRoomDetail(selectedRoom);
            }
        });
    }

    private void loadFirstRoomPage() {
        roomList.clear();
        hasMoreData = true;
        isLoading = false;
        statePageMap.clear();
        RoomState[] states = {RoomState.READY_TO_SERVE, RoomState.BEING_RENTED, RoomState.BOOKED};
        for (RoomState state : states) {
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
                boolean loadedAny = false;
                RoomState[] states = {RoomState.READY_TO_SERVE, RoomState.BEING_RENTED, RoomState.BOOKED};
                for (RoomState state : states) {
                    int page = statePageMap.getOrDefault(state, 0);
                    String path = "room/state/" + state.name() + "?page=" + page + "&size=" + PAGE_SIZE;
                    String json = ApiHttpClientCaller.call(path, ApiHttpClientCaller.Method.GET, null);
                    PageResponse<ResponseRoomDto> pageResponse = mapper.readValue(json, new TypeReference<>() {});
                    List<ResponseRoomDto> rooms = pageResponse.getContent();
                    if (rooms == null || rooms.isEmpty()) {
                        statePageMap.remove(state);
                    } else {
                        Platform.runLater(() -> roomList.addAll(rooms));
                        statePageMap.put(state, page + 1);
                        loadedAny = true;
                    }
                }
                if (!loadedAny) {
                    hasMoreData = false;
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
        guestList.clear();
        allGuests.clear();
        new Thread(() -> {
            int page = 0;
            int size = 10;
            boolean hasMore = true;
            while (hasMore) {
                try {
                    String path = "guest/get-all-page?page=" + page + "&size=" + size;
                    String json = ApiHttpClientCaller.call(path, ApiHttpClientCaller.Method.GET, null);
                    PageResponse<ResponseGuestDto> pageResponse = mapper.readValue(json, new TypeReference<>() {});
                    List<ResponseGuestDto> guests = pageResponse.getContent();
                    if (guests == null || guests.isEmpty()) {
                        hasMore = false;
                    } else {
                        Platform.runLater(() -> {
                            allGuests.addAll(guests);
                        });
                        page++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hasMore = false;
                    Platform.runLater(() -> showErrorAlert("Lỗi tải dữ liệu", "Không thể lấy dữ liệu danh sách khách hàng"));
                }
            }
        }).start();
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
        btnBack.setOnAction(e -> showSelectExistingGuest());
        actionBox.getChildren().add(btnBack);

        detailPane.getChildren().addAll(infoBox, accordion, actionBox);
    }

    private void showCustomerChoiceMenu() {
        detailPane.getChildren().clear();
        Label title = new Label("» Thêm khách thuê");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Button btnNew = new Button("➕ Thêm mới khách hàng");
        Button btnChoose = new Button("🔍 Chọn khách hàng sẵn có");
        btnNew.setOnAction(e -> showCreateGuestForm());
        btnChoose.setOnAction(e -> showSelectExistingGuest());
        VBox box = new VBox(10, title, btnNew, btnChoose);
        box.setPadding(new Insets(10));
        detailPane.getChildren().add(box);
    }

    private void showSelectExistingGuest() {
        detailPane.getChildren().clear();

        Label title = new Label("» Chọn khách hàng sẵn có");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Tên");
        TextField identificationField = new TextField();
        identificationField.setPromptText("CCCD");
        TextField ageField = new TextField();
        ageField.setPromptText("Tuổi");
        TextField phoneField = new TextField();
        phoneField.setPromptText("SĐT");

        HBox searchBar = new HBox(10, nameField, idField, identificationField, ageField, phoneField);
        searchBar.setPadding(new Insets(5));

        guestAccordion = new Accordion();
        filteredGuests = new FilteredList<>(allGuests, p -> true);

        ChangeListener<String> searchListener = (obs, oldVal, newVal) -> {
            String name = nameField.getText().toLowerCase();
            String id = idField.getText().toLowerCase();
            String cccd = identificationField.getText().toLowerCase();
            String ageText = ageField.getText().trim();
            String phone = phoneField.getText().toLowerCase();

            filteredGuests.setPredicate(g -> {
                boolean matchesName = g.getName().toLowerCase().contains(name);
                boolean matchesId = Integer.toString(g.getId()).contains(id);
                boolean matchesCCCD = g.getIdentificationNumber().toLowerCase().contains(cccd);
                boolean matchesPhone = g.getPhoneNumber().toLowerCase().contains(phone);

                boolean matchesAge = true;
                if (!ageText.isEmpty()) {
                    try {
                        short inputAge = Short.parseShort(ageText);
                        matchesAge = g.getAge() == inputAge;
                    } catch (NumberFormatException e) {
                        matchesAge = false;
                    }
                }
                return matchesName && matchesId && matchesCCCD && matchesPhone && matchesAge;
            });
            refreshGuestAccordion(guestAccordion, filteredGuests);
        };

        nameField.textProperty().addListener(searchListener);
        idField.textProperty().addListener(searchListener);
        identificationField.textProperty().addListener(searchListener);
        ageField.textProperty().addListener(searchListener);
        phoneField.textProperty().addListener(searchListener);

        Button btnCancel = new Button("Hủy");
        btnCancel.setOnAction(e -> showCustomerChoiceMenu());

        VBox box = new VBox(10, title, searchBar, guestAccordion, btnCancel);
        box.setPadding(new Insets(10));
        detailPane.getChildren().add(box);

        if (allGuests.isEmpty() && guestList.isEmpty()) {
            new Thread(() -> {
                try {
                    String path = "guest?page=0&size=1000";
                    String json = ApiHttpClientCaller.call(path, ApiHttpClientCaller.Method.GET, null);
                    List<ResponseGuestDto> guests = mapper.readValue(json, new TypeReference<>() {
                    });
                    Platform.runLater(() -> {
                        allGuests.setAll(guests);
                        refreshGuestAccordion(guestAccordion, filteredGuests);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showErrorAlert("Lỗi", "Không thể tải danh sách khách hàng"));
                }
            }).start();
        } else {
            refreshGuestAccordion(guestAccordion, filteredGuests);
        }
    }

    private void refreshGuestAccordion(Accordion accordion, List<ResponseGuestDto> guests) {
        accordion.getPanes().clear();
        for (ResponseGuestDto guest : guests) {
            VBox guestInfoBox = new VBox(5,
                    new Label("ID: " + guest.getId()),
                    new Label("Tên: " + guest.getName()),
                    new Label("Tuổi: " + guest.getAge()),
                    new Label("Giới tính: " + guest.getSex()),
                    new Label("SĐT: " + guest.getPhoneNumber()),
                    new Label("CCCD: " + guest.getIdentificationNumber()),
                    new Label("Email: " + guest.getEmail())
            );
            guestInfoBox.setPadding(new Insets(6));
            TitledPane pane = new TitledPane("👤 " + guest.getName(), guestInfoBox);
            pane.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    guestList.add(guest);
                    allGuests.remove(guest);
                    refreshGuestAccordion(accordion, allGuests);
                }
            });
            accordion.getPanes().add(pane);
        }
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
        btnCancel.setOnAction(e -> showCustomerChoiceMenu());
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
                if (age <= 0) {
                    showErrorAlert("Lỗi dữ liệu", "Tuổi phải là số nguyên dương.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Sai định dạng", "Tuổi phải là số nguyên dương");
                return;
            }
            if (!idNumber.matches("^[1-9]\\d{11}$")) {
                showErrorAlert("Lỗi dữ liệu", "CMND/CCCD phải đúng 12 chữ số và không bắt đầu bằng 0.");
                return;
            }
            if (!phoneNumber.matches("^\\d{10,11}$")) {
                showErrorAlert("Lỗi dữ liệu", "Số điện thoại phải gồm 10 hoặc 11 chữ số.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showErrorAlert("Lỗi dữ liệu", "Địa chỉ email không hợp lệ.");
                return;
            }
            new Thread(() -> {
                try {
                    GuestDto newGuest = new GuestDto();
                    newGuest.setName(name);
                    newGuest.setAge(age);
                    newGuest.setPhoneNumber(phoneNumber);
                    newGuest.setIdentificationNumber(idNumber);
                    newGuest.setEmail(email);
                    newGuest.setSex(sex);
                    newGuest.setAccountId(null);

                    String jsonResponse = ApiHttpClientCaller.call(
                            "guest",
                            ApiHttpClientCaller.Method.POST,
                            newGuest
                    );
                    ResponseGuestDto createdGuest = mapper.readValue(jsonResponse, ResponseGuestDto.class);

                    Platform.runLater(() -> {
                        guestList.add(createdGuest);
                        showInfoAlert("Thêm thành công", "Khách hàng đã được thêm vào danh sách.");
                    });
                } catch (Exception exception) {
                exception.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Lỗi", "Không thể thêm khách hàng mới: " + (exception.getMessage() != null ? exception.getMessage() : "Không rõ lỗi"));
                });
            }

        }).start();
        });
        btnBox.getChildren().addAll(btnSave, btnCancel);
        detailPane.getChildren().addAll(title, grid, btnBox);
    }

    private TitledPane createListTitledPane(String title, List<Integer> ids, String prefix) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(6));

        if (ids == null || ids.isEmpty()) {
            Label empty = new Label("Chưa có mục nào");
            empty.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
            box.getChildren().add(empty);
        } else {
            for (Integer id : ids) {
                Label label = new Label(prefix + " #" + id);
                label.setStyle("-fx-cursor: hand; -fx-text-fill: #000000;");
                label.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        // TODO: Chuyển đến trang chi tiết
                        showInfoAlert("Điều hướng", "Chuyển đến chi tiết " + prefix + " #" + id);
                    }
                });

                box.getChildren().add(label);
            }
        }

        TitledPane tp = new TitledPane(title + " (" + (ids == null ? 0 : ids.size()) + ")", box);
        tp.setExpanded(false);
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
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.show();
        });
    }

    private void showRoomDetail(ResponseRoomDto room) {
        detailPane.getChildren().clear();

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 4px;");

        Label title = new Label("» Thông tin phòng");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label roomInfo = new Label(
                "ID: " + room.getId() +
                        "\nTên: " + room.getName() +
                        "\nGhi chú: " + room.getNote() +
                        "\nTầng: " + room.getFloorName() +
                        "\nLoại phòng: " + room.getRoomTypeName() +
                        "\nTrạng thái: " + room.getRoomState()
        );

        box.getChildren().addAll(title, roomInfo);
        new Thread(() -> {
            try {
                List<Integer> bookingConfirmationForms = room.getBookingConfirmationFormIds();

                if (bookingConfirmationForms == null || bookingConfirmationForms.isEmpty()) {
                    Platform.runLater(() -> {
                        Label noBooking = new Label("Phòng chưa có phiếu đặt.");
                        noBooking.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
                        box.getChildren().add(noBooking);
                    });
                    return;
                }
                String requestBody = mapper.writeValueAsString(bookingConfirmationForms);
                String json = ApiHttpClientCaller.call(
                        "booking-confirmation-form/list-id",
                        ApiHttpClientCaller.Method.POST,
                        requestBody
                );
                List<ResponseBookingConfirmationFormDto> bookings = mapper.readValue(json, new TypeReference<>() {});

                Platform.runLater(() -> {
                    VBox bookingBox = new VBox(6);
                    bookingBox.setPadding(new Insets(6));

                    Label bookingTitle = new Label("📋 Danh sách đặt phòng:");
                    bookingTitle.setStyle("-fx-font-weight: bold;");

                    for (ResponseBookingConfirmationFormDto booking : bookings) {
                        VBox item = new VBox(3);
                        item.getChildren().addAll(
                                new Label("Mã phiếu: " + booking.getId()),
                                new Label("Khách: " + booking.getGuestName() + " - " + booking.getGuestPhoneNumber()),
                                new Label("CCCD: " + booking.getGuestIdentificationNumber()),
                                new Label("Email: " + booking.getGuestEmail()),
                                new Label("Trạng thái: " + booking.getBookingState()),
                                new Label("Ngày tạo: " + booking.getCreatedAt())
                        );
                        item.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 6px; -fx-border-color: #ddd;");
                        bookingBox.getChildren().add(item);
                    }

                    box.getChildren().add(bookingTitle);
                    box.getChildren().add(bookingBox);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Lỗi", "Không thể tải danh sách đặt phòng"));
            }
        }).start();
        detailPane.getChildren().add(box);
    }
}
