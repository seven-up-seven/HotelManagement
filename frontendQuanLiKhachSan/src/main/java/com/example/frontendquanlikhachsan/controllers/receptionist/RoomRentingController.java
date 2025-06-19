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
import com.example.frontendquanlikhachsan.entity.roomType.ResponseRoomTypeDto;
import com.example.frontendquanlikhachsan.entity.roomType.RoomTypeDto;
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
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.GET;

public class RoomRentingController {

    @FXML private TextField bookingIdField;
    @FXML private Button    btnLoadBooking;

    private FilteredList<ResponseRoomDto> filteredRooms;

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

//    @FXML
//    private VBox detailPane;

    @FXML
    private TextArea noteArea;

    @FXML
    private TextField rentalDaysField;

    @FXML
    private Button createRentalButton;

    @FXML private Button btnCreateGuest; // nút thêm khách

    // RIGHT
    @FXML private TextField tfFilterName, tfFilterId, tfFilterCCCD, tfFilterAge, tfFilterPhone;
    @FXML private TableView<ResponseGuestDto> tableAllGuests;
    @FXML private TableColumn<ResponseGuestDto,Integer> colAllId;
    @FXML private TableColumn<ResponseGuestDto,String>  colAllName;
    @FXML private TableColumn<ResponseGuestDto,Sex>     colAllSex;
    @FXML private TableColumn<ResponseGuestDto,Integer> colAllAge;
    @FXML private TableColumn<ResponseGuestDto,String>  colAllCCCD;
    @FXML private TableColumn<ResponseGuestDto,String>  colAllPhone;

    @FXML private VBox detailPane;
    @FXML private HBox filterBox;

//    @FXML
//    private ComboBox<String> roomTypePicker;

    private ObservableList<ResponseGuestDto> guestList = FXCollections.observableArrayList();
    private ObservableList<ResponseGuestDto> allGuests=FXCollections.observableArrayList();
    private ObservableList<ResponseRoomDto> roomList = FXCollections.observableArrayList();

    @FXML private ComboBox<ResponseRoomTypeDto>     roomTypePicker;
    @FXML private Button                    btnCheckAvailable;
    @FXML private ComboBox<ResponseRoomDto> roomPicker;



    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private final Map<RoomState, Integer> statePageMap = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
        initRoomTypePicker();


