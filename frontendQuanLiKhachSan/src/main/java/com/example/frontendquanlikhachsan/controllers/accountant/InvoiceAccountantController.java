package com.example.frontendquanlikhachsan.controllers.accountant;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.guest.SearchGuestDto;
import com.example.frontendquanlikhachsan.entity.rentalForm.ResponseRentalFormDto;
import com.example.frontendquanlikhachsan.entity.rentalForm.SearchRentalFormDto;
import com.example.frontendquanlikhachsan.entity.invoice.InvoiceDto;
import com.example.frontendquanlikhachsan.entity.invoice.ResponseInvoiceDto;
import com.example.frontendquanlikhachsan.entity.invoicedetail.InvoiceDetailDto;
import com.example.frontendquanlikhachsan.entity.invoicedetail.ResponseInvoiceDetailDto;
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
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.*;

public class InvoiceAccountantController {
    // --- UI controls ---
    @FXML private TableView<ResponseInvoiceDto> tableInvoice;
    @FXML private TableColumn<ResponseInvoiceDto,Integer> colInvId;
    @FXML private TableColumn<ResponseInvoiceDto,Integer> colPayingGuestId;
    @FXML private TableColumn<ResponseInvoiceDto,String>  colPayingGuestName;
    @FXML private TableColumn<ResponseInvoiceDto,Integer> colStaffId;
    @FXML private TableColumn<ResponseInvoiceDto,String>  colStaffName;
    @FXML private TableColumn<ResponseInvoiceDto,String>  colCreatedAt;
    @FXML private TableColumn<ResponseInvoiceDto,Double>  colTotalCost;


    @FXML private VBox detailPane;

    // --- filter controls ---
    @FXML private TextField  tfFilterInvId;
    @FXML private TextField  tfFilterGuestId;
    @FXML private TextField  tfFilterCostMin;
    @FXML private TextField  tfFilterCostMax;
    @FXML private DatePicker dpFilterFrom;
    @FXML private DatePicker dpFilterTo;
    @FXML private Button btnFilter;

    private final ObservableList<ResponseInvoiceDto> invoiceList = FXCollections.observableArrayList();
    private FilteredList<ResponseInvoiceDto> filteredInvoices;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final String token = ""; // TODO: gán token
    private ResponseInvoiceDto editingDto;

