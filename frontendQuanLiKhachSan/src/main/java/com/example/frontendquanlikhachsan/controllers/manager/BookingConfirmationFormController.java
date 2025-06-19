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
import javafx.collections.ObservableList;
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

    @FXML private TableView<ResponseBookingConfirmationFormDto> tblBooking;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,Integer> colId;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,BookingState> colState;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colCreated;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colGuestName;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,Integer> colGuestId;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colRoomName;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,Integer> colRoomId;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto,String> colRoomType;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto, String> colBookingDate;
    @FXML private TableColumn<ResponseBookingConfirmationFormDto, Integer> colRentalDays;


    @FXML private VBox detailPane;
    @FXML private Button btnAdd;

    @FXML private Button btnReset;   // nút Reset mới

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private final String token = "";
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private List<ResponseBookingConfirmationFormDto> allItems;
    private FilteredList<ResponseBookingConfirmationFormDto> filteredList;

    private final ObservableList<ResponseBookingConfirmationFormDto> masterList = FXCollections.observableArrayList();
    private List<Integer> multiFilterIds = null;

    @FXML
    public void initialize() {
        // --- 1) set up columns ---
        colId      .setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        colState   .setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getBookingState()));
        colCreated .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt().format(fmt)));
        colGuestName.setCellValueFactory(c-> new SimpleStringProperty(c.getValue().getGuestName()));
        colGuestId .setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getGuestId()));
        colRoomName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomName()));
        colRoomId  .setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRoomId()));
        colRoomType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomTypeName()));
        colBookingDate.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getBookingDate() == null ? "" : c.getValue().getBookingDate().format(fmt))
        );
        colRentalDays.setCellValueFactory(
                c -> new SimpleObjectProperty<>(c.getValue().getRentalDays())
        );

        // --- Thay vì hiển thị “–” khi null, ta để blank ---
        colBookingDate.setCellFactory(col -> new TableCell<ResponseBookingConfirmationFormDto,String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // empty cell hoặc null thì để chuỗi rỗng
                setText(empty || item == null || item.isBlank() ? "" : item);
            }
        });