        Platform.runLater(() -> {
            if (!roomList.isEmpty()) {
                ScrollBar scrollBar = (ScrollBar) roomPicker.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((obs, oldV, newV) -> {
                        if (newV.doubleValue() >= 1.0) {
                            loadNextRoomPage();
                        }
                    });
                }
            }
        });
        roomPicker.setEditable(false);

        // 2) Khách đã chọn
        bindCustomerTable();
        removeSelectedGuestsButton.setOnAction(e->removeSelectedGuests());

        // 3) Tải allGuests rồi setup table, filter, double-click
        loadAllGuests();
        bindAllGuestsTable();
        setupFilters();
        setupDoubleClick();

        // 4) Tạo mới khách bật dialog
        btnCreateGuest.setOnAction(e -> showCreateForm());

        // 5) Tạo phiếu thuê
        createRentalButton.setOnAction(e->createNewRentalForm());
        customerTable.setEditable(true);
        colCustomerSelect.setEditable(true);

        showListView();
    }

    @FXML
    private void onLoadBooking() {
        String raw = bookingIdField.getText().trim();
        if (raw.isEmpty()) {
            showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập Booking ID.");
            return;
        }
        int bookingId;
        try {
            bookingId = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            showErrorAlert("Sai định dạng", "Booking ID phải là số nguyên.");
            return;
        }

        new Thread(() -> {
            try {
                // 1) GET booking-confirmation-form/{id}
                String json = ApiHttpClientCaller.call(
                        "booking-confirmation-form/" + bookingId,
                        GET, null);
                ResponseBookingConfirmationFormDto booking =
                        mapper.readValue(json, ResponseBookingConfirmationFormDto.class);

                Platform.runLater(() -> {
                    // 2) Điền ngày và số ngày thuê
                    creationDatePicker.setValue(booking.getBookingDate().toLocalDate());
                    rentalDaysField.setText(String.valueOf(booking.getRentalDays()));

                    // 3) Lấy thông tin phòng
                    new Thread(() -> {
                        try {
                            String roomJson = ApiHttpClientCaller.call(
                                    "room/" + booking.getRoomId(),
                                    GET, null);
                            ResponseRoomDto room =
                                    mapper.readValue(roomJson, ResponseRoomDto.class);

                            Platform.runLater(() -> {
                                // thêm vào list nếu chưa có
                                if (!roomList.contains(room)) {
                                    roomList.add(0, room);
                                }
                                roomPicker.getSelectionModel().select(room);
                            });
                        } catch (Exception roomEx) {
                            roomEx.printStackTrace();
                            Platform.runLater(() ->
                                    showErrorAlert("Lỗi tải phòng", roomEx.getMessage()));
                        }
                    }).start();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showErrorAlert("Lỗi tải booking", "Không tìm thấy Booking #" + bookingId));
            }
        }).start();
    }

    
    @FXML private void onCreateGuest() {
        showCreateForm();
    }

    private void showListView() {
        detailPane.getChildren().setAll(
                filterBox,
                tableAllGuests,
                btnCreateGuest
        );
    }

    @FXML
    private void onCheckAvailableRooms() {
        ResponseRoomTypeDto sel = roomTypePicker.getValue();
        if (sel == null) {
            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn loại phòng.");
            return;
        }

        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("room/room-type/" + sel.getId(), GET, null);
                List<ResponseRoomDto> rooms = mapper.readValue(
                        json, new TypeReference<List<ResponseRoomDto>>() {}
                );

                Platform.runLater(() -> {
                    if (rooms.isEmpty()) {
                        showErrorAlert("Hết phòng", "Không có phòng trống cho loại này.");
                        roomList.clear();               // xoá hết
                        createRentalButton.setDisable(true);
                    } else {

                        createRentalButton.setDisable(false);
                        roomList.setAll(rooms);         // CẬP NHẬT dữ liệu
                        roomPicker.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showErrorAlert("Lỗi tải phòng", ex.getMessage())
                );
            }
        }).start();
    }



    private void initRoomTypePicker() {
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("room-type", GET, null);
                List<ResponseRoomTypeDto> types = mapper.readValue(json,
                        new TypeReference<List<ResponseRoomTypeDto>>(){});
                Platform.runLater(() -> {
                    roomTypePicker.getItems().setAll(types);
                    roomTypePicker.setConverter(new StringConverter<>() {
                        @Override public String toString(ResponseRoomTypeDto t) {
                            return t == null? "" : t.getName();
                        }
                        @Override public ResponseRoomTypeDto fromString(String s) { return null; }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Lỗi tải loại phòng", e.getMessage()));
            }
        }).start();
    }

    private void loadAvailableRooms() {
        ResponseRoomTypeDto sel = roomTypePicker.getValue();
        if (sel == null) {
            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn loại phòng.");
            return;
        }
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call(
                        "room-type/" + sel.getId(), GET, null);
                List<ResponseRoomDto> rooms = mapper.readValue(json,
                        new TypeReference<List<ResponseRoomDto>>(){});
                Platform.runLater(() -> {
                    if (rooms.isEmpty()) {
                        showErrorAlert("Hết phòng", "Không có phòng trống cho loại này.");
                        roomPicker.getItems().clear();
                        roomPicker.setDisable(true);
                    } else {
                        roomPicker.getItems().setAll(rooms);
                        roomPicker.getSelectionModel().selectFirst();
                        roomPicker.setDisable(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorAlert("Lỗi tải phòng", e.getMessage()));
            }
        }).start();
    }


    private void openCreateGuestDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateGuestForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            // sau khi close, reload lại danh sách allGuests
            loadAllGuests();
        } catch (IOException ex) {
            ex.printStackTrace();
            showErrorAlert("Lỗi", "Không mở được form tạo khách.");
        }
    }