    @FXML
    public void initialize() {
        // 1) Khởi tạo filtered & sorted list
        filteredInvoices = new FilteredList<>(invoiceList, f -> true);
        SortedList<ResponseInvoiceDto> sorted = new SortedList<>(filteredInvoices);
        sorted.comparatorProperty().bind(tableInvoice.comparatorProperty());
        tableInvoice.setItems(sorted);

        // 2) Lắng nghe filter controls
        tfFilterInvId.textProperty().addListener((o,oldV,newV) -> applyFilters());
        tfFilterGuestId.textProperty().addListener((o,oldV,newV) -> applyFilters());
        tfFilterCostMin.textProperty().addListener((o,oldV,newV) -> applyFilters());
        tfFilterCostMax.textProperty().addListener((o,oldV,newV) -> applyFilters());
        dpFilterFrom.valueProperty().addListener((o,oldV,newV) -> applyFilters());
        dpFilterTo.valueProperty().addListener((o,oldV,newV) -> applyFilters());

        // 3) Column bindings
        colInvId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colPayingGuestId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPayingGuestId()));
        colPayingGuestName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPayingGuestName()));
        colStaffId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStaffId()));
        colStaffName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStaffName()));
        colCreatedAt.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCreatedAt().toString()));
        colTotalCost.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getTotalReservationCost()));

        // 4) Load data
        loadInvoices();

        // 5) Chọn 1 invoice để xem detail
        tableInvoice.getSelectionModel().selectedItemProperty()
                .addListener((obs,old,sel) -> { if (sel!=null) showDetail(sel); });

        tableInvoice.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadInvoices() {
        try {
            String json = ApiHttpClientCaller.call("invoice", GET, null, token);
            List<ResponseInvoiceDto> list = mapper.readValue(json, new TypeReference<List<ResponseInvoiceDto>>() {});
            invoiceList.setAll(list);
        } catch(Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải", "Không thể tải danh sách hoá đơn.");
        }
    }

    private void applyFilters() {
        String idText     = Optional.ofNullable(tfFilterInvId.getText()).orElse("").trim();
        String guestText  = Optional.ofNullable(tfFilterGuestId.getText()).orElse("").trim();
        String minCostStr = Optional.ofNullable(tfFilterCostMin.getText()).orElse("").trim();
        String maxCostStr = Optional.ofNullable(tfFilterCostMax.getText()).orElse("").trim();
        LocalDate from    = dpFilterFrom.getValue();
        LocalDate to      = dpFilterTo.getValue();

        filteredInvoices.setPredicate(inv -> {
            // filter by invoice id
            if (!idText.isEmpty()) {
                try { if (inv.getId() != Integer.parseInt(idText)) return false; }
                catch(NumberFormatException ex){ return false; }
            }
            // filter by payingGuestId
            if (!guestText.isEmpty()) {
                try{ if (inv.getPayingGuestId() != Integer.parseInt(guestText)) return false; }
                catch(NumberFormatException ex){ return false; }
            }
            // filter by totalCost
            double cost = inv.getTotalReservationCost();
            if (!minCostStr.isEmpty()) {
                try{ if (cost < Double.parseDouble(minCostStr)) return false; }
                catch(NumberFormatException ex){ return false; }
            }
            if (!maxCostStr.isEmpty()) {
                try{ if (cost > Double.parseDouble(maxCostStr)) return false; }
                catch(NumberFormatException ex){ return false; }
            }
            // filter by createdAt
            LocalDate date = inv.getCreatedAt().toLocalDate();
            if (from != null && date.isBefore(from)) return false;
            if (to   != null && date.isAfter(to))   return false;

            return true;
        });
    }

    @FXML private void onCreateOrEditForm() {
        showInvoiceForm(null);
    }

    @FXML
    private void onFilterAction() {
        applyFilters();
    }

    private void showDetail(ResponseInvoiceDto dto) {
        detailPane.getChildren().clear();

        // --- Tiêu đề ---
        Label title = new Label("Chi tiết Hoá đơn – ID: " + dto.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        // --- Grid cơ bản ---
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String,Label> lb = t-> {
            Label l = new Label(t);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };
        grid.add(lb.apply("Người thanh toán ID:"), 0, 0);
        grid.add(new Label(String.valueOf(dto.getPayingGuestId())), 1, 0);
        grid.add(lb.apply("Người thanh toán:"),   0, 1);
        grid.add(new Label(dto.getPayingGuestName()), 1, 1);
        grid.add(lb.apply("Nhân viên ID:"),      0, 2);
        grid.add(new Label(String.valueOf(dto.getStaffId())), 1, 2);
        grid.add(lb.apply("Nhân viên:"),         0, 3);
        grid.add(new Label(dto.getStaffName()),  1, 3);
        grid.add(lb.apply("Ngày tạo:"),         0, 4);
        grid.add(new Label(dto.getCreatedAt().toString()), 1,4);
        grid.add(lb.apply("Tổng chi phí:"),     0, 5);
        grid.add(new Label(String.valueOf(dto.getTotalReservationCost())), 1,5);

        // --- Accordion cho InvoiceDetails ---
        Accordion acc = new Accordion();
        // ListView chi tiết
        ListView<ResponseInvoiceDetailDto> lvD = new ListView<>();
        for (Integer dId : dto.getInvoiceDetailIds()) {
            try {
                String j = ApiHttpClientCaller.call("invoice-detail/" + dId, GET, null, token);
                ResponseInvoiceDetailDto d = mapper.readValue(j, ResponseInvoiceDetailDto.class);
                lvD.getItems().add(d);
            } catch (Exception ignored) {}
        }
        lvD.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ResponseInvoiceDetailDto it, boolean empty) {
                super.updateItem(it, empty);
                if (empty||it==null) setText(null);
                else setText("#"+it.getId()
                        + " – RF#" + it.getRentalFormId()
                        + " – " + it.getReservationCost());
            }
        });
        lvD.setPrefHeight(lvD.getItems().size()*42 + 2);

        VBox boxD = new VBox(6, lvD);
        boxD.setPadding(new Insets(8,0,0,0));
        acc.getPanes().add(new TitledPane("Chi tiết hoá đơn", boxD));

        detailPane.getChildren().setAll(title, grid, acc);
    }

    private void showInvoiceForm(ResponseInvoiceDto dto) {
        editingDto = dto;
        detailPane.getChildren().clear();

        Label title = new Label(dto == null
                ? "➕ Tạo Hoá đơn mới"
                : "✏️ Sửa Hoá đơn #" + dto.getId()
        );
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        // --- Form chính ---
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(8));
        Function<String,Label> lb = t-> {
            Label l = new Label(t);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };

        // 1) Tìm PayingGuest
        form.add(lb.apply("Tìm người thanh toán:"), 0, 0);
        TextField tfGId    = new TextField(); tfGId.setPromptText("ID");
        TextField tfGName  = new TextField(); tfGName.setPromptText("Tên");
        TextField tfGEmail = new TextField(); tfGEmail.setPromptText("Email");
        TextField tfGIdentificationNumber = new TextField(); tfGIdentificationNumber.setPromptText("CCCD/CMND");
        TextField tfGPhoneNumber = new TextField(); tfGPhoneNumber.setPromptText("SĐT");
        Button  btnGSearch = new Button("🔍 Tìm");
        HBox    hbG        = new HBox(6, tfGId, tfGName, tfGEmail,
                tfGIdentificationNumber, tfGPhoneNumber, btnGSearch);
        form.add(hbG, 1, 0, 3, 1);

        ComboBox<SearchGuestDto> cbG = new ComboBox<>();
        cbG.setPrefWidth(300);
        form.add(lb.apply("Kết quả:"), 0, 1);
        form.add(cbG, 1, 1, 3, 1);

        // 2) Staff ID
        form.add(lb.apply("ID Nhân viên:"), 0, 2);
        TextField tfStaff = new TextField(dto==null?"":String.valueOf(dto.getStaffId()));
        form.add(tfStaff, 1, 2);

        // 3) Save / Cancel
        Button btnSave   = new Button(dto==null?"💾 Tạo":"💾 Lưu");
        Button btnCancel = new Button("❌ Hủy");
        HBox btns        = new HBox(8, btnSave, btnCancel);
        btns.setPadding(new Insets(12,0,0,0));
        form.add(btns, 1, 3, 3, 1);

        // --- Search guest action ---
        btnGSearch.setOnAction(evt -> {
            try {
                SearchGuestDto req = new SearchGuestDto();
                if (!tfGId.getText().isBlank())
                    req.setId(Integer.parseInt(tfGId.getText().trim()));
                if (!tfGName.getText().isBlank())
                    req.setName(tfGName.getText().trim());
                if (!tfGEmail.getText().isBlank())
                    req.setEmail(tfGEmail.getText().trim());
                if (!tfGIdentificationNumber.getText().isBlank())
                    req.setIdentificationNumber(tfGIdentificationNumber.getText().trim());
                if (!tfGPhoneNumber.getText().isBlank())
                    req.setPhoneNumber(tfGPhoneNumber.getText().trim());
                String j = ApiHttpClientCaller.call("guest/search", POST, req, token);
                List<ResponseGuestDto> r = mapper.readValue(j, new TypeReference<List<ResponseGuestDto>>(){});
                // Chuyển đổi ResponseGuestDto sang SearchGuestDto
                List<SearchGuestDto> rs = r.stream()
                        .map(g -> SearchGuestDto.builder()
                                .id(g.getId())
                                .name(g.getName())
                                .email(g.getEmail())
                                .identificationNumber(g.getIdentificationNumber())
                                .phoneNumber(g.getPhoneNumber())
                                .build())
                        .toList();
                cbG.getItems().setAll(rs);
                cbG.setConverter(new StringConverter<>() {
                    @Override public String toString(SearchGuestDto g) {
                        return g==null? "": "#" + g.getId()
                                + (g.getName() != null ? " – " + g.getName() : "")
                                + (g.getIdentificationNumber() != null ? " (" + g.getIdentificationNumber() + ")" : "")
                                + (g.getPhoneNumber() != null ? " – " + g.getPhoneNumber() : "")
                                + (g.getEmail() != null ? " <" + g.getEmail() + ">" : "");
                    }
                    @Override public SearchGuestDto fromString(String s) { return null; }
                });
                if(!rs.isEmpty()) cbG.getSelectionModel().selectFirst();
            } catch(Exception ex) {
                showErrorAlert("Lỗi tìm", ex.getMessage());
            }
        });

        // --- Save invoice action ---
        btnSave.setOnAction(evt -> {
            SearchGuestDto sel = cbG.getValue();
            if (sel == null) { showErrorAlert("Lỗi","Chưa chọn người thanh toán"); return; }
            try {
                InvoiceDto payload = InvoiceDto.builder()
                        .totalReservationCost(0.0) // sẽ tính lại sau khi có details
                        .payingGuestId(sel.getId())
                        .staffId(Integer.parseInt(tfStaff.getText().trim()))
                        .build();

                String path = dto==null ? "invoice" : "invoice/"+dto.getId();
                String j = ApiHttpClientCaller.call(path,
                        dto==null? POST:PUT, payload, token);

                ResponseInvoiceDto created = mapper.readValue(j, ResponseInvoiceDto.class);
                showInfoAlert("Thành công",
                        dto==null
                                ? "Hoá đơn mới đã được tạo"
                                : "Hoá đơn đã được cập nhật");
                loadInvoices();
                showDetail(created);
            } catch(Exception ex) {
                showErrorAlert("Lỗi lưu", ex.getMessage());
            }
        });

        btnCancel.setOnAction(evt -> {
            if (editingDto != null) showDetail(editingDto);
            else detailPane.getChildren().clear();
        });

        detailPane.getChildren().setAll(title, form);
    }

    // Helper để show dialog thông báo
    private void showInfoAlert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.setContentText(content);

        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }
    private void showErrorAlert(String h, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(h);
        a.setContentText(c);

        a.getDialogPane().getStylesheets().add(getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }
}
