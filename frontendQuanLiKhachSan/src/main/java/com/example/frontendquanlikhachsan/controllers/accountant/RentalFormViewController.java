    package com.example.frontendquanlikhachsan.controllers.accountant;

    import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
    import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
    import com.example.frontendquanlikhachsan.entity.guest.SearchGuestDto;
    import com.example.frontendquanlikhachsan.entity.invoice.InvoiceDto;
    import com.example.frontendquanlikhachsan.entity.invoice.ResponseInvoiceDto;
    import com.example.frontendquanlikhachsan.entity.rentalForm.ResponseRentalFormDto;
    import com.example.frontendquanlikhachsan.entity.rentalFormDetail.RentalFormDetailDto;
    import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.RentalExtensionFormDto;
    import com.example.frontendquanlikhachsan.entity.rentalFormDetail.ResponseRentalFormDetailDto;
    import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.ResponseRentalExtensionFormDto;
    import com.fasterxml.jackson.core.type.TypeReference;
    import com.fasterxml.jackson.databind.ObjectMapper;
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

    import java.time.LocalDate;
    import java.util.List;
    import java.util.Optional;
    import java.util.function.Function;

    public class RentalFormViewController {

        // filter controls
        @FXML private TextField tfFilterId, tfFilterRoom, tfFilterStaff;
        @FXML private DatePicker dpFrom, dpTo;
        @FXML private ComboBox<String> cbPaid;
        @FXML private Button btnReset;

        // table & columns
        @FXML private TableView<ResponseRentalFormDto> tableForm;
        @FXML private TableColumn<ResponseRentalFormDto,Integer> colId;
        @FXML private TableColumn<ResponseRentalFormDto,String>  colRoomName;
        @FXML private TableColumn<ResponseRentalFormDto,String>  colStaffName;
        @FXML private TableColumn<ResponseRentalFormDto,String>  colDate;
        @FXML private TableColumn<ResponseRentalFormDto,Short>   colDays;
        @FXML private TableColumn<ResponseRentalFormDto,String>  colNote;
        @FXML private TableColumn<ResponseRentalFormDto,String>  colPaidAt;

        // detail pane & action buttons
        @FXML private VBox   detailPane;
        @FXML private Button btnAddDetail, btnAddExtension, btnCheckout;
        @FXML private Button btnLapHoaDon;

        private boolean isInvoiceMode = false;
        private TableRow<ResponseRentalFormDto> lastRowInvoiceMode = null; // để lưu lại row khi bật invoice mode

        private final javafx.beans.value.ChangeListener<ResponseRentalFormDto> detailListener = (o, old, sel) -> {
            if(sel != null) showDetail(sel);
        };


        private final ObservableList<ResponseRentalFormDto> formList = FXCollections.observableArrayList();
        private FilteredList<ResponseRentalFormDto> filteredForms;
        private final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        private void resetRowAction() {
            tableForm.setRowFactory(null);
            tableForm.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // Remove hết listener (phòng trường hợp bị double add)
            tableForm.getSelectionModel().selectedItemProperty().removeListener(detailListener);
            // Gắn lại listener xem chi tiết
            tableForm.getSelectionModel().selectedItemProperty().addListener(detailListener);

            isInvoiceMode = false;
        }

        private void enableInvoiceRowAction(ObservableList<ResponseRentalFormDto> selectedForms) {
            // Ngắt đúng cái listener detail
            tableForm.getSelectionModel().selectedItemProperty().removeListener(detailListener);

            tableForm.setRowFactory(tv -> {
                TableRow<ResponseRentalFormDto> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        ResponseRentalFormDto sel = row.getItem();
                        if (!selectedForms.contains(sel)) selectedForms.add(sel);
                    }
                });
                return row;
            });
            tableForm.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            isInvoiceMode = true;
        }


        @FXML
        public void initialize() {
            // 1) setup filtered + sorted list
            filteredForms = new FilteredList<>(formList, f->true);
            SortedList<ResponseRentalFormDto> sorted = new SortedList<>(filteredForms);
            sorted.comparatorProperty().bind(tableForm.comparatorProperty());
            tableForm.setItems(sorted);

            btnLapHoaDon.setOnAction(e -> showInvoiceCreatePane());
            // 2) bind columns
            colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
            colRoomName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getRoomName()));
            colStaffName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStaffName()));
            colDate.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getRentalDate().toString()));
            colDays.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getNumberOfRentalDays()));
            colNote.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(Optional.ofNullable(cd.getValue().getNote()).orElse("–")));
            colPaidAt.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(Optional.ofNullable(cd.getValue().getIsPaidAt()).map(Object::toString).orElse("–")));

            // 3) filter listeners
            tfFilterId.textProperty().addListener((o,old,v)->applyFilters());
            tfFilterRoom.textProperty().addListener((o,old,v)->applyFilters());
            tfFilterStaff.textProperty().addListener((o,old,v)->applyFilters());
            dpFrom.valueProperty().addListener((o,old,v)->applyFilters());
            dpTo.valueProperty().addListener((o,old,v)->applyFilters());
            cbPaid.setItems(FXCollections.observableArrayList("All","Paid","Unpaid"));
            cbPaid.getSelectionModel().select("All");
            cbPaid.valueProperty().addListener((o,old,v)->applyFilters());

            btnReset.setOnAction(e-> onResetFilter());

            // 4) row selection -> detail view
            tableForm.getSelectionModel().selectedItemProperty().addListener(detailListener);

            // 5) load initial data
            loadForms();
            resetRowAction();
        }

        @FXML
        private void showInvoiceCreatePane() {
            // 1) Clear pane cũ
            detailPane.getChildren().clear();

            // 2) Tạo section tìm khách
            HBox searchGuestBox = new HBox(8);
            TextField tfGuestId    = new TextField(); tfGuestId.setPromptText("ID");
            TextField tfGuestName  = new TextField(); tfGuestName.setPromptText("Tên");
            TextField tfGuestPhone = new TextField(); tfGuestPhone.setPromptText("SĐT");
            TextField tfGuestCmnd  = new TextField(); tfGuestCmnd.setPromptText("CMND");
            TextField tfGuestEmail = new TextField(); tfGuestEmail.setPromptText("Email");
            TextField tfGuestAccId = new TextField(); tfGuestAccId.setPromptText("AccountId");
            Button  btnSearchGuest = new Button("🔍 Tìm");
            searchGuestBox.getChildren().addAll(
                    tfGuestId, tfGuestName, tfGuestPhone,
                    tfGuestCmnd, tfGuestEmail, tfGuestAccId,
                    btnSearchGuest
            );

            ComboBox<ResponseGuestDto> cbGuestResults = new ComboBox<>();
            cbGuestResults.setPromptText("Chọn khách trả phòng");

            // 3) Bảng bên phải chứa phiếu thuê được chọn
            TableView<ResponseRentalFormDto> tableSelected = new TableView<>();
            TableColumn<ResponseRentalFormDto, Integer> colSelId   = new TableColumn<>("ID");
            TableColumn<ResponseRentalFormDto, String>  colSelRoom = new TableColumn<>("Phòng");
            TableColumn<ResponseRentalFormDto, Short>   colSelDays = new TableColumn<>("Tổng ngày");
            colSelId  .setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
            colSelRoom.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getRoomName()));
            colSelDays.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getNumberOfRentalDays()));
            tableSelected.getColumns().addAll(colSelId, colSelRoom, colSelDays);

            ObservableList<ResponseRentalFormDto> selectedForms = FXCollections.observableArrayList();
            tableSelected.setItems(selectedForms);

            // 4) Bật mode lập hoá đơn: remove detail-listener, clear selection, add double-click handler
            tableForm.getSelectionModel().selectedItemProperty().removeListener(detailListener);
            tableForm.getSelectionModel().clearSelection();

            tableForm.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    ResponseRentalFormDto sel = tableForm.getSelectionModel().getSelectedItem();
                    if (sel != null && !selectedForms.contains(sel)) {
                        selectedForms.add(sel);
                        System.out.println("Đã add phiếu thuê #" + sel.getId());
                    }
                }
            });

            // 5) Xử lý search guest
            btnSearchGuest.setOnAction(e -> {
                try {
                    SearchGuestDto req = new SearchGuestDto();
                    req.setId(tfGuestId.getText().isBlank()   ? null : Integer.parseInt(tfGuestId.getText().trim()));
                    req.setName(tfGuestName.getText().isBlank() ? null : tfGuestName.getText().trim());
                    req.setPhoneNumber(tfGuestPhone.getText().isBlank() ? null : tfGuestPhone.getText().trim());
                    req.setIdentificationNumber(tfGuestCmnd.getText().isBlank() ? null : tfGuestCmnd.getText().trim());
                    req.setEmail(tfGuestEmail.getText().isBlank() ? null : tfGuestEmail.getText().trim());
                    req.setAccountId(tfGuestAccId.getText().isBlank() ? null
                            : Integer.parseInt(tfGuestAccId.getText().trim()));

                    String json = ApiHttpClientCaller.call("guest/search",
                            ApiHttpClientCaller.Method.POST, req);
                    List<ResponseGuestDto> guests = mapper.readValue(
                            json, new TypeReference<List<ResponseGuestDto>>() {});
                    cbGuestResults.setItems(FXCollections.observableArrayList(guests));
                    if (guests.isEmpty()) {
                        showInfoAlert("Không tìm thấy", "Không có khách nào phù hợp!");
                    }
                } catch (Exception ex) {
                    showErrorAlert("Lỗi tìm kiếm khách", ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // 6) Nút Lập hoá đơn
            Button btnConfirm = new Button("Lập hoá đơn ✅");
            btnConfirm.setOnAction(e -> {
                if (selectedForms.isEmpty()) {
                    showErrorAlert("Thiếu dữ liệu", "Bạn chưa chọn phiếu thuê nào!");
                    return;
                }
                ResponseGuestDto guest = cbGuestResults.getValue();
                if (guest == null) {
                    showErrorAlert("Thiếu thông tin", "Vui lòng chọn khách trả phòng!");
                    return;
                }

                try {
                    // 1) Tạo hoá đơn cha, không nhập staffId, totalReservationCost = null
                    InvoiceDto dto = new InvoiceDto();
                    dto.setTotalReservationCost(0.0);
                    dto.setPayingGuestId(guest.getId());
                    dto.setStaffId(null);

                    String invJson = ApiHttpClientCaller.call(
                            "invoice",                         // ApiHttpClientCaller sẽ gắn /{impactorId}/{impactor}
                            ApiHttpClientCaller.Method.POST,
                            dto
                    );
                    ResponseInvoiceDto created = mapper.readValue(invJson, ResponseInvoiceDto.class);

                    // 2) Tạo detail cho từng phiếu thuê
                    for (ResponseRentalFormDto rf : selectedForms) {
                        ApiHttpClientCaller.call(
                                "invoice-detail/checkout/"
                                        + created.getId() + "/"
                                        + rf.getId(),                 // ApiHttpClientCaller sẽ gắn /{impactorId}/{impactor} luôn
                                ApiHttpClientCaller.Method.POST,
                                null
                        );
                    }

                    showInfoAlert("Thành công",
                            "Đã tạo hoá đơn #" + created.getId()
                                    + " và thanh toán " + selectedForms.size() + " phiếu.");

                    // Restore lại chế độ bình thường
                    tableForm.setOnMouseClicked(null);
                    resetRowAction();
                    detailPane.getChildren().clear();
                    loadForms();

                } catch (Exception ex) {
                    showErrorAlert("Lỗi lập hoá đơn", ex.getMessage());
                }
            });


            // 7) Nút Huỷ
            Button btnCancel = new Button("❌ Huỷ");
            btnCancel.setOnAction(e -> {
                tableForm.setOnMouseClicked(null);
                resetRowAction();
                detailPane.getChildren().clear();
            });

            // 8) Gói vào layout
            HBox actions = new HBox(10, btnConfirm, btnCancel);
            VBox vbox = new VBox(12,
                    searchGuestBox,
                    cbGuestResults,
                    tableSelected,
                    actions
            );
            vbox.setPadding(new Insets(16,24,16,24));
            detailPane.getChildren().add(vbox);
        }


        private void loadForms() {
            try {
                String json = ApiHttpClientCaller.call("rental-form", ApiHttpClientCaller.Method.GET, null);
                List<ResponseRentalFormDto> list = mapper.readValue(json, new TypeReference<>(){});
                formList.setAll(list);
            } catch(Exception ex) {
                showErrorAlert("Lỗi tải", "Không thể tải phiếu thuê.");
            }
        }

        private void applyFilters() {
            String idText    = Optional.ofNullable(tfFilterId.getText()).orElse("").trim();
            String roomText  = Optional.ofNullable(tfFilterRoom.getText()).orElse("").trim().toLowerCase();
            String staffText = Optional.ofNullable(tfFilterStaff.getText()).orElse("").trim().toLowerCase();
            LocalDate from   = dpFrom.getValue();
            LocalDate to     = dpTo.getValue();
            String paidSel   = cbPaid.getValue();

            filteredForms.setPredicate(f -> {
                if (!idText.isEmpty()) {
                    try { if (f.getId() != Integer.parseInt(idText)) return false; }
                    catch(Exception e){ return false; }
                }
                if (!roomText.isEmpty() && !f.getRoomName().toLowerCase().contains(roomText)) return false;
                if (!staffText.isEmpty() && !f.getStaffName().toLowerCase().contains(staffText)) return false;
                LocalDate date = f.getRentalDate().toLocalDate();
                if (from!=null && date.isBefore(from)) return false;
                if (to  !=null && date.isAfter(to))   return false;
                if ("Paid".equals(paidSel)   && f.getIsPaidAt()==null) return false;
                if ("Unpaid".equals(paidSel) && f.getIsPaidAt()!=null) return false;
                return true;
            });
        }

        @FXML
        private void onResetFilter() {
            tfFilterId.clear();
            tfFilterRoom.clear();
            tfFilterStaff.clear();
            dpFrom.setValue(null);
            dpTo.setValue(null);
            cbPaid.getSelectionModel().select("All");
            applyFilters();
        }

        @FXML
        private void showDetail(ResponseRentalFormDto dto) {
            detailPane.getChildren().clear();

            // Tiêu đề
            Label title = new Label("Phiếu thuê #" + dto.getId());
            title.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

            // Grid thông tin cơ bản
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(8);
            grid.setPadding(new Insets(8));
            Function<String, Label> lbl = t -> {
                Label l = new Label(t);
                l.setStyle("-fx-font-weight:bold;");
                return l;
            };
            grid.add(lbl.apply("Phòng:"),      0, 0); grid.add(new Label(dto.getRoomName()),    1, 0);
            grid.add(lbl.apply("Nhân viên:"),  0, 1); grid.add(new Label(dto.getStaffName()),   1, 1);
            grid.add(lbl.apply("Ngày thuê:"),  0, 2); grid.add(new Label(dto.getRentalDate().toString()), 1, 2);
            grid.add(lbl.apply("Ghi chú:"),    0, 3); grid.add(new Label(Optional.ofNullable(dto.getNote()).orElse("–")), 1, 3);

            // Accordion chứa Chi tiết và Gia hạn
            Accordion acc = new Accordion();

            // --- Pane Chi tiết ---
            ListView<ResponseRentalFormDetailDto> lvD = new ListView<>();
            for (Integer detId : dto.getRentalFormDetailIds()) {
                try {
                    String json = ApiHttpClientCaller.call("rental-form-detail/" + detId, ApiHttpClientCaller.Method.GET, null);
                    ResponseRentalFormDetailDto detail = mapper.readValue(json, new TypeReference<>() {});
                    lvD.getItems().add(detail);
                } catch (Exception ignored) {}
            }
            lvD.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ResponseRentalFormDetailDto item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null
                            ? null
                            : "#" + item.getId() + " – " + item.getGuestName());
                }
            });
            // Tự động điều chỉnh chiều cao:
            double rowHeight = 24; // cao 1 dòng ~24px
            lvD.setPrefHeight(lvD.getItems().size() * rowHeight + 2);

            TitledPane paneD = new TitledPane("Chi tiết", lvD);

            // --- Pane Gia hạn ---
            ListView<ResponseRentalExtensionFormDto> lvE = new ListView<>();
            for (Integer extId : dto.getRentalExtensionFormIds()) {
                try {
                    String json = ApiHttpClientCaller.call("rental-extension-form/" + extId, ApiHttpClientCaller.Method.GET, null);
                    ResponseRentalExtensionFormDto ext = mapper.readValue(json, new TypeReference<>() {});
                    lvE.getItems().add(ext);
                } catch (Exception ignored) {}
            }
            lvE.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ResponseRentalExtensionFormDto item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null
                            ? null
                            : "#" + item.getId() + " – " + item.getNumberOfRentalDays() + " ngày");
                }
            });
            // Điều chỉnh chiều cao tương tự:
            lvE.setPrefHeight(lvE.getItems().size() * rowHeight + 2);

            TitledPane paneE = new TitledPane("Gia hạn", lvE);

            acc.getPanes().setAll(paneD, paneE);
            acc.setExpandedPane(paneD);

            // Các nút hành động
            HBox actions = new HBox(10,
                    btnAddDetail,
                    btnAddExtension,
                    new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                    btnCheckout
            );
            actions.setPadding(new Insets(8, 0, 0, 0));

            btnAddDetail.setOnAction(e -> showDetailForm(dto, null));
            btnAddExtension.setOnAction(e -> showExtensionForm(dto, null));
            btnCheckout.setOnAction(e -> doCheckout());

            detailPane.getChildren().setAll(title, grid, acc, actions);
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

        @FXML
        private void doCheckout() {
            // 1) Lấy các phiếu thuê đang được chọn
            ObservableList<ResponseRentalFormDto> selectedForms = tableForm.getSelectionModel().getSelectedItems();
            if (selectedForms.isEmpty()) {
                showErrorAlert("Chưa chọn", "Vui lòng chọn ít nhất một phiếu thuê để thanh toán.");
                return;
            }

            // 2) Hiển thị dialog nhập PayingGuestId
            TextInputDialog guestDialog = new TextInputDialog();
            guestDialog.setTitle("Thanh toán");
            guestDialog.setHeaderText("Nhập ID Guest trả tiền:");
            Optional<String> guestRes = guestDialog.showAndWait();
            if (guestRes.isEmpty()) return;
            int payingGuestId;
            try {
                payingGuestId = Integer.parseInt(guestRes.get().trim());
            } catch (NumberFormatException ex) {
                showErrorAlert("Lỗi nhập", "ID Guest không hợp lệ.");
                return;
            }

            // 3) Hiển thị dialog nhập StaffId
            TextInputDialog staffDialog = new TextInputDialog();
            staffDialog.setTitle("Thanh toán");
            staffDialog.setHeaderText("Nhập ID Nhân viên thực hiện:");
            Optional<String> staffRes = staffDialog.showAndWait();
            if (staffRes.isEmpty()) return;
            int staffId;
            try {
                staffId = Integer.parseInt(staffRes.get().trim());
            } catch (NumberFormatException ex) {
                showErrorAlert("Lỗi nhập", "ID Nhân viên không hợp lệ.");
                return;
            }

            try {
                // 4) Tạo hoá đơn cha với total = 0.0
                InvoiceDto invoiceDto = new InvoiceDto();
                invoiceDto.setTotalReservationCost(0.0);
                invoiceDto.setPayingGuestId(payingGuestId);
                invoiceDto.setStaffId(staffId);

                String invJson = ApiHttpClientCaller.call(
                        // endpoint: POST /invoice/{impactorId}/{impactor}
                        // ở đây mình giả sử impactorId = staffId, impactor = "app"
                        "invoice/" + staffId + "/app",
                        ApiHttpClientCaller.Method.POST,
                        invoiceDto
                );
                ResponseInvoiceDto createdInvoice = mapper.readValue(invJson, ResponseInvoiceDto.class);
                int invoiceId = createdInvoice.getId();

                // 5) Với mỗi phiếu thuê, gọi checkout detail
                for (ResponseRentalFormDto rf : selectedForms) {
                    ApiHttpClientCaller.call(
                            // GET /invoice/checkout/{invoiceId}/{rentalFormId}/{impactorId}/{impactor}
                            "invoice/checkout/"
                                    + invoiceId + "/"
                                    + rf.getId() + "/"
                                    + staffId + "/app",
                            ApiHttpClientCaller.Method.GET,
                            null
                    );
                }

                showInfoAlert(
                        "Thanh toán thành công",
                        String.format("Đã tạo hoá đơn #%d và thanh toán %d phiếu thuê.",
                                invoiceId, selectedForms.size())
                );
                // 6) Load lại danh sách
                loadForms();

            } catch (Exception ex) {
                showErrorAlert("Lỗi thanh toán", ex.getMessage());
            }
        }


        private void showErrorAlert(String h, String c) {
            Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
            a.setHeaderText(h);
            a.showAndWait();
        }

        private void showInfoAlert(String h, String c) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, c, ButtonType.OK);
            a.setHeaderText(h);
            a.showAndWait();
        }
    }