// Tương tự với RentalDays
        colRentalDays.setCellFactory(col -> new TableCell<ResponseBookingConfirmationFormDto,Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });



        // --- 2) filtered + sorted list ---
        filteredList = new FilteredList<>(masterList, b->true);
        SortedList<ResponseBookingConfirmationFormDto> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(tblBooking.comparatorProperty());
        tblBooking.setItems(sorted);

        // --- 3) fill state & type filters ---
        filterState.setItems(FXCollections.observableArrayList(BookingState.values()));
        filterState.getItems().add(0, null);  // null = All
        filterState.getSelectionModel().selectFirst();

        // --- 4) wire up listeners ---
        filterId.setOnAction(e -> applyFilters());
        filterRoomId.setOnAction(e -> applyFilters());
        filterState.valueProperty().addListener((o,ov,nv)->applyFilters());
        filterFrom.valueProperty().addListener((o,ov,nv)->applyFilters());
        filterTo.valueProperty().addListener((o,ov,nv)->applyFilters());
        btnReset.setOnAction(e -> onResetFilter());
        btnAdd  .setOnAction(e -> showBookingForm(null));
        tblBooking.getSelectionModel().selectedItemProperty()
                .addListener((o,old,sel)-> { detailPane.getChildren().clear(); if (sel!=null) showDetail(sel); });

        // --- 5) load data lần đầu ---
        loadBookingForms();
    }


    @FXML
    private void onResetFilter() {
        multiFilterIds = null;           // tắt chế độ “xem liên quan”
        filterId.clear();
        filterRoomId.clear();
        filterState.getSelectionModel().selectFirst();
        filterFrom.setValue(null);
        filterTo.setValue(null);
        filterType.getSelectionModel().selectFirst();
        applyFilters();
    }

    private void loadBookingForms() {
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("booking-confirmation-form", GET, null);
                List<ResponseBookingConfirmationFormDto> all = mapper.readValue(
                        json, new TypeReference<List<ResponseBookingConfirmationFormDto>>() {});

                Platform.runLater(() -> {
                    // update master list
                    masterList.setAll(all);

                    tblBooking.getSelectionModel().clearSelection();
                    tblBooking.refresh();


                    // refill roomType filter
                    Set<String> types = all.stream()
                            .map(ResponseBookingConfirmationFormDto::getRoomTypeName)
                            .collect(Collectors.toSet());
                    List<String> typeList = new ArrayList<>();
                    typeList.add("All");
                    typeList.addAll(types);
                    filterType.setItems(FXCollections.observableArrayList(typeList));
                    filterType.getSelectionModel().selectFirst();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi tải dữ liệu", e.getMessage()));
            }
        }).start();
    }

    private void applyFilters() {
        if (multiFilterIds != null) {
            // chỉ show những id trong multiFilterIds
            filteredList.setPredicate(b -> multiFilterIds.contains(b.getId()));
            return;
        }

        String idText    = Optional.ofNullable(filterId.getText()).orElse("").trim();
        BookingState st  = filterState.getValue();
        LocalDate from   = filterFrom.getValue();
        LocalDate to     = filterTo.getValue();
        String roomText  = Optional.ofNullable(filterRoomId.getText()).orElse("").trim();
        String typeSel   = filterType.getValue();

        filteredList.setPredicate(b -> {
            if (!idText.isEmpty()) {
                try { if (b.getId() != Integer.parseInt(idText)) return false; }
                catch(Exception ex) { return false; }
            }
            if (st != null && b.getBookingState() != st) return false;
            LocalDate ld = b.getCreatedAt().toLocalDate();
            if (from != null && ld.isBefore(from)) return false;
            if (to   != null && ld.isAfter(to))   return false;
            if (!roomText.isEmpty()) {
                try { if (b.getRoomId() != Integer.parseInt(roomText)) return false; }
                catch(Exception ex) { return false; }
            }
            if (!"All".equals(typeSel) && !b.getRoomTypeName().equals(typeSel)) return false;
            return true;
        });
    }

    public void selectBookingConfirmationFormsByIds(List<Integer> ids) {
        multiFilterIds = new ArrayList<>(ids);
        tblBooking.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        applyFilters();
        // show detail của phần tử đầu
        if (!ids.isEmpty()) {
            filteredList.stream()
                    .filter(b -> b.getId()==ids.get(0))
                    .findFirst().ifPresent(this::showDetail);
        }
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

        DatePicker dpBookingDate = new DatePicker();
        TextField tfRentalDays = new TextField();

        dpBookingDate.setPromptText("Booking Date");
        tfRentalDays.setPromptText("Số ngày thuê");

        if(edit) {
            var cur = tblBooking.getSelectionModel().getSelectedItem();
            tfGuestId.setText(String.valueOf(cur.getGuestId()));
            cbState.getSelectionModel().select(cur.getBookingState());
            tfRoomId.setText(String.valueOf(cur.getRoomId()));
            dpBookingDate.setValue(cur.getBookingDate().toLocalDate());
            tfRentalDays.setText(String.valueOf(cur.getRentalDays()));
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
        form.add(new Label("Ngày thuê:"), 0, 3);
        form.add(dpBookingDate, 1, 3);
        form.add(new Label("Số ngày thuê:"), 0, 4);
        form.add(tfRentalDays, 1, 4);

        Button btnSave = new Button(edit ? "Lưu" : "Tạo");
        Button btnCancel = new Button("Hủy");
        btnSave.setOnAction(e -> {
            String roomIdText = tfRoomId.getText().trim();
            try {
                int roomId = Integer.parseInt(roomIdText);
                // 1) Lấy thông tin phòng từ API
                String roomJson = ApiHttpClientCaller.call(
                        "room/" + roomId, GET, null
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
                        roomId,
                        dpBookingDate.getValue().atStartOfDay(),
                        Integer.parseInt(tfRentalDays.getText().trim())
                );

                if (edit) {
                    ApiHttpClientCaller.call(
                            "booking-confirmation-form/" + id, PUT, dto
                    );
                } else {
                    ApiHttpClientCaller.call(
                            "booking-confirmation-form", POST, dto
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
                    "booking-confirmation-form/"+id, DELETE, null
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
//                createBoldRow("Booking Date:", d.getBookingDate().format(fmt)),
//                createBoldRow("Số ngày thuê:", String.valueOf(d.getRentalDays()))
        );

        String bd = d.getBookingDate() == null
                ? "–"
                : d.getBookingDate().format(fmt);
        detailPane.getChildren().add(createBoldRow("Booking Date:", bd));

        int rd = d.getRentalDays();
        detailPane.getChildren().add(createBoldRow("Số ngày thuê:", String.valueOf(rd)));

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
