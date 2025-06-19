package com.example.frontendquanlikhachsan.controllers.receptionist;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.rentalForm.ResponseRentalFormDto;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.RentalFormDetailDto;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.ResponseRentalFormDetailDto;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.RentalExtensionFormDto;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.ResponseRentalExtensionFormDto;
import com.example.frontendquanlikhachsan.entity.guest.SearchGuestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.GET;
import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.POST;

public class RentalFormEditController {

    @FXML private TableView<ResponseRentalFormDto> tableForm;
    @FXML private TableColumn<ResponseRentalFormDto,Integer> colId;
    @FXML private TableColumn<ResponseRentalFormDto,String>  colRoomName;
    @FXML private TableColumn<ResponseRentalFormDto,String>  colStaffName;
    @FXML private TableColumn<ResponseRentalFormDto,String>  colDate;
    @FXML private TableColumn<ResponseRentalFormDto,Short>   colDays;
    @FXML private TableColumn<ResponseRentalFormDto,String>  colNote;
    @FXML private TableColumn<ResponseRentalFormDto,String>  colPaidAt;

    @FXML private TextField   tfFilterId, tfFilterRoom, tfFilterStaff;
    @FXML private DatePicker  dpFrom, dpTo;
    @FXML private ComboBox<String> cbPaid;
    @FXML private Button btnReset;
    @FXML private VBox detailPane;

