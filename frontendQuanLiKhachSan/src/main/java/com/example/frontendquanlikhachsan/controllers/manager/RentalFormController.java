//TODO: chỉnh lại thông báo khi tìm guest k hợp lệ do
// đã có trong details, gọi thêm endpoint tính lại

package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.rentalForm.RentalFormDto;
import com.example.frontendquanlikhachsan.entity.rentalForm.ResponseRentalFormDto;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.RentalFormDetailDto;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.ResponseRentalFormDetailDto;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.RentalExtensionFormDto;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.ResponseRentalExtensionFormDto;
import com.example.frontendquanlikhachsan.entity.guest.SearchGuestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RentalFormController {

    @FXML private TableView<ResponseRentalFormDto> tableForm;
    @FXML private TableColumn<ResponseRentalFormDto,Integer> colId;
    @FXML private TableColumn<ResponseRentalFormDto,String> colRoomName;
    @FXML private TableColumn<ResponseRentalFormDto,String> colStaffName;
    @FXML private TableColumn<ResponseRentalFormDto,String> colDate;
    @FXML private TableColumn<ResponseRentalFormDto,Short> colDays;
    @FXML private TableColumn<ResponseRentalFormDto,String> colNote;
    @FXML private TableColumn<ResponseRentalFormDto,String> colPaidAt;
    @FXML private VBox detailPane;

    @FXML private TextField   tfFilterRoom, tfFilterStaff;
    @FXML private DatePicker  dpFrom, dpTo;
    @FXML private ComboBox<String> cbPaid;

    @FXML private TextField tfFilterId;

    private FilteredList<ResponseRentalFormDto> filteredForms;

    private List<Integer> multiFilterFormIds = null;

    @FXML private Button btnReset;    // nhớ bind cái nút Reset này trong FXML

    private final ObservableList<ResponseRentalFormDto> formList = FXCollections.observableArrayList();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String token = ""; // TODO: gán token
    private ResponseRentalFormDto editingDto;

    @FXML
    public void initialize() {
        // 1. Khởi tạo FilteredList
        filteredForms = new FilteredList<>(formList, f -> true);

        btnReset.setOnAction(e -> onResetFilter());

        // 2. Set vào TableView
        SortedList<ResponseRentalFormDto> sorted = new SortedList<>(filteredForms);
        sorted.comparatorProperty().bind(tableForm.comparatorProperty());
        tableForm.setItems(sorted);

        tfFilterId.textProperty().addListener((obs, old, neu) -> applyFilters());
        tfFilterRoom.textProperty().addListener((obs, old, neu) -> applyFilters());
        tfFilterStaff.textProperty().addListener((obs, old, neu) -> applyFilters());

        tfFilterRoom.setOnKeyReleased(e -> applyFilters());
        tfFilterStaff.setOnKeyReleased(e -> applyFilters());
        tfFilterId.setOnKeyReleased(e -> applyFilters());

        dpFrom.valueProperty().addListener((o,oldV,newV) -> applyFilters());
        dpTo.valueProperty().addListener((o,oldV,newV) -> applyFilters());
        cbPaid.valueProperty().addListener((o,oldV,newV) -> applyFilters());

        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colRoomName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getRoomName()));
        colStaffName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStaffName()));
        colDate.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getRentalDate().toString()));
        colNote.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(Optional.ofNullable(cd.getValue().getNote()).orElse("–")));
        colPaidAt.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(Optional.ofNullable(cd.getValue().getIsPaidAt()).map(Object::toString).orElse("–")));
        colDays.setCellValueFactory(cd -> {
            ResponseRentalFormDto dto = cd.getValue();
            // base days
            int total = dto.getNumberOfRentalDays();
            // fetch all extensions for this form and sum their days
            try {
                String json = ApiHttpClientCaller.call(
                        "rental-extension-form/rental-form/" + dto.getId(),
                        ApiHttpClientCaller.Method.GET, null
                );
                List<ResponseRentalExtensionFormDto> exts =
                        mapper.readValue(json, new TypeReference<List<ResponseRentalExtensionFormDto>>(){});
                total += exts.stream()
                        .mapToInt(ResponseRentalExtensionFormDto::getNumberOfRentalDays)
                        .sum();
            } catch(Exception e) {
                // swallow—just show base days if fetch fails
            }
            return new ReadOnlyObjectWrapper<>((short) total);
        });

        cbPaid.setItems(FXCollections.observableArrayList("All", "Paid", "Unpaid"));
        cbPaid.getSelectionModel().select("All");

        loadForms();
        tableForm.getSelectionModel().selectedItemProperty()
                .addListener((o,old,n)->{ if(n!=null) showDetail(n); });
    }
    @FXML
    private void onFilterAction() {
        applyFilters();
    }

    @FXML
    private void onCreateOrEditForm() {
        showForm(null);
    }

    private void loadForms() {
        try {
            String json = ApiHttpClientCaller.call("rental-form", ApiHttpClientCaller.Method.GET, null);
            List<ResponseRentalFormDto> list = mapper.readValue(json, new TypeReference<List<ResponseRentalFormDto>>(){});
            formList.setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải", "Không thể tải phiếu thuê.");
        }
    }

    @FXML
    private void onResetFilter() {
        // clear chế độ “xem liên quan”
        multiFilterFormIds = null;
        // reset UI controls nếu cần
        tfFilterId.clear();
        tfFilterRoom.clear();
        tfFilterStaff.clear();
        dpFrom.setValue(null);
        dpTo.setValue(null);
        cbPaid.getSelectionModel().select("All");
        // áp lại filters
        applyFilters();
    }

    public void selectRentalFormsByIds(List<Integer> rentalFormIds) {
        // bật chế độ “xem liên quan”
        multiFilterFormIds = new ArrayList<>(rentalFormIds);
        // multi-select nếu muốn
        tableForm.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // áp filter ngay
        applyFilters();
        // select & show detail của item đầu
        if (!rentalFormIds.isEmpty()) {
            int first = rentalFormIds.get(0);
            tableForm.getItems().stream()
                    .filter(rf -> rf.getId() == first)
                    .findFirst()
                    .ifPresent(this::showDetail);
        }
    }

    private void showDetail(ResponseRentalFormDto dto) {
        detailPane.getChildren().clear();

        Label title = new Label("Chi tiết Phiếu thuê – ID: " + dto.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        // Basic info
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String, Label> lb = t -> { Label l = new Label(t); l.setStyle("-fx-font-weight:bold;"); return l; };
        grid.add(lb.apply("Phòng:"), 0, 0);
        grid.add(new Label(dto.getRoomName()), 1, 0);
        grid.add(lb.apply("Nhân viên:"), 0, 1);
        grid.add(new Label(dto.getStaffName()), 1, 1);
        grid.add(lb.apply("Ngày thuê:"), 0, 2);
        grid.add(new Label(dto.getRentalDate().toString()), 1, 2);
        grid.add(lb.apply("Số ngày thuê lúc đầu:"), 0, 3);
        grid.add(new Label(dto.getNumberOfRentalDays().toString()), 1, 3);
        grid.add(lb.apply("Ghi chú:"), 0, 4);
        grid.add(new Label(Optional.ofNullable(dto.getNote()).orElse("–")), 1, 4);
        grid.add(lb.apply("Tạo lúc:"), 0, 5);
        grid.add(new Label(dto.getCreatedAt().toString()), 1, 5);
        grid.add(lb.apply("Thanh toán:"), 0, 6);
        grid.add(new Label(Optional.ofNullable(dto.getIsPaidAt()).map(Object::toString).orElse("–")), 1, 6);

        Accordion acc = new Accordion();

        // Details from IDs
        ListView<ResponseRentalFormDetailDto> lvD = new ListView<>();
        for (Integer detId : dto.getRentalFormDetailIds()) {
            try {
                String json = ApiHttpClientCaller.call("rental-form-detail/" + detId, ApiHttpClientCaller.Method.GET, null);
                ResponseRentalFormDetailDto detail = mapper.readValue(json, ResponseRentalFormDetailDto.class);
                lvD.getItems().add(detail);
            } catch (Exception ignored) {}
        }
        lvD.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ResponseRentalFormDetailDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty||item==null?null:"#"+item.getId()+" – "+item.getGuestName());
            }
        });
        //double click sửa chi tiết
        lvD.setOnMouseClicked(e -> { if (e.getClickCount()==2) showDetailForm(dto, lvD.getSelectionModel().getSelectedItem()); });

        lvD.setPrefHeight(lvD.getItems().size()*24+2);


        // delete button below the list
        Button btnDelDetail = new Button("🗑️ Xóa chi tiết");
        btnDelDetail.setOnAction(e -> {
            ResponseRentalFormDetailDto sel = lvD.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Xóa chi tiết #" + sel.getId() + "?", ButtonType.OK, ButtonType.CANCEL);
            a.showAndWait().filter(b -> b==ButtonType.OK).ifPresent(b2 -> {
                try {
                    ApiHttpClientCaller.call(
                            "rental-form-detail/" + sel.getId(),
                            ApiHttpClientCaller.Method.DELETE,
                            null
                    );
                    showDetail(dto); // reload detail view
                } catch (Exception ex) {
                    showErrorAlert("Không thể xóa chi tiết", ex.getMessage());
                }
            });
        });

        HBox detailButtons = new HBox(6);
        detailButtons.getChildren().addAll(
                new Button("➕ Thêm chi tiết") {{ setOnAction(ev->showDetailForm(dto,null)); }},
                btnDelDetail
        );

        VBox boxD = new VBox(6, detailButtons, lvD);
        boxD.setPadding(new Insets(8,0,0,0));
        acc.getPanes().add(new TitledPane("Chi tiết", boxD));

        // Extensions from IDs
        ListView<ResponseRentalExtensionFormDto> lvE = new ListView<>();
        for (Integer extId : dto.getRentalExtensionFormIds()) {
            try {
                String json = ApiHttpClientCaller.call("rental-extension-form/" + extId, ApiHttpClientCaller.Method.GET, null);
                ResponseRentalExtensionFormDto ext = mapper.readValue(json, ResponseRentalExtensionFormDto.class);
                lvE.getItems().add(ext);
            } catch (Exception ignored) {}
        }
        lvE.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ResponseRentalExtensionFormDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty||item==null?null:"#"+item.getId()+" – "+item.getNumberOfRentalDays()+" ngày");
            }
        });
        lvE.setOnMouseClicked(e -> { if (e.getClickCount()==2) showExtensionForm(dto, lvE.getSelectionModel().getSelectedItem()); });
        lvE.setPrefHeight(lvE.getItems().size()*24+2);

        Button btnDelExt = new Button("🗑️ Xóa gia hạn");
        btnDelExt.setOnAction(e -> {
            ResponseRentalExtensionFormDto sel = lvE.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Xóa gia hạn #" + sel.getId() + "?", ButtonType.OK, ButtonType.CANCEL);
            a.showAndWait().filter(b -> b==ButtonType.OK).ifPresent(b2 -> {
                try {
                    ApiHttpClientCaller.call(
                            "rental-extension-form/" + sel.getId(),
                            ApiHttpClientCaller.Method.DELETE,
                            null
                    );
                    showDetail(dto);
                } catch (Exception ex) {
                    showErrorAlert("Không thể xóa gia hạn", ex.getMessage());
                }
            });
        });

        HBox extensionButtons = new HBox(6);
        extensionButtons.getChildren().addAll(
                new Button("➕ Thêm gia hạn") {{ setOnAction(ev->showExtensionForm(dto,null)); }},
                btnDelExt
        );

        VBox boxE = new VBox(6, extensionButtons ,lvE );
        boxE.setPadding(new Insets(8,0,0,0));
        acc.getPanes().add(new TitledPane("Gia hạn", boxE));

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(12,0,0,0));
        actions.getChildren().addAll(
                new Button("✏️ Sửa") {{ setOnAction(ev->showForm(dto)); }},
                new Button("🗑️ Xóa") {{ setOnAction(ev->deleteForm(dto)); }}
        );

        detailPane.getChildren().addAll(title, grid, acc, actions);
    }

    private void deleteForm(ResponseRentalFormDto dto) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa phiếu thuê này?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try {
                ApiHttpClientCaller.call("rental-form/" + dto.getId(), ApiHttpClientCaller.Method.DELETE, null);
                loadForms();
            } catch (Exception e) {
                showErrorAlert("Lỗi", "Không thể xóa.");
            }
        });
    }

    private void showForm(ResponseRentalFormDto dto) {
        editingDto = dto;
        detailPane.getChildren().clear();

        Label title = new Label(dto == null ? "➕ Tạo Phiếu thuê mới" : "✏️ Sửa Phiếu thuê #" + dto.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        if (dto != null) {
            // dto.getRoomId() là roomId cũ,
            // showFormFields tự động điền sẵn các giá trị dựa trên dto
            showFormFields(detailPane, dto, dto.getRoomId());
            return;
        }

        // Step 1: Room ID + Check
        GridPane step1 = new GridPane();
        step1.setHgap(10);
        step1.setVgap(10);
        step1.setPadding(new Insets(8));
        Function<String, Label> makeLabel = txt -> {
            Label l = new Label(txt);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };

        step1.add(makeLabel.apply("ID Phòng:"), 0, 0);
        TextField tfRoom = new TextField(dto == null ? "" : String.valueOf(dto.getRoomId()));
        step1.add(tfRoom, 1, 0);
        Button btnCheck = new Button("🔍 Kiểm tra");
        step1.add(btnCheck, 2, 0);
        Label lblMsg = new Label();
        step1.add(lblMsg, 1, 1, 2, 1);

        // Step 2: Container cho form thật sự
        VBox step2 = new VBox(8);
        step2.setPadding(new Insets(10, 0, 0, 0));

        btnCheck.setOnAction(evt -> {
            try {
                int roomId = Integer.parseInt(tfRoom.getText().trim());
                String roomJson = ApiHttpClientCaller.call(
                        "room/" + roomId,
                        ApiHttpClientCaller.Method.GET,
                        null
                );
                JsonNode roomNode = mapper.readTree(roomJson);
                String state = roomNode.get("roomState").asText();
                if (!"READY_TO_SERVE".equals(state)) {
                    lblMsg.setText("Phòng không sẵn sàng");
                    step2.getChildren().clear();
                    return;
                }
                lblMsg.setText("");
                // Hiển thị form nhập/chỉnh sửa
                showFormFields(step2, dto, roomId);
            } catch (NumberFormatException ex) {
                lblMsg.setText("ID phòng phải là số");
                step2.getChildren().clear();
            } catch (Exception ex) {
                ex.printStackTrace();
                lblMsg.setText("Lỗi kết nối");
                step2.getChildren().clear();
            }
        });

        detailPane.getChildren().addAll(title, step1, step2);
    }

    // phương thức phụ hiển thị các trường nhập/chỉnh sửa sau khi check phòng OK
    private void showFormFields(VBox container, ResponseRentalFormDto dto, int roomId) {
        container.getChildren().clear();
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(8));
        Function<String, Label> makeLabel = txt -> {
            Label l = new Label(txt);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };

        // Nhân viên
        form.add(makeLabel.apply("ID Nhân viên:"), 0, 0);
        TextField tfStaff = new TextField(dto == null ? "" : String.valueOf(dto.getStaffId()));
        form.add(tfStaff, 1, 0);

        // Ngày thuê
        form.add(makeLabel.apply("Ngày thuê (ISO):"), 0, 1);
        TextField tfDate = new TextField(dto == null
                ? LocalDateTime.now().toString()
                : dto.getRentalDate().toString());
        form.add(tfDate, 1, 1);

        // Số ngày
        form.add(makeLabel.apply("Số ngày:"), 0, 2);
        TextField tfDays = new TextField(dto == null
                ? "1"
                : String.valueOf(dto.getNumberOfRentalDays()));
        form.add(tfDays, 1, 2);

        // Ghi chú
        form.add(makeLabel.apply("Ghi chú:"), 0, 3);
        TextField tfNote = new TextField(dto == null ? "" : Optional.ofNullable(dto.getNote()).orElse(""));
        form.add(tfNote, 1, 3);

        // Thanh toán
        form.add(makeLabel.apply("Đã thanh toán (ISO):"), 0, 4);
        TextField tfPaid = new TextField(dto == null
                ? ""
                : Optional.ofNullable(dto.getIsPaidAt()).map(Object::toString).orElse(""));
        form.add(tfPaid, 1, 4);

        // Nút Lưu / Hủy
        Button btnSave = new Button(dto == null ? "💾 Tạo" : "💾 Lưu");
        Button btnCancel = new Button("❌ Hủy");
        HBox btnBox = new HBox(8, btnSave, btnCancel);
        btnBox.setPadding(new Insets(12, 0, 0, 0));
        form.add(btnBox, 1, 5, 2, 1);

        // Xử lý lưu
        btnSave.setOnAction(e -> {
            try {
                LocalDateTime rentalDate;
                LocalDateTime paidDate = null;

                try {
                    rentalDate = LocalDateTime.parse(tfDate.getText().trim());
                } catch (Exception ex) {
                    showErrorAlert("Invalid Date Format", "Rental date should be in ISO format (yyyy-MM-ddTHH:mm:ss)");
                    return;
                }

                if (!tfPaid.getText().isBlank()) {
                    try {
                        paidDate = LocalDateTime.parse(tfPaid.getText().trim());
                    } catch (Exception ex) {
                        showErrorAlert("Invalid Date Format", "Payment date should be in ISO format (yyyy-MM-ddTHH:mm:ss)");
                        return;
                    }
                }

                RentalFormDto payload = RentalFormDto.builder()
                        .roomId(roomId)
                        .staffId(Integer.parseInt(tfStaff.getText().trim()))
                        .rentalDate(rentalDate)
                        .numberOfRentalDays(Short.parseShort(tfDays.getText().trim()))
                        .note(tfNote.getText().trim())
                        .isPaidAt(paidDate)
                        .build();

                // trong btnSave.setOnAction:
                if (editingDto == null) {
                    ApiHttpClientCaller.call("rental-form",
                            ApiHttpClientCaller.Method.POST, payload);
                    showInfoAlert("Tạo thành công", "Đã tạo Phiếu thuê mới");
                } else {
                    ApiHttpClientCaller.call("rental-form/" + editingDto.getId(),
                            ApiHttpClientCaller.Method.PUT, payload);
                    showInfoAlert("Cập nhật thành công", "Đã cập nhật Phiếu thuê #" + editingDto.getId());
                }
                loadForms();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Lỗi lưu", ex.getMessage());
            }
        });

        // Hủy về chi tiết (nếu sửa) hoặc clear pane (nếu tạo mới)
        btnCancel.setOnAction(e -> {
            if (editingDto != null) showDetail(editingDto);
            else detailPane.getChildren().clear();
        });

        container.getChildren().add(form);
    }

    private void showDetailForm(ResponseRentalFormDto parent, ResponseRentalFormDetailDto detail) {
        detailPane.getChildren().clear();
        Label title = new Label(detail == null
                ? "➕ Thêm Chi tiết"
                : "✏️ Sửa Chi tiết #" + detail.getId());
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        // --- Form container ---
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(8));
        Function<String, Label> lb = t -> {
            Label l = new Label(t);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };

        // ID Guest
        form.add(lb.apply("ID Guest:"), 0, 0);
        TextField tfId = new TextField(detail == null ? "" : String.valueOf(detail.getGuestId()));
        form.add(tfId, 1, 0);

        // CMND
        form.add(lb.apply("CMND:"), 0, 1);
        TextField tfIdNum = new TextField(detail == null ? "" : detail.getGuestIdentificationNumber());
        form.add(tfIdNum, 1, 1);

        // Email
        form.add(lb.apply("Email:"), 0, 2);
        TextField tfEmail = new TextField(detail == null ? "" : detail.getGuestEmail());
        form.add(tfEmail, 1, 2);

        // SĐT
        form.add(lb.apply("SĐT:"), 0, 3);
        TextField tfPhone = new TextField(detail == null ? "" : detail.getGuestPhoneNumber());
        form.add(tfPhone, 1, 3);

        // Nút tìm
        Button btnSearch = new Button("🔍 Tìm");
        form.add(btnSearch, 2, 0, 1, 4);

        // ComboBox kết quả
        ComboBox<SearchGuestDto> cbResults = new ComboBox<>();
        form.add(lb.apply("Kết quả:"), 0, 4);
        form.add(cbResults, 1, 4, 2, 1);

        // Nút Lưu / Hủy
        HBox btns = new HBox(8);
        Button btnSave = new Button(detail == null ? "💾 Thêm" : "💾 Lưu");
        Button btnCancel = new Button("❌ Hủy");
        btns.getChildren().addAll(btnSave, btnCancel);
        form.add(btns, 1, 5, 2, 1);

        // --- Xử lý tìm Guest ---
        btnSearch.setOnAction(evt -> {
            try {
                // 1) Build request body
                SearchGuestDto req = new SearchGuestDto();
                if (!tfId.getText().isBlank())
                    req.setId(Integer.parseInt(tfId.getText().trim()));
                req.setIdentificationNumber(tfIdNum.getText().isBlank() ? null : tfIdNum.getText().trim());
                req.setEmail(tfEmail.getText().isBlank() ? null : tfEmail.getText().trim());
                req.setPhoneNumber(tfPhone.getText().isBlank() ? null : tfPhone.getText().trim());

                // 2) POST /guest/search
                String searchJson = ApiHttpClientCaller.call(
                        "guest/search",
                        ApiHttpClientCaller.Method.POST,
                        req
                );
                List<SearchGuestDto> respList = mapper.readValue(
                        searchJson, new TypeReference<List<SearchGuestDto>>() {});

                // 3) GET /rental-form/{id}/guest-ids
                String idsJson = ApiHttpClientCaller.call(
                        "rental-form/" + parent.getId() + "/guest-ids",
                        ApiHttpClientCaller.Method.GET,
                        null
                );
                List<Integer> takenIds = mapper.readValue(
                        idsJson, new TypeReference<List<Integer>>() {});

                // 4) Lọc trùng
                List<SearchGuestDto> filtered = respList.stream()
                        .filter(g -> !takenIds.contains(g.getId()))
                        .toList();

                cbResults.setItems(FXCollections.observableArrayList(filtered));
                if (filtered.isEmpty()) {
                    showInfoAlert("Không tìm thấy",
                            "Không có khách phù hợp hoặc đã tồn tại trong phiếu.");
                }
            } catch (Exception e) {
                showErrorAlert("Lỗi tìm guest", e.getMessage());
            }
        });

        // --- Xử lý Lưu Chi tiết ---
        btnSave.setOnAction(evt -> {
            SearchGuestDto sel = cbResults.getValue();
            if (sel == null) {
                showErrorAlert("Lỗi","Chưa chọn guest");
                return;
            }
            // Kiểm tra duplicate lần cuối
            try {
                String idsJson = ApiHttpClientCaller.call(
                        "rental-form/" + parent.getId() + "/guest-ids",
                        ApiHttpClientCaller.Method.GET,
                        null
                );
                List<Integer> takenIds = mapper.readValue(
                        idsJson, new TypeReference<List<Integer>>() {});
                if (takenIds.contains(sel.getId())) {
                    showErrorAlert("Lỗi","Guest đã tồn tại trong chi tiết rồi.");
                    return;
                }
            } catch (Exception ignored) {}

            try {
                RentalFormDetailDto dto = new RentalFormDetailDto();
                dto.setRentalFormId(parent.getId());
                dto.setGuestId(sel.getId());
                String ep = detail == null
                        ? "rental-form-detail"
                        : "rental-form-detail/" + detail.getId();
                ApiHttpClientCaller.call(
                        ep,
                        detail == null
                                ? ApiHttpClientCaller.Method.POST
                                : ApiHttpClientCaller.Method.PUT,
                        dto
                );
                showInfoAlert("Thành công",
                        detail == null ? "Đã thêm detail" : "Đã cập nhật detail");

                // Load lại dữ liệu và quay về detail view
                ResponseRentalFormDto updated = getRefreshedRentalForm(parent.getId());
                showDetail(updated != null ? updated : parent);
            } catch (Exception e) {
                showErrorAlert("Lỗi lưu detail", e.getMessage());
            }
        });

        // Hủy về detail view
        btnCancel.setOnAction(evt -> showDetail(parent));

        detailPane.getChildren().addAll(title, form);
    }


    // Create a method to fetch fresh data for a specific rental form
    private ResponseRentalFormDto getRefreshedRentalForm(int formId) {
        try {
            String json = ApiHttpClientCaller.call("rental-form/" + formId,
                    ApiHttpClientCaller.Method.GET, null);
            return mapper.readValue(json, ResponseRentalFormDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải", "Không thể tải dữ liệu phiếu thuê #" + formId);
            return null;
        }
    }

    private void showExtensionForm(ResponseRentalFormDto parent, ResponseRentalExtensionFormDto ext) {
        detailPane.getChildren().clear();

        Label title = new Label(ext == null
                ? "➕ Thêm Gia hạn"
                : "✏️ Sửa Gia hạn #" + ext.getId());
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");
        detailPane.getChildren().add(title);

        // --- Nếu là Edit, skip luôn phần check, show form edit ngay ---
        if (ext != null) {
            GridPane form = new GridPane();
            form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(8));
            Function<String,Label> lb = t->{ Label l=new Label(t); l.setStyle("-fx-font-weight:bold;"); return l; };

            form.add(lb.apply("Số ngày gia hạn:"), 0, 0);
            TextField tfDays = new TextField(String.valueOf(ext.getNumberOfRentalDays()));
            form.add(tfDays, 1, 0);

            form.add(lb.apply("ID Nhân viên:"), 0, 1);
            TextField tfStaff = new TextField(String.valueOf(ext.getStaffId()));
            form.add(tfStaff, 1, 1);

            Button btnSave = new Button("💾 Lưu");
            Button btnCancel = new Button("❌ Hủy");
            HBox hb = new HBox(8, btnSave, btnCancel);
            hb.setPadding(new Insets(12,0,0,0));
            form.add(hb, 1, 2, 2, 1);

            btnSave.setOnAction(evt -> {
                try {
                    short days = Short.parseShort(tfDays.getText().trim());
                    RentalExtensionFormDto dto = RentalExtensionFormDto.builder()
                            .rentalFormId(parent.getId())
                            .numberOfRentalDays(days)
                            .staffId(Integer.parseInt(tfStaff.getText().trim()))
                            .build();
                    ApiHttpClientCaller.call(
                            "rental-extension-form/" + ext.getId(),
                            ApiHttpClientCaller.Method.PUT,
                            dto);
                    showInfoAlert("Cập nhật thành công", "Gia hạn đã được cập nhật");
                    loadForms();

                    // Get fresh data
                    ResponseRentalFormDto refreshedParent = getRefreshedRentalForm(parent.getId());
                    if (refreshedParent != null) {
                        showDetail(refreshedParent);
                    } else {
                        showDetail(parent);
                    }
                } catch (Exception ex) {
                    showErrorAlert("Lỗi lưu gia hạn", ex.getMessage());
                }
            });
            btnCancel.setOnAction(evt -> showDetail(parent));

            detailPane.getChildren().add(form);
            return;
        }

        // --- Nếu là Thêm mới, giữ nguyên phần kiểm tra ngày remains + tạo ---
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String,Label> lb = t->{ Label l=new Label(t); l.setStyle("-fx-font-weight:bold;"); return l; };

        grid.add(lb.apply("Phiếu thuê ID:"), 0, 0);
        grid.add(new Label(String.valueOf(parent.getId())), 1, 0);
        grid.add(lb.apply("Kiểm tra ngày còn lại:"), 0, 1);
        Button btnCheck = new Button("🔍 Kiểm tra");
        grid.add(btnCheck, 1, 1);
        Label lblInfo = new Label();
        grid.add(lblInfo, 1, 2);
        Button btnCancel = new Button("❌ Hủy");
        btnCancel.setOnAction(e -> showDetail(parent));
        grid.add(btnCancel, 2, 2);
        VBox step2 = new VBox(8);
        step2.setPadding(new Insets(10,0,0,0));

        btnCheck.setOnAction(e -> {
            try {
                String json = ApiHttpClientCaller.call(
                        "rental-extension-form/day-remains/" + parent.getId(),
                        ApiHttpClientCaller.Method.GET, null);
                int remains = mapper.readValue(json, Integer.class);
                if (remains <= 0) {
                    lblInfo.setText("Không thể gia hạn thêm");
                    step2.getChildren().clear();
                } else {
                    grid.getChildren().remove(btnCancel);
                    lblInfo.setText("");
                    GridPane g2 = new GridPane();
                    g2.setHgap(8); g2.setVgap(8);
                    g2.add(lb.apply("Số ngày (1–" + Math.min(remains,5) + "):"), 0, 0);
                    TextField tfDays = new TextField();
                    g2.add(tfDays, 1, 0);

                    g2.add(lb.apply("ID Nhân viên:"), 0, 1);
                    TextField tfStaff = new TextField();
                    g2.add(tfStaff, 1, 1);

                    Button btnSave2 = new Button("💾 Thêm");
                    Button btnCancel2 = new Button("❌ Hủy");
                    HBox btns2 = new HBox(8);
                    btns2.getChildren().addAll(btnSave2, btnCancel2);
                    btns2.setPadding(new Insets(12,0,0,0));
                    g2.add(btns2, 1, 2, 2, 1);

                    btnCancel2.setOnAction(ev -> showDetail(parent));

                    btnSave2.setOnAction(ev -> {
                        try {
                            short days = Short.parseShort(tfDays.getText().trim());
                            int max = Math.min(remains,5);
                            if (days<1 || days>max) {
                                showErrorAlert("Lỗi","Số ngày phải từ 1 đến " + max);
                                return;
                            }
                            RentalExtensionFormDto dto = RentalExtensionFormDto.builder()
                                    .rentalFormId(parent.getId())
                                    .numberOfRentalDays(days)
                                    .staffId(Integer.parseInt(tfStaff.getText().trim()))
                                    .build();
                            ApiHttpClientCaller.call(
                                    "rental-extension-form",
                                    ApiHttpClientCaller.Method.POST,
                                    dto);
                            showInfoAlert("Tạo thành công","Gia hạn đã được tạo");
                            loadForms();

                            // Get fresh data
                            ResponseRentalFormDto refreshedParent = getRefreshedRentalForm(parent.getId());
                            if (refreshedParent != null) {
                                showDetail(refreshedParent);
                            } else {
                                showDetail(parent);
                            }
                        } catch (Exception ex) {
                            showErrorAlert("Lỗi lưu gia hạn", ex.getMessage());
                        }
                    });

                    step2.getChildren().setAll(g2);
                }
            } catch (Exception ex) {
                lblInfo.setText("Lỗi kiểm tra");
                step2.getChildren().clear();
            }
        });

        detailPane.getChildren().addAll(grid, step2);
    }

    private void showInfoAlert(String h, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(h);
        a.setContentText(c);
        a.showAndWait();
    }

    private void showErrorAlert(String h, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(h);
        a.setContentText(c);
        a.showAndWait();
    }

    private void applyFilters() {
        String idText    = Optional.ofNullable(tfFilterId.getText()).orElse("").trim();
        String roomText  = Optional.ofNullable(tfFilterRoom.getText()).orElse("").trim().toLowerCase();
        String staffText = Optional.ofNullable(tfFilterStaff.getText()).orElse("").trim().toLowerCase();
        LocalDate from   = dpFrom.getValue();
        LocalDate to     = dpTo.getValue();
        String paidSel   = cbPaid.getValue();

        // nếu user đã gõ hoặc chọn gì đó, tắt luôn chế độ multi-filter
        boolean hasAny = !idText.isEmpty() || !roomText.isEmpty() ||
                !staffText.isEmpty() || from!=null || to!=null ||
                (paidSel!=null && !"All".equals(paidSel));
        if (multiFilterFormIds != null && hasAny) {
            multiFilterFormIds = null;
            tableForm.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }

        // giữ nguyên nhánh multiFilter nếu chưa clear
        if (multiFilterFormIds != null) {
            filteredForms.setPredicate(f -> multiFilterFormIds.contains(f.getId()));
            return;
        }

        // nhánh lọc bình thường
        filteredForms.setPredicate(f -> {
            // 0) ID
            if (!idText.isEmpty()) {
                try {
                    if (f.getId() != Integer.parseInt(idText)) return false;
                } catch (NumberFormatException ex) { return false; }
            }
            // 1) phòng
            if (!roomText.isEmpty()
                    && !f.getRoomName().toLowerCase().contains(roomText)) return false;
            // 2) nhân viên
            if (!staffText.isEmpty()
                    && !f.getStaffName().toLowerCase().contains(staffText)) return false;
            // 3) ngày thuê
            LocalDate rent = f.getRentalDate().toLocalDate();
            if (from!=null && rent.isBefore(from)) return false;
            if (to  !=null && rent.isAfter(to))   return false;
            // 4) paid/unpaid
            if ("Paid".equals(paidSel)   && f.getIsPaidAt()==null) return false;
            if ("Unpaid".equals(paidSel) && f.getIsPaidAt()!=null) return false;
            return true;
        });

        // thêm dòng này để TableView vẽ lại
        tableForm.refresh();
    }

}
