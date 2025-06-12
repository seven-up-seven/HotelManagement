package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.bookingconfirmationform.BookingConfirmationFormDto;
import com.example.frontendquanlikhachsan.entity.bookingconfirmationform.ResponseBookingConfirmationFormDto;
import com.example.frontendquanlikhachsan.entity.enums.BookingState;
import com.example.frontendquanlikhachsan.entity.enums.RoomState;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.*;

public class BookingConfirmationFormController {
    @FXML private TextField   filterId;
    @FXML private ComboBox<BookingState> filterState;
    @FXML private DatePicker  filterFrom, filterTo;
    @FXML private TextField   filterRoomId;
    @FXML private ComboBox<String> filterType;
    @FXML private Button      btnFilter;

    @FXML private TableView<ResponseBookingConfirmationFormDto> tblBooking;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,Integer> colId;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,BookingState> colState;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colCreated;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colGuestName;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,Integer> colGuestId;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colRoomName;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,Integer> colRoomId;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colRoomType;

    @FXML private VBox detailPane;
    @FXML private Button btnAdd;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private final String token = "";
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private List<ResponseBookingConfirmationFormDto> allItems;

    @FXML
    public void initialize() {
        // ID
        colId.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getId())
        );
        // Trạng thái
        colState.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getBookingState())
        );
        // Ngày tạo vẫn ổn
        colCreated.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCreatedAt().format(fmt))
        );
        // Tên khách vẫn ổn
        colGuestName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getGuestName())
        );
        // Guest ID
        colGuestId.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getGuestId())
        );
        // Tên phòng vẫn ổn
        colRoomName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRoomName())
        );
        // Room ID
        colRoomId.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getRoomId())
        );
        // Loại phòng vẫn ổn
        colRoomType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRoomTypeName())
        );

        // --- 2. Lấy danh sách và điền vào combo filter ---
        filterState.setItems(FXCollections.observableArrayList(BookingState.values()));
        filterState.getItems().add(0, null); // null = All
        filterState.getSelectionModel().selectFirst();

        // Lấy danh sách roomType từ data vừa load
        btnFilter.setDisable(true); // disable cho tới khi load xong

        // chờ load xong mới fill filterType
        loadBookingForms();

        // --- 3. Listener khi bấm Áp dụng hoặc enter trong TextField ---
        btnFilter.setOnAction(e -> applyFilters());
        filterId.setOnAction(e -> applyFilters());
        filterRoomId.setOnAction(e -> applyFilters());
        filterState.valueProperty().addListener((o,ov,nv)->applyFilters());
        filterFrom.valueProperty().addListener((o,ov,nv)->applyFilters());
        filterTo.valueProperty().addListener((o,ov,nv)->applyFilters());

        // Nút thêm, listener, load data…
        btnAdd.setOnAction(e -> showBookingForm(null));
        tblBooking.getSelectionModel().selectedItemProperty().addListener((o,old,sel)-> {
            detailPane.getChildren().clear();
            if(sel!=null) showDetail(sel);
        });
        loadBookingForms();
    }

    private void loadBookingForms() {
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("booking-confirmation-form", GET, null, token);
                allItems = mapper.readValue(json, new TypeReference<List<ResponseBookingConfirmationFormDto>>(){});
                Platform.runLater(() -> {
                    // cập nhật TableView
                    tblBooking.setItems(FXCollections.observableArrayList(allItems));
                    // fill roomType list + “All”
                    Set<String> types = allItems.stream()
                            .map(ResponseBookingConfirmationFormDto::getRoomTypeName)
                            .collect(Collectors.toSet());
                    List<String> typeList = new ArrayList<>();
                    typeList.add("All");
                    typeList.addAll(types);
                    filterType.setItems(FXCollections.observableArrayList(typeList));
                    filterType.getSelectionModel().selectFirst();
                    btnFilter.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi tải dữ liệu", e.getMessage()));
            }
        }).start();
    }

    private void applyFilters() {
        String idText      = Optional.ofNullable(filterId.getText()).orElse("").trim();
        BookingState stSel  = filterState.getValue();
        LocalDate fromDate  = filterFrom.getValue();
        LocalDate toDate    = filterTo.getValue();
        String roomIdText  = Optional.ofNullable(filterRoomId.getText()).orElse("").trim();
        String typeSel     = filterType.getValue();

        FilteredList<ResponseBookingConfirmationFormDto> fl =
                new FilteredList<>(FXCollections.observableArrayList(allItems), b -> true);

        fl.setPredicate(b -> {
            // ID
            if (!idText.isEmpty()) {
                try {
                    if (b.getId() != Integer.parseInt(idText)) return false;
                } catch (NumberFormatException e) { return false; }
            }
            // Trạng thái
            if (stSel != null && b.getBookingState() != stSel) return false;
            // Ngày tạo
            LocalDate ld = b.getCreatedAt().toLocalDate();
            if (fromDate != null && ld.isBefore(fromDate)) return false;
            if (toDate   != null && ld.isAfter(toDate))   return false;
            // Room ID
            if (!roomIdText.isEmpty()) {
                try {
                    if (b.getRoomId() != Integer.parseInt(roomIdText)) return false;
                } catch (NumberFormatException e) { return false; }
            }
            // Loại phòng
            if (!"All".equals(typeSel) && !b.getRoomTypeName().equals(typeSel)) return false;

            return true;
        });

        // Wrap thành SortedList để duy trì sort
        SortedList<ResponseBookingConfirmationFormDto> sl = new SortedList<>(fl);
        sl.comparatorProperty().bind(tblBooking.comparatorProperty());
        tblBooking.setItems(sl);
    }


    private Integer getSelectedBookingId() {
        var sel = tblBooking.getSelectionModel().getSelectedItem();
        return sel!=null ? sel.getId() : null;
    }

    private void showBookingForm(Integer id) {
        detailPane.getChildren().clear();
        boolean edit = id != null;

        Label hdr = new Label(edit ? "✏️ Sửa booking" : "➕ Tạo booking");
        hdr.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        TextField tfGuestId = new TextField();
        tfGuestId.setPromptText("Guest ID");
        ComboBox<BookingState> cbState = new ComboBox<>(
                FXCollections.observableArrayList(BookingState.values())
        );
        cbState.getSelectionModel().selectFirst();
        TextField tfRoomId = new TextField();
        tfRoomId.setPromptText("Room ID");

        if(edit) {
            var cur = tblBooking.getSelectionModel().getSelectedItem();
            tfGuestId.setText(String.valueOf(cur.getGuestId()));
            cbState.getSelectionModel().select(cur.getBookingState());
            tfRoomId.setText(String.valueOf(cur.getRoomId()));
        }

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        form.add(new Label("Guest ID:"), 0, 0);
        form.add(tfGuestId, 1, 0);
        form.add(new Label("Trạng thái:"), 0, 1);
        form.add(cbState, 1, 1);
        form.add(new Label("Room ID:"), 0, 2);
        form.add(tfRoomId, 1, 2);

        Button btnSave = new Button(edit ? "Lưu" : "Tạo");
        Button btnCancel = new Button("Hủy");
        btnSave.setOnAction(e -> {
            String roomIdText = tfRoomId.getText().trim();
            try {
                int roomId = Integer.parseInt(roomIdText);
                // 1) Lấy thông tin phòng từ API
                String roomJson = ApiHttpClientCaller.call(
                        "room/" + roomId, GET, null, token
                );
                ResponseRoomDto room = mapper.readValue(
                        roomJson, ResponseRoomDto.class
                );

                // 2) Kiểm tra trạng thái
                if (room.getRoomState() != RoomState.READY_TO_SERVE) {
                    showError("Phòng không khả dụng",
                            "Phòng " + room.getName() + " đang ở trạng thái "
                                    + room.getRoomState() + ", không thể đặt.");
                    return;
                }

                // 3) Tạo DTO và gọi API
                BookingConfirmationFormDto dto = new BookingConfirmationFormDto(
                        Integer.parseInt(tfGuestId.getText().trim()),
                        cbState.getValue(),
                        roomId
                );
                if (edit) {
                    ApiHttpClientCaller.call(
                            "booking-confirmation-form/" + id, PUT, dto, token
                    );
                } else {
                    ApiHttpClientCaller.call(
                            "booking-confirmation-form", POST, dto, token
                    );
                }

                // 4) Refresh và clear form
                loadBookingForms();
                detailPane.getChildren().clear();

            } catch (NumberFormatException nfe) {
                showError("Lỗi nhập", "Room ID phải là số nguyên.");
            } catch (Exception ex) {
                showError("Lỗi lưu dữ liệu", ex.getMessage());
            }
        });

        btnCancel.setOnAction(e -> detailPane.getChildren().clear());

        HBox actions = new HBox(10, btnSave, btnCancel);
        actions.setPadding(new Insets(10));

        detailPane.getChildren().addAll(hdr, form, actions);
    }

    private void deleteBooking(Integer id) throws Exception {
        if(id == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa booking này?", ButtonType.OK, ButtonType.CANCEL
        );
        if(a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ApiHttpClientCaller.call(
                    "booking-confirmation-form/"+id, DELETE, null, token
            );
            loadBookingForms();
            detailPane.getChildren().clear();
        }
    }

    private void showDetail(ResponseBookingConfirmationFormDto d) {
        detailPane.getChildren().clear();
        // --- phần hiển thị thông tin ---
        detailPane.getChildren().addAll(
                new Label("ID: " + d.getId()),
                new Label("Trạng thái: " + d.getBookingState()),
                new Label("Ngày tạo: " + d.getCreatedAt().format(fmt)),
                new Separator(),
                createBoldRow("Guest ID:", String.valueOf(d.getGuestId())),
                createBoldRow("Guest Name:", d.getGuestName()),
                createBoldRow("Room ID:", String.valueOf(d.getRoomId())),
                createBoldRow("Room Name:", d.getRoomName()),
                createBoldRow("Loại phòng:", d.getRoomTypeName())
        );

        // --- tạo HBox chứa 2 nút Sửa + Xóa ---
        Button btnEdit  = new Button("✏️ Sửa");
        Button btnDelete= new Button("❌ Xóa");
        btnEdit.setOnAction(e -> showBookingForm(d.getId()));
        btnDelete.setOnAction(e -> {
            try {
                deleteBooking(d.getId());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        HBox actions = new HBox(10, btnEdit, btnDelete);
        actions.setPadding(new Insets(10, 0, 0, 0));
        actions.setAlignment(Pos.CENTER_RIGHT);
        detailPane.getChildren().add(actions);
    }



    private HBox createBoldRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight:bold;");
        Label val = new Label(value);
        HBox row = new HBox(5, lbl, val);
        row.setPadding(new Insets(5,0,5,0));
        return row;
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