    private final ObservableList<ResponseRentalFormDto> formList = FXCollections.observableArrayList();
    private FilteredList<ResponseRentalFormDto> filteredForms;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML public void initialize() {
        // setup columns
        colId.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colRoomName.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getRoomName()));
        colStaffName.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getStaffName()));
        colDate.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getRentalDate().toString()));
        colDays.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getNumberOfRentalDays()));
        colNote.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(Optional.ofNullable(cd.getValue().getNote()).orElse("–")));
        colPaidAt.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(Optional.ofNullable(cd.getValue().getIsPaidAt()).map(Object::toString).orElse("–")));

        // filters
        filteredForms = new FilteredList<>(formList, f->true);
        SortedList<ResponseRentalFormDto> sorted = new SortedList<>(filteredForms);
        sorted.comparatorProperty().bind(tableForm.comparatorProperty());
        tableForm.setItems(sorted);

        tfFilterId.textProperty().addListener((o,old,v)->applyFilters());
        tfFilterRoom.textProperty().addListener((o,old,v)->applyFilters());
        tfFilterStaff.textProperty().addListener((o,old,v)->applyFilters());
        dpFrom.valueProperty().addListener((o,old,v)->applyFilters());
        dpTo.valueProperty().addListener((o,old,v)->applyFilters());
        cbPaid.setItems(FXCollections.observableArrayList("All","Paid","Unpaid"));
        cbPaid.getSelectionModel().select("All");
        cbPaid.valueProperty().addListener((o,old,v)->applyFilters());
        btnReset.setOnAction(e->onResetFilter());

        // selection -> show detail
        tableForm.getSelectionModel().selectedItemProperty().addListener((o,old,sel)->{ if(sel!=null) showDetail(sel); });

        loadForms();
    }

    private void loadForms() {
        try {
            String json = ApiHttpClientCaller.call("rental-form", GET, null);
            List<ResponseRentalFormDto> list = mapper.readValue(json, new TypeReference<List<ResponseRentalFormDto>>(){});
            formList.setAll(list);
        } catch(Exception e) { showErrorAlert("Lỗi tải","Không thể tải phiếu thuê"); }
    }

    private void applyFilters() {
        String idText = Optional.ofNullable(tfFilterId.getText()).orElse("").trim();
        String roomText = Optional.ofNullable(tfFilterRoom.getText()).orElse("").trim().toLowerCase();
        String staffText= Optional.ofNullable(tfFilterStaff.getText()).orElse("").trim().toLowerCase();
        LocalDate from = dpFrom.getValue(); LocalDate to = dpTo.getValue();
        String paidSel = cbPaid.getValue();
        filteredForms.setPredicate(f->{
            if(!idText.isEmpty()){ try{ if(f.getId()!=Integer.parseInt(idText))return false;}catch(Exception e) { e.printStackTrace(); } }
            if(!roomText.isEmpty() && !f.getRoomName().toLowerCase().contains(roomText)) return false;
            if(!staffText.isEmpty()&& !f.getStaffName().toLowerCase().contains(staffText))return false;
            LocalDate d = f.getRentalDate().toLocalDate();
            if(from!=null&&d.isBefore(from)) return false;
            if(to  !=null&&d.isAfter(to))   return false;
            if("Paid".equals(paidSel)&&f.getIsPaidAt()==null) return false;
            if("Unpaid".equals(paidSel)&&f.getIsPaidAt()!=null) return false;
            return true;
        });
        tableForm.refresh();
    }
    private void onResetFilter(){ tfFilterId.clear(); tfFilterRoom.clear(); tfFilterStaff.clear(); dpFrom.setValue(null); dpTo.setValue(null); cbPaid.getSelectionModel().select("All"); applyFilters(); }

    private void showDetail(ResponseRentalFormDto dto) {
        detailPane.getChildren().clear();
        Label title=new Label("Phiếu thuê #"+dto.getId());
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        // info grid omitted for brevity…
        Accordion acc=new Accordion();
        // Chi tiết pane: only add
        ListView<ResponseRentalFormDetailDto> lvD=new ListView<>();
        dto.getRentalFormDetailIds().forEach(id->{ try{
            String j= null;
            try {
                j = ApiHttpClientCaller.call("rental-form-detail/"+id, GET,null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                lvD.getItems().add(mapper.readValue(j,ResponseRentalFormDetailDto.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }catch(Exception e){e.printStackTrace();} });
        lvD.setCellFactory(lv->new ListCell<>(){@Override protected void updateItem(ResponseRentalFormDetailDto i,boolean e){ super.updateItem(i,e); setText(e||i==null?null:"#"+i.getId()+" – "+i.getGuestName());}});
        Button btnAddD=new Button("➕ Thêm chi tiết"); btnAddD.setOnAction(e->showDetailForm(dto,null));
        VBox boxD=new VBox(6,btnAddD,lvD); boxD.setPadding(new Insets(8));
        acc.getPanes().add(new TitledPane("Chi tiết",boxD));

        // Gia hạn pane: only add
        ListView<ResponseRentalExtensionFormDto>lvE=new ListView<>();
        dto.getRentalExtensionFormIds().forEach(id->{ try{
            String j= null;
            try {
                j = ApiHttpClientCaller.call("rental-extension-form/"+id, GET,null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                lvE.getItems().add(mapper.readValue(j,ResponseRentalExtensionFormDto.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }catch(Exception e){e.printStackTrace();} });
        lvE.setCellFactory(lv->new ListCell<>(){@Override protected void updateItem(ResponseRentalExtensionFormDto i,boolean e){ super.updateItem(i,e); setText(e||i==null?null:"#"+i.getId()+" – "+i.getNumberOfRentalDays()+" ngày");}});
        Button btnAddE=new Button("➕ Thêm gia hạn"); btnAddE.setOnAction(e->showExtensionForm(dto,null));
        VBox boxE=new VBox(6,btnAddE,lvE); boxE.setPadding(new Insets(8));
        acc.getPanes().add(new TitledPane("Gia hạn",boxE));

        detailPane.getChildren().addAll(title,acc);
    }

    private void showDetailForm(ResponseRentalFormDto parent, ResponseRentalFormDetailDto detail) {
        detailPane.getChildren().clear();

        if (parent.getIsPaidAt() != null) {
            showInfoAlert("Không được thêm", "Phiếu này đã thanh toán, không thể thêm chi tiết.");
            showDetail(parent);
            return;
        }

        // Chỉ cho thêm mới, nếu detail != null thì báo và quay về
        if (detail != null) {
            showInfoAlert("Chỉ được thêm", "Không thể sửa chi tiết đã tồn tại.");
            showDetail(parent);
            return;
        }

        Label title = new Label("➕ Thêm Chi tiết");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

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
        TextField tfId = new TextField();
        form.add(tfId, 1, 0);

        // CMND
        form.add(lb.apply("CMND:"), 0, 1);
        TextField tfIdNum = new TextField();
        form.add(tfIdNum, 1, 1);

        // Email
        form.add(lb.apply("Email:"), 0, 2);
        TextField tfEmail = new TextField();
        form.add(tfEmail, 1, 2);

        // SĐT
        form.add(lb.apply("SĐT:"), 0, 3);
        TextField tfPhone = new TextField();
        form.add(tfPhone, 1, 3);

        // Nút tìm
        Button btnSearch = new Button("🔍 Tìm");
        form.add(btnSearch, 2, 0, 1, 4);

        // Kết quả
        ComboBox<ResponseGuestDto> cbResults = new ComboBox<>();
        form.add(lb.apply("Kết quả:"), 0, 4);
        form.add(cbResults, 1, 4, 2, 1);

        // Nút Thêm / Hủy
        HBox btns = new HBox(8);
        Button btnSave = new Button("💾 Thêm");
        Button btnCancel = new Button("❌ Hủy");
        btns.getChildren().addAll(btnSave, btnCancel);
        form.add(btns, 1, 5, 2, 1);

        // Xử lý tìm Guest
        btnSearch.setOnAction(evt -> {
            try {
                SearchGuestDto req = new SearchGuestDto();
                if (!tfId.getText().isBlank()) req.setId(Integer.parseInt(tfId.getText().trim()));
                req.setIdentificationNumber(tfIdNum.getText().isBlank() ? null : tfIdNum.getText().trim());
                req.setEmail(tfEmail.getText().isBlank() ? null : tfEmail.getText().trim());
                req.setPhoneNumber(tfPhone.getText().isBlank() ? null : tfPhone.getText().trim());

                String searchJson = ApiHttpClientCaller.call("guest/search", POST, req);
                List<ResponseGuestDto> respList = mapper.readValue(searchJson, new TypeReference<>() {});
                String idsJson = ApiHttpClientCaller.call("rental-form/" + parent.getId() + "/guest-ids", GET, null);
                List<Integer> taken = mapper.readValue(idsJson, new TypeReference<>() {});

                List<ResponseGuestDto> filtered = respList.stream()
                        .filter(g -> !taken.contains(g.getId()))
                        .toList();
                cbResults.setItems(FXCollections.observableArrayList(filtered));
                if (filtered.isEmpty()) showInfoAlert("Không tìm thấy", "Không có guest phù hợp.");
            } catch (Exception e) {
                showErrorAlert("Lỗi tìm guest", e.getMessage());
            }
        });

        // Thêm chi tiết
        btnSave.setOnAction(evt -> {
            ResponseGuestDto sel = cbResults.getValue();
            if (sel == null) { showErrorAlert("Lỗi", "Chưa chọn guest"); return; }
            try {
                RentalFormDetailDto dto = new RentalFormDetailDto();
                dto.setRentalFormId(parent.getId());
                dto.setGuestId(sel.getId());
                ApiHttpClientCaller.call("rental-form-detail", POST, dto);
                showInfoAlert("Thành công", "Đã thêm chi tiết.");
                ResponseRentalFormDto updated = getRefreshedRentalForm(parent.getId());
                showDetail(updated);
            } catch (Exception e) {
                showErrorAlert("Lỗi lưu detail", e.getMessage());
            }
        });

        btnCancel.setOnAction(evt -> showDetail(parent));

        detailPane.getChildren().addAll(title, form);
    }

    private void showExtensionForm(ResponseRentalFormDto parent, ResponseRentalExtensionFormDto ext) {
        detailPane.getChildren().clear();

        if (parent.getIsPaidAt() != null) {
            showInfoAlert("Không được thêm", "Phiếu này đã thanh toán, không thể gia hạn.");
            showDetail(parent);
            return;
        }

        // Chỉ cho thêm mới
        if (ext != null) {
            showInfoAlert("Chỉ được thêm", "Không thể sửa gia hạn đã tồn tại.");
            showDetail(parent);
            return;
        }

        Label title = new Label("➕ Thêm Gia hạn");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(8));
        Function<String, Label> lb = t -> {
            Label l = new Label(t);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };

        // Kiểm tra day-remains
        form.add(lb.apply("Phiếu thuê ID:"), 0, 0);
        form.add(new Label(String.valueOf(parent.getId())), 1, 0);
        form.add(lb.apply("Ngày còn lại:"), 0, 1);
        Button btnCheck = new Button("🔍 Kiểm tra");
        form.add(btnCheck, 1, 1);
        Label lblInfo = new Label();
        form.add(lblInfo, 1, 2);

        VBox step2 = new VBox(8);
        step2.setPadding(new Insets(10,0,0,0));

        btnCheck.setOnAction(e -> {
            try {
                String json = ApiHttpClientCaller.call("rental-extension-form/day-remains/" + parent.getId(), GET, null);
                int remains = mapper.readValue(json, Integer.class);
                if (remains <= 0) {
                    lblInfo.setText("Không thể gia hạn thêm");
                    step2.getChildren().clear();
                } else {
                    // Form nhập số ngày & staff
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
                    HBox hb = new HBox(8, btnSave2, btnCancel2);
                    hb.setPadding(new Insets(8,0,0,0));
                    g2.add(hb, 1, 2, 2, 1);

                    btnSave2.setOnAction(ev -> {
                        try {
                            short days = Short.parseShort(tfDays.getText().trim());
                            int max = Math.min(remains,5);
                            if (days < 1 || days > max) {
                                showErrorAlert("Lỗi", "Số ngày phải từ 1 đến " + max);
                                return;
                            }
                            RentalExtensionFormDto dto = RentalExtensionFormDto.builder()
                                    .rentalFormId(parent.getId())
                                    .numberOfRentalDays(days)
                                    .staffId(Integer.parseInt(tfStaff.getText().trim()))
                                    .build();
                            ApiHttpClientCaller.call("rental-extension-form", POST, dto);
                            showInfoAlert("Thành công","Đã thêm gia hạn.");
                            ResponseRentalFormDto updated = getRefreshedRentalForm(parent.getId());
                            showDetail(updated);
                        } catch (Exception ex) {
                            showErrorAlert("Lỗi lưu gia hạn", ex.getMessage());
                        }
                    });

                    btnCancel2.setOnAction(ev -> showDetail(parent));
                    step2.getChildren().setAll(g2);
                }
            } catch (Exception ex) {
                lblInfo.setText("Lỗi kiểm tra");
                step2.getChildren().clear();
            }
        });

        detailPane.getChildren().addAll(title, form, step2);
    }

    private ResponseRentalFormDto getRefreshedRentalForm(int formId) {
        try {
            String json = ApiHttpClientCaller.call("rental-form/" + formId,
                    GET, null);
            return mapper.readValue(json, ResponseRentalFormDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải", "Không thể tải dữ liệu phiếu thuê #" + formId);
            return null;
        }
    }

    private void showErrorAlert(String h,String c){ Alert a=new Alert(Alert.AlertType.ERROR,c,ButtonType.OK); a.setHeaderText(h); a.showAndWait(); }
    private void showInfoAlert(String h,String c){ Alert a=new Alert(Alert.AlertType.INFORMATION,c,ButtonType.OK); a.setHeaderText(h); a.showAndWait(); }
}