//    private void initRoomTypePicker() {
//        new Thread(() -> {
//            try {
//                String json = ApiHttpClientCaller.call("room-type", GET, null);
//                List<RoomTypeDto> types = mapper.readValue(json, new TypeReference<>() {});
//                Platform.runLater(() -> {
//                    roomTypePicker.getItems().setAll(types);
//                    roomTypePicker.setConverter(new StringConverter<>() {
//                        @Override public String toString(RoomTypeDto rt) {
//                            return rt == null? "" : rt.getName();
//                        }
//                        @Override public RoomTypeDto fromString(String s) { return null; }
//                    });
//                });
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                Platform.runLater(() ->
//                        showErrorAlert("Lỗi tải loại phòng", ex.getMessage())
//                );
//            }
//        }).start();
//    }

    /** Gọi API GET /room-type/{id} để lấy danh sách phòng trống của loại đã chọn */
//    private void loadAvailableRooms() {
//        RoomTypeDto selType = roomTypePicker.getValue();
//        if (selType == null) {
//            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn loại phòng.");
//            return;
//        }
//        new Thread(() -> {
//            try {
//                String json = ApiHttpClientCaller.call(
//                        "room-type/" + selType.getId(),
//                        GET, null
//                );
//                List<ResponseRoomDto> rooms = mapper.readValue(
//                        json, new TypeReference<>() {}
//                );
//                Platform.runLater(() -> {
//                    if (rooms.isEmpty()) {
//                        showErrorAlert("Hết phòng", "Không có phòng trống cho loại này.");
//                        roomPicker.getItems().clear();
//                        roomPicker.setDisable(true);
//                    } else {
//                        roomPicker.getItems().setAll(rooms);
//                        roomPicker.getSelectionModel().selectFirst();
//                        roomPicker.setDisable(false);
//                    }
//                });
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                Platform.runLater(() ->
//                        showErrorAlert("Lỗi tải phòng", ex.getMessage())
//                );
//            }
//        }).start();
//    }

    private void showCreateForm() {
        detailPane.getChildren().clear();

        Label title = new Label("» Tạo khách mới");
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(8));

        TextField tfName = new TextField();
        TextField tfAge = new TextField();
        TextField tfPhone = new TextField();
        TextField tfIdNum = new TextField();
        TextField tfEmail = new TextField();
        ComboBox<Sex> cbSex = new ComboBox<>(FXCollections.observableArrayList(Sex.values()));

        grid.add(new Label("Họ & Tên:"),           0, 0);
        grid.add(tfName,                           1, 0);
        grid.add(new Label("Tuổi:"),              0, 1);
        grid.add(tfAge,                            1, 1);
        grid.add(new Label("Giới tính:"),         0, 2);
        grid.add(cbSex,                            1, 2);
        grid.add(new Label("SĐT:"),                0, 3);
        grid.add(tfPhone,                          1, 3);
        grid.add(new Label("CMND/CCCD:"),         0, 4);
        grid.add(tfIdNum,                          1, 4);
        grid.add(new Label("Email:"),              0, 5);
        grid.add(tfEmail,                          1, 5);

        HBox btns = new HBox(10);
        btns.setPadding(new Insets(12,0,0,0));
        Button save = new Button("💾 Lưu"), cancel = new Button("❌ Hủy");

        cancel.setOnAction(e -> showListView());
        save.setOnAction(e -> {
            try {
                // build dto
                GuestDto dto = new GuestDto();
                dto.setName(tfName.getText().trim());
                dto.setAge(Short.parseShort(tfAge.getText().trim()));
                dto.setSex(cbSex.getValue());
                dto.setPhoneNumber(tfPhone.getText().trim());
                dto.setIdentificationNumber(tfIdNum.getText().trim());
                dto.setEmail(tfEmail.getText().trim());

                // gọi API
                ApiHttpClientCaller.call("guest", ApiHttpClientCaller.Method.POST, dto);

                // reload data và back về list
                loadAllGuests();
                showListView();
            } catch(Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Lỗi", "Không thể tạo khách: " + ex.getMessage());
            }
        });
        btns.getChildren().addAll(save, cancel);

        detailPane.getChildren().addAll(title, grid, btns);
    }

    private void loadAllGuests(){
        new Thread(()->{
            try{
                String json = ApiHttpClientCaller.call("guest/get-all-page?page=0&size=1000", GET, null);
                PageResponse<ResponseGuestDto> page = mapper.readValue(json, new TypeReference<>(){});
                Platform.runLater(()->{
                    allGuests.setAll(page.getContent());
                });
            }catch(Exception ex){
                ex.printStackTrace();
                Platform.runLater(()-> showErrorAlert("Lỗi","Không tải được danh sách khách."));
            }
        }).start();
    }



    private void bindCustomerTable() {
        customerTable.setItems(guestList);
        colCustomerSelect.setCellValueFactory(p->{
            var g=p.getValue();
            selectionMap.putIfAbsent(g,new SimpleBooleanProperty(false));
            return selectionMap.get(g);
        });
        colCustomerSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colCustomerSelect));
        colCustomerId   .setCellValueFactory(d->new SimpleObjectProperty<>(d.getValue().getId()));
        colCustomerName .setCellValueFactory(d->new SimpleStringProperty(d.getValue().getName()));
        colCustomerGender.setCellValueFactory(d->new SimpleObjectProperty<>(d.getValue().getSex()));
        colCustomerAge  .setCellValueFactory(d->new SimpleObjectProperty<>((int)d.getValue().getAge()));
        colCustomerIdCard.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getIdentificationNumber()));
    }


    private void bindAllGuestsTable() {
        tableAllGuests.setItems(allGuests);
        colAllId  .setCellValueFactory(d->new SimpleObjectProperty<>(d.getValue().getId()));
        colAllName.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getName()));
        colAllSex .setCellValueFactory(d->new SimpleObjectProperty<>(d.getValue().getSex()));
        colAllAge .setCellValueFactory(d->new SimpleObjectProperty<>((int)d.getValue().getAge()));
        colAllCCCD.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getIdentificationNumber()));
        colAllPhone.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getPhoneNumber()));
    }


    private void setupFilters() {
        Runnable apply=() -> {
            String name=tfFilterName.getText().toLowerCase(), id=tfFilterId.getText(),
                    cccd=tfFilterCCCD.getText(), age=tfFilterAge.getText(), phone=tfFilterPhone.getText();
            tableAllGuests.setItems(allGuests.filtered(g->{
                if(!name.isEmpty() && !g.getName().toLowerCase().contains(name)) return false;
                if(!id.isEmpty() && g.getId()!=Integer.parseInt(id)) return false;
                if(!cccd.isEmpty() && !g.getIdentificationNumber().contains(cccd)) return false;
                if(!phone.isEmpty() && !g.getPhoneNumber().contains(phone)) return false;
                if(!age.isEmpty() && g.getAge()!=Short.parseShort(age)) return false;
                return true;
            }));
        };
        tfFilterName.textProperty().addListener((o,old,nv)->apply.run());
        tfFilterId  .textProperty().addListener((o,old,nv)->apply.run());
        tfFilterCCCD.textProperty().addListener((o,old,nv)->apply.run());
        tfFilterAge .textProperty().addListener((o,old,nv)->apply.run());
        tfFilterPhone.textProperty().addListener((o,old,nv)->apply.run());
    }

    private void setupDoubleClick() {
        tableAllGuests.setRowFactory(tv->{
            var row=new TableRow<ResponseGuestDto>();
            row.setOnMouseClicked(e->{
                if(e.getClickCount()==2 && !row.isEmpty()){
                    var g=row.getItem();
                    if(guestList.contains(g)) {
                        showInfoAlert("Thông báo","Khách đã có!");
                    } else {
                        guestList.add(g);
                        allGuests.remove(g);
                    }
                }
            });
            return row;
        });
    }

    private void createNewRentalForm() {
        ResponseRoomDto selected = roomPicker.getValue();
        if (selected == null) {
            showErrorAlert("Thiếu dữ liệu", "Vui lòng check và chọn phòng.");
            return;
        }
        if (creationDatePicker.getValue() == null) {
            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn ngày tạo phiếu.");
            return;
        }
        String daysTxt = rentalDaysField.getText().trim();
        if (daysTxt.isEmpty()) {
            showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập số ngày thuê.");
            return;
        }
        String note = noteArea.getText().trim();
        if (guestList.isEmpty()) {
            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn khách thuê.");
            return;
        }
        short rentalDays;
        try {
            rentalDays = Short.parseShort(daysTxt);
            if (rentalDays <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showErrorAlert("Lỗi định dạng", "Số ngày thuê không hợp lệ.");
            return;
        }

                LocalDate picked = creationDatePicker.getValue();
              LocalDateTime rentalDate;
             if (picked.isEqual(LocalDate.now())) {
                       // nếu chọn hôm nay → đặt thời điểm là bây giờ + 1 giây
                              rentalDate = LocalDateTime.now().plusSeconds(1);
                   } else {
                      // nếu chọn ngày tương lai → đặt lúc 00:00
                              rentalDate = picked.atStartOfDay();
                 }

        RentalFormDto dto = RentalFormDto.builder()
                .roomId(selected.getId())
                .staffId(TokenHolder.getInstance().getCurrentUserId())
                .rentalDate(rentalDate)
                .numberOfRentalDays(rentalDays)
                .note(note)
                .isPaidAt(null)
                .build();

        new Thread(() -> {
            try {
                // in payload để debug
                System.out.println(">>> POST /rental-form payload: " + mapper.writeValueAsString(dto));
                String resp = ApiHttpClientCaller.call("rental-form", ApiHttpClientCaller.Method.POST, dto);
                System.out.println("<<< response: " + resp);
                ResponseRentalFormDto created = mapper.readValue(resp, ResponseRentalFormDto.class);
                Platform.runLater(() -> {
                    showInfoAlert("Thành công", "Tạo phiếu thuê #" + created.getId() + " thành công!");
                    // TODO: reset UI nếu cần
                });
                new Thread(() -> {
                    try {
                        for (ResponseGuestDto guest : guestList) {
                            RentalFormDetailDto detail = RentalFormDetailDto.builder()
                                    .rentalFormId(created.getId())
                                    .guestId(guest.getId())
                                    .build();
                            // gọi API POST /rental-form-detail
                            ApiHttpClientCaller.call("rental-form-detail", ApiHttpClientCaller.Method.POST, detail);
                        }
                        // 4) Cập nhật UI: xoá phòng vừa thuê, xoá guestList
                        Platform.runLater(() -> {
                            roomList.remove(selected);
                            guestList.clear();
                            creationDatePicker.setValue(null);

                            // 2. Số ngày thuê
                            rentalDaysField.clear();

                            // 3. Ghi chú
                            noteArea.clear();

                            // 4. Loại phòng + phòng chọn
                            roomTypePicker.getSelectionModel().clearSelection();
                            roomPicker.getItems().clear();

                            // 5. Danh sách khách
                            guestList.clear();

                            // 6. Reload lại right pane (nếu bạn muốn làm mới filter)
                            loadAllGuests();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() ->
                                showErrorAlert("Lỗi tạo chi tiết phiếu thuê", e.getMessage())
                        );
                    }
                }).start();
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                        showErrorAlert("Lỗi tạo phiếu thuê", ex.getMessage())
                );
            }
        }).start();
    }

//    private void createNewRentalForm() {
//        ResponseRoomTypeDto selectedType = roomTypePicker.getValue();
//        if (selectedType == null) {
//            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn loại phòng.");
//            return;
//        }
//
//        // 2) Gọi API lấy phòng trống đầu tiên của loại đó
//        List<ResponseRoomDto> available;
//        try {
//            // Giả sử endpoint hỗ trợ filter theo loại và trạng thái
//            String path = "room/state/READY_TO_SERVE?roomType="
//                    + URLEncoder.encode(selectedType, StandardCharsets.UTF_8)
//                    + "&page=0&size=1";
//            String json = ApiHttpClientCaller.call(path, GET, null);
//            PageResponse<ResponseRoomDto> page = mapper.readValue(json,
//                    new TypeReference<PageResponse<ResponseRoomDto>>() {});
//            available = page.getContent();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            showErrorAlert("Lỗi tải phòng", ex.getMessage());
//            return;
//        }
//
//        if (available.isEmpty()) {
//            // không có phòng trống
//            System.err.println("❌ Không còn phòng trống cho loại " + selectedType);
//            showErrorAlert("Hết phòng", "Không tìm thấy phòng trống cho loại “" + selectedType + "”.");
//            return;
//        }
//
//        // 3) Có phòng → dùng phòng đầu tiên
//        ResponseRoomDto room = available.get(0);
//        Integer roomId = room.getId();
//
//        ResponseRoomDto selectedRoom = roomPicker.getSelectionModel().getSelectedItem();
//        if (selectedRoom == null) {
//            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn phòng.");
//            return;
//        }
//
//        // 2. Phải chọn ngày
//        if (creationDatePicker.getValue() == null) {
//            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn ngày tạo phiếu.");
//            return;
//        }
//
//        // 3. Nhập số ngày
//        String daysTxt = rentalDaysField.getText().trim();
//        if (daysTxt.isEmpty()) {
//            showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập số ngày thuê.");
//            return;
//        }
//
//        // 4. (Nếu bắt buộc) ghi chú
//        String note = noteArea.getText().trim();
//        if (note.isEmpty()) {
//            showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập ghi chú.");
//            return;
//        }
//
//        // 5. Phải có ít nhất 1 khách
//        if (guestList.isEmpty()) {
//            showErrorAlert("Thiếu dữ liệu", "Vui lòng chọn khách thuê.");
//            return;
//        }
//
//        // 6. Parse số ngày
//        short rentalDays;
//        try {
//            rentalDays = Short.parseShort(daysTxt);
//            if (rentalDays <= 0) {
//                showErrorAlert("Lỗi định dạng", "Số ngày thuê phải là số nguyên dương.");
//                return;
//            }
//        } catch (NumberFormatException e) {
//            showErrorAlert("Lỗi định dạng", "Số ngày thuê không hợp lệ.");
//            return;
//        }
//
//        // 7. Lấy roomId an toàn
//
//        LocalDateTime rentalDate = creationDatePicker.getValue().atStartOfDay();
//
//        new Thread(() -> {
//            try {
//                // 1) Build DTO
//                RentalFormDto rentalFormDto = RentalFormDto.builder()
//                        .roomId(selectedRoom.getId())
//                        .staffId(TokenHolder.getInstance().getCurrentUserId())
//                        .rentalDate(rentalDate)
//                        .numberOfRentalDays(rentalDays)
//                        .note(note)
//                        .isPaidAt(null)
//                        .build();
//
//                // 2) In payload ra console trước khi gửi
//                String payload = mapper.writeValueAsString(rentalFormDto);
//                System.out.println(">>> POST /rental-form payload: " + payload);
//
//                // 3) Gọi API
//                String responseJson = ApiHttpClientCaller.call("rental-form", ApiHttpClientCaller.Method.POST, rentalFormDto);
//
//                // 4) In luôn response để đối chiếu
//                System.out.println("<<< POST /rental-form response: " + responseJson);
//
//                // 5) Xử lý tiếp như bình thường…
//                ResponseRentalFormDto created = mapper.readValue(responseJson, ResponseRentalFormDto.class);
//                Platform.runLater(() -> {
//                    showInfoAlert("Thành công", "Tạo phiếu thuê #" + created.getId() + " thành công!");
//                    // … reset UI …
//                });
//            } catch (Exception ex) {
//                // 6) In toàn bộ stacktrace để debug
//                ex.printStackTrace();
//                Platform.runLater(() ->
//                        showErrorAlert("Lỗi tạo phiếu thuê", ex.getMessage())
//                );
//            }
//        }).start();
//    }

//    private void initRoomPicker() {
//        roomPicker.setEditable(true);
//        FilteredList<ResponseRoomDto> filteredRooms = new FilteredList<>(roomList, p -> true);
//        roomPicker.setItems(filteredRooms);
//        roomPicker.getEditor().textProperty().addListener((obs, oldText, newText) -> {
//            final String filter = newText == null ? "" : newText.toLowerCase();
//            filteredRooms.setPredicate(room -> {
//                if (filter.isEmpty()) return true;
//                return room.getName().toLowerCase().contains(filter);
//            });
//        });
//        roomPicker.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(ResponseRoomDto room) {
//                if (room == null) {
//                    return "";
//                }
//                return room.getName() + " (" + room.getRoomTypeName() + ")";
//            }
//            @Override
//            public ResponseRoomDto fromString(String string) {
//                return roomList.stream()
//                        .filter(r -> (r.getName() + " (" + r.getRoomTypeName() + ")").equals(string))
//                        .findFirst()
//                        .orElse(null);
//            }
//        });
//        roomPicker.setCellFactory(param -> new ListCell<>() {
//            @Override
//            protected void updateItem(ResponseRoomDto item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getName() + " (" + item.getRoomTypeName() + ")");
//                }
//            }
//        });
//        roomPicker.setButtonCell(new ListCell<>() {
//            @Override
//            protected void updateItem(ResponseRoomDto item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getName() + " (" + item.getRoomTypeName() + ")");
//                }
//            }
//        });
//        roomPicker.setOnShown(e -> {
//            ScrollBar scrollBar = (ScrollBar) roomPicker.lookup(".scroll-bar:vertical");
//            if (scrollBar != null) {
//                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
//                    if (newVal.doubleValue() == 1.0) {
//                        loadNextRoomPage();
//                    }
//                });
//            }
//        });
//        roomPicker.valueProperty().addListener((obs, oldRoom, selectedRoom) -> {
//            if (selectedRoom != null) {
//                showRoomDetail(selectedRoom);
//            }
//        });
//    }

        private void initRoomPicker() {
            roomPicker.setEditable(true);

            // dùng filteredRooms để bind với roomList
            filteredRooms = new FilteredList<>(roomList, r -> true);
            roomPicker.setItems(filteredRooms);

            roomPicker.getEditor().textProperty().addListener((obs, oldText, newText) -> {
                String raw = newText == null ? "" : newText.toLowerCase();
                int idx = raw.indexOf(" (");
                String nameFilter = (idx > 0 ? raw.substring(0, idx) : raw).trim();

                filteredRooms.setPredicate(room ->
                        room.getName().toLowerCase().contains(nameFilter)
                );
            });

            roomPicker.setConverter(new StringConverter<>() {
                @Override public String toString(ResponseRoomDto room) {
                    return room == null ? "" : room.getName() + " (" + room.getRoomTypeName() + ")";
                }
                @Override public ResponseRoomDto fromString(String string) { return null; }
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
                    String json = ApiHttpClientCaller.call(path, GET, null);
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
                    String json = ApiHttpClientCaller.call(path, GET, null);
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

//    private void clearDetailPane() {
//        detailPane.getChildren().clear();
//        Label placeholder = new Label("Chọn thực thể để xem chi tiết...");
//        placeholder.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
//        detailPane.getChildren().add(placeholder);
//    }

//    private void showGuestDetail(ResponseGuestDto guest) {
//        detailPane.getChildren().clear();
//
//        VBox infoBox = new VBox(6);
//        infoBox.setPadding(new Insets(8));
//        infoBox.setStyle("-fx-border-color: #dedede; -fx-border-radius: 4px; -fx-border-width: 1;");
//
//        Label title = new Label("» Thông tin khách hàng");
//        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
//
//        infoBox.getChildren().addAll(
//                title,
//                new Label("ID: " + guest.getId()),
//                new Label("Họ và Tên: " + guest.getName()),
//                new Label("Tuổi: " + guest.getAge()),
//                new Label("Giới tính: " + guest.getSex()),
//                new Label("Số điện thoại: " + guest.getPhoneNumber()),
//                new Label("CMND/CCCD: " + guest.getIdentificationNumber()),
//                new Label("Email: " + guest.getEmail())
//        );
//
//        Accordion accordion = new Accordion();
//        accordion.getPanes().addAll(
//                createListTitledPane("📄 Hóa đơn", guest.getInvoiceIds(), "Invoice"),
//                createListTitledPane("🏠 Phiếu thuê", guest.getRentalFormDetailIds(), "RentalFormDetail"),
//                createListTitledPane("📝 Phiếu đặt phòng", guest.getBookingConfirmationFormIds(), "BookingConfirmation")
//        );
//
//        HBox actionBox = new HBox(10);
//        Button btnBack = new Button("« Quay lại");
//        btnBack.setOnAction(e -> showSelectExistingGuest());
//        actionBox.getChildren().add(btnBack);
//
//        detailPane.getChildren().addAll(infoBox, accordion, actionBox);
//    }

//    private void showCustomerChoiceMenu() {
//        detailPane.getChildren().clear();
//        Label title = new Label("» Thêm khách thuê");
//        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//        Button btnNew = new Button("➕ Thêm mới khách hàng");
//        Button btnChoose = new Button("🔍 Chọn khách hàng sẵn có");
//        btnNew.setOnAction(e -> showCreateGuestForm());
//        btnChoose.setOnAction(e -> showSelectExistingGuest());
//        VBox box = new VBox(10, title, btnNew, btnChoose);
//        box.setPadding(new Insets(10));
//        detailPane.getChildren().add(box);
//    }

//    private void showSelectExistingGuest() {
//        detailPane.getChildren().clear();
//
//        Label title = new Label("» Chọn khách hàng sẵn có");
//        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//
//        TextField idField = new TextField();
//        idField.setPromptText("ID");
//        TextField nameField = new TextField();
//        nameField.setPromptText("Tên");
//        TextField identificationField = new TextField();
//        identificationField.setPromptText("CCCD");
//        TextField ageField = new TextField();
//        ageField.setPromptText("Tuổi");
//        TextField phoneField = new TextField();
//        phoneField.setPromptText("SĐT");
//
//        HBox searchBar = new HBox(10, nameField, idField, identificationField, ageField, phoneField);
//        searchBar.setPadding(new Insets(5));
//
//        guestAccordion = new Accordion();
//        filteredGuests = new FilteredList<>(allGuests, p -> true);
//
//        ChangeListener<String> searchListener = (obs, oldVal, newVal) -> {
//            String name = nameField.getText().toLowerCase();
//            String id = idField.getText().toLowerCase();
//            String cccd = identificationField.getText().toLowerCase();
//            String ageText = ageField.getText().trim();
//            String phone = phoneField.getText().toLowerCase();
//
//            filteredGuests.setPredicate(g -> {
//                boolean matchesName = g.getName().toLowerCase().contains(name);
//                boolean matchesId = Integer.toString(g.getId()).contains(id);
//                boolean matchesCCCD = g.getIdentificationNumber().toLowerCase().contains(cccd);
//                boolean matchesPhone = g.getPhoneNumber().toLowerCase().contains(phone);
//
//                boolean matchesAge = true;
//                if (!ageText.isEmpty()) {
//                    try {
//                        short inputAge = Short.parseShort(ageText);
//                        matchesAge = g.getAge() == inputAge;
//                    } catch (NumberFormatException e) {
//                        matchesAge = false;
//                    }
//                }
//                return matchesName && matchesId && matchesCCCD && matchesPhone && matchesAge;
//            });
//            refreshGuestAccordion(guestAccordion, filteredGuests);
//        };
//
//        nameField.textProperty().addListener(searchListener);
//        idField.textProperty().addListener(searchListener);
//        identificationField.textProperty().addListener(searchListener);
//        ageField.textProperty().addListener(searchListener);
//        phoneField.textProperty().addListener(searchListener);
//
//        Button btnCancel = new Button("Hủy");
//        btnCancel.setOnAction(e -> showCustomerChoiceMenu());
//
//        VBox box = new VBox(10, title, searchBar, guestAccordion, btnCancel);
//        box.setPadding(new Insets(10));
//        detailPane.getChildren().add(box);
//
//        if (allGuests.isEmpty() && guestList.isEmpty()) {
//            new Thread(() -> {
//                try {
//                    String path = "guest?page=0&size=1000";
//                    String json = ApiHttpClientCaller.call(path, GET, null);
//                    List<ResponseGuestDto> guests = mapper.readValue(json, new TypeReference<>() {
//                    });
//                    Platform.runLater(() -> {
//                        allGuests.setAll(guests);
//                        refreshGuestAccordion(guestAccordion, filteredGuests);
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Platform.runLater(() -> showErrorAlert("Lỗi", "Không thể tải danh sách khách hàng"));
//                }
//            }).start();
//        } else {
//            refreshGuestAccordion(guestAccordion, filteredGuests);
//        }
//    }

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

//    private void showCreateGuestForm() {
//        detailPane.getChildren().clear();
//        Label title = new Label("» Thêm mới khách thuê");
//        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(8));
//        TextField tfName = new TextField();
//        TextField tfAge = new TextField();
//        TextField tfPhoneNumber = new TextField();
//        TextField tfIdentificationNumber = new TextField();
//        TextField tfEmail = new TextField();
//        ComboBox<Sex> cbSex = new ComboBox<>(FXCollections.observableArrayList(Sex.values()));
//        cbSex.setPromptText("Chọn giới tính");
//        grid.add(new Label("Họ và tên:"), 0, 0);
//        grid.add(tfName, 1, 0);
//        grid.add(new Label("Tuổi:"), 0, 1);
//        grid.add(tfAge, 1, 1);
//        grid.add(new Label("Số điện thoại:"), 0, 2);
//        grid.add(tfPhoneNumber, 1, 2);
//        grid.add(new Label("CMND/CCCD:"), 0, 3);
//        grid.add(tfIdentificationNumber, 1, 3);
//        grid.add(new Label("Email:"), 0, 4);
//        grid.add(tfEmail, 1, 4);
//        grid.add(new Label("Giới tính:"), 0, 5);
//        grid.add(cbSex, 1, 5);
//        HBox btnBox = new HBox(10);
//        Button btnSave = new Button("Lưu");
//        Button btnCancel = new Button("Hủy");
//        btnCancel.setOnAction(e -> showCustomerChoiceMenu());
//        btnSave.setOnAction(e -> {
//            String name = tfName.getText().trim();
//            String ageStr = tfAge.getText().trim();
//            String phoneNumber = tfPhoneNumber.getText().trim();
//            String idNumber = tfIdentificationNumber.getText().trim();
//            String email = tfEmail.getText().trim();
//            Sex sex = cbSex.getValue();
//            if (name.isEmpty() || ageStr.isEmpty() || phoneNumber.isEmpty() || idNumber.isEmpty() || email.isEmpty() || sex == null) {
//                showErrorAlert("Thiếu dữ liệu", "Vui lòng nhập đầy đủ thông tin!");
//                return;
//            }
//            short age;
//            try {
//                age = Short.parseShort(ageStr);
//                if (age <= 0) {
//                    showErrorAlert("Lỗi dữ liệu", "Tuổi phải là số nguyên dương.");
//                    return;
//                }
//            } catch (NumberFormatException ex) {
//                showErrorAlert("Sai định dạng", "Tuổi phải là số nguyên dương");
//                return;
//            }
//            if (!idNumber.matches("^[1-9]\\d{11}$")) {
//                showErrorAlert("Lỗi dữ liệu", "CMND/CCCD phải đúng 12 chữ số và không bắt đầu bằng 0.");
//                return;
//            }
//            if (!phoneNumber.matches("^\\d{10,11}$")) {
//                showErrorAlert("Lỗi dữ liệu", "Số điện thoại phải gồm 10 hoặc 11 chữ số.");
//                return;
//            }
//            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
//                showErrorAlert("Lỗi dữ liệu", "Địa chỉ email không hợp lệ.");
//                return;
//            }
//            new Thread(() -> {
//                try {
//                    GuestDto newGuest = new GuestDto();
//                    newGuest.setName(name);
//                    newGuest.setAge(age);
//                    newGuest.setPhoneNumber(phoneNumber);
//                    newGuest.setIdentificationNumber(idNumber);
//                    newGuest.setEmail(email);
//                    newGuest.setSex(sex);
//                    newGuest.setAccountId(null);
//
//                    String jsonResponse = ApiHttpClientCaller.call(
//                            "guest",
//                            ApiHttpClientCaller.Method.POST,
//                            newGuest
//                    );
//                    ResponseGuestDto createdGuest = mapper.readValue(jsonResponse, ResponseGuestDto.class);
//
//                    Platform.runLater(() -> {
//                        guestList.add(createdGuest);
//                        showInfoAlert("Thêm thành công", "Khách hàng đã được thêm vào danh sách.");
//                    });
//                } catch (Exception exception) {
//                exception.printStackTrace();
//                Platform.runLater(() -> {
//                    showErrorAlert("Lỗi", "Không thể thêm khách hàng mới: " + (exception.getMessage() != null ? exception.getMessage() : "Không rõ lỗi"));
//                });
//            }
//
//        }).start();
//        });
//        btnBox.getChildren().addAll(btnSave, btnCancel);
//        detailPane.getChildren().addAll(title, grid, btnBox);
//    }

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

//    private void showRoomDetail(ResponseRoomDto room) {
//        detailPane.getChildren().clear();
//
//        VBox box = new VBox(10);
//        box.setPadding(new Insets(10));
//        box.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 4px;");
//
//        Label title = new Label("» Thông tin phòng");
//        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//
//        Label roomInfo = new Label(
//                "ID: " + room.getId() +
//                        "\nTên: " + room.getName() +
//                        "\nGhi chú: " + room.getNote() +
//                        "\nTầng: " + room.getFloorName() +
//                        "\nLoại phòng: " + room.getRoomTypeName() +
//                        "\nTrạng thái: " + room.getRoomState()
//        );
//
//        box.getChildren().addAll(title, roomInfo);
//        new Thread(() -> {
//            try {
//                List<Integer> bookingConfirmationForms = room.getBookingConfirmationFormIds();
//
//                if (bookingConfirmationForms == null || bookingConfirmationForms.isEmpty()) {
//                    Platform.runLater(() -> {
//                        Label noBooking = new Label("Phòng chưa có phiếu đặt.");
//                        noBooking.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
//                        box.getChildren().add(noBooking);
//                    });
//                    return;
//                }
//                String requestBody = mapper.writeValueAsString(bookingConfirmationForms);
//                String json = ApiHttpClientCaller.call(
//                        "booking-confirmation-form/list-id",
//                        ApiHttpClientCaller.Method.POST,
//                        requestBody
//                );
//                List<ResponseBookingConfirmationFormDto> bookings = mapper.readValue(json, new TypeReference<>() {});
//
//                Platform.runLater(() -> {
//                    VBox bookingBox = new VBox(6);
//                    bookingBox.setPadding(new Insets(6));
//
//                    Label bookingTitle = new Label("📋 Danh sách đặt phòng:");
//                    bookingTitle.setStyle("-fx-font-weight: bold;");
//
//                    for (ResponseBookingConfirmationFormDto booking : bookings) {
//                        VBox item = new VBox(3);
//                        item.getChildren().addAll(
//                                new Label("Mã phiếu: " + booking.getId()),
//                                new Label("Khách: " + booking.getGuestName() + " - " + booking.getGuestPhoneNumber()),
//                                new Label("CCCD: " + booking.getGuestIdentificationNumber()),
//                                new Label("Email: " + booking.getGuestEmail()),
//                                new Label("Trạng thái: " + booking.getBookingState()),
//                                new Label("Ngày tạo: " + booking.getCreatedAt())
//                        );
//                        item.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 6px; -fx-border-color: #ddd;");
//                        bookingBox.getChildren().add(item);
//                    }
//
//                    box.getChildren().add(bookingTitle);
//                    box.getChildren().add(bookingBox);
//                });
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                Platform.runLater(() -> showErrorAlert("Lỗi", "Không thể tải danh sách đặt phòng"));
//            }
//        }).start();
//        detailPane.getChildren().add(box);
//    }
}
