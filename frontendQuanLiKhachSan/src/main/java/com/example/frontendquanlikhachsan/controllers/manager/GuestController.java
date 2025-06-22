package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.controllers.MainController;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.guest.GuestDto;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.GET;
// …v.v.

public class GuestController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML private TextField tfFilterGuestId;
    @FXML private TextField tfFilterName;
    @FXML private TextField tfFilterIdNum;
    @FXML private TextField tfFilterPhone;
    @FXML private TextField tfFilterEmail;

    @FXML private TableView<ResponseGuestDto> tableGuest;

    @FXML private TableColumn<ResponseGuestDto, Integer> colId;
    @FXML private TableColumn<ResponseGuestDto, String> colName;
    @FXML private TableColumn<ResponseGuestDto, Sex> colSex;
    @FXML private TableColumn<ResponseGuestDto, Short> colAge;
    @FXML private TableColumn<ResponseGuestDto, String> colIdNum;
    @FXML private TableColumn<ResponseGuestDto, String> colPhone;
    @FXML private TableColumn<ResponseGuestDto, String> colEmail;

    @FXML private VBox detailPane;

    // In GuestController.java, modify the ObjectMapper initialization
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private final String token = ""; // TODO: gán token
    private final ObservableList<ResponseGuestDto> guestList = FXCollections.observableArrayList();
    private FilteredList<ResponseGuestDto> filteredGuests;

    @FXML public void initialize() {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getName()));
        colSex.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getSex()));
        colAge.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getAge()));
        colIdNum.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getIdentificationNumber()));
        colPhone.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPhoneNumber()));
        colEmail.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getEmail()));

        loadGuests();
        filteredGuests = new FilteredList<>(guestList, g -> true);
        SortedList<ResponseGuestDto> sorted = new SortedList<>(filteredGuests);
        sorted.comparatorProperty().bind(tableGuest.comparatorProperty());
        tableGuest.setItems(sorted);

        tableGuest.getSelectionModel().selectedItemProperty().addListener((obs,ov,nv)->{
            detailPane.getChildren().clear();
            if (nv!=null) showGuestDetail(nv);
        });

        tableGuest.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Runnable apply = this::applyFilters;
        tfFilterGuestId.textProperty().addListener((o,ov,nv)->apply.run());
        tfFilterName     .textProperty().addListener((o,ov,nv)->apply.run());
        tfFilterIdNum    .textProperty().addListener((o,ov,nv)->apply.run());
        tfFilterPhone    .textProperty().addListener((o,ov,nv)->apply.run());
        tfFilterEmail    .textProperty().addListener((o,ov,nv)->apply.run());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "–";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @FXML private void onCreateGuest() {
        showCreateForm();
    }

    private void loadGuests() {
        try {
            String json = ApiHttpClientCaller.call("guest", GET, null, token);
            List<ResponseGuestDto> list = mapper.readValue(json, new TypeReference<>() {});
            guestList.setAll(list);
        } catch(Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải dữ liệu", "Không thể tải danh sách guest.");
        }
    }

    private void applyFilters() {
        String idText   = tfFilterGuestId.getText().trim();
        String nameText = tfFilterName.getText().trim().toLowerCase();
        String idNum    = tfFilterIdNum.getText().trim();
        String phone    = tfFilterPhone.getText().trim();
        String email    = tfFilterEmail.getText().trim().toLowerCase();

        filteredGuests.setPredicate(g -> {
            // ID exact match
            if (!idText.isEmpty()) {
                try {
                    if (g.getId() != Integer.parseInt(idText)) return false;
                } catch(NumberFormatException ex) {
                    return false;
                }
            }
            // name contains
            if (!nameText.isEmpty()
                    && !g.getName().toLowerCase().contains(nameText)) return false;
            // CCCD/CMND contains
            if (!idNum.isEmpty()
                    && (g.getIdentificationNumber()==null
                    || !g.getIdentificationNumber().contains(idNum))) return false;
            // phone contains
            if (!phone.isEmpty()
                    && (g.getPhoneNumber()==null
                    || !g.getPhoneNumber().contains(phone))) return false;
            // email contains
            if (!email.isEmpty()
                    && (g.getEmail()==null
                    || !g.getEmail().toLowerCase().contains(email))) return false;

            return true;
        });
    }

    private void showGuestDetail(ResponseGuestDto g) {
        detailPane.getChildren().clear();
        Label title = new Label("» Thông tin Guest – ID: " + g.getId());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:14; -fx-padding:0 0 8 0;");

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(8);
        grid.setPadding(new Insets(8));

        Function<String,Label> lb = txt -> {
            Label l = new Label(txt);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };

        grid.add(lb.apply("Họ & Tên:"), 0, 0);
        grid.add(new Label(g.getName()), 1, 0);
        grid.add(lb.apply("Giới tính:"), 0, 1);
        grid.add(new Label(Optional.ofNullable(g.getSex()).map(Sex::toString).orElse("–")), 1, 1);
        grid.add(lb.apply("Tuổi:"), 0, 2);
        grid.add(new Label(String.valueOf(g.getAge())), 1, 2);
        grid.add(lb.apply("CMND/CCCD:"), 0, 3);
        grid.add(new Label(Optional.ofNullable(g.getIdentificationNumber()).orElse("–")), 1, 3);
        grid.add(lb.apply("SĐT:"), 0, 4);
        grid.add(new Label(Optional.ofNullable(g.getPhoneNumber()).orElse("–")), 1, 4);
        grid.add(lb.apply("Email:"), 0, 5);
        grid.add(new Label(Optional.ofNullable(g.getEmail()).orElse("–")), 1, 5);

        HBox btnBox = new HBox(10);
        btnBox.setPadding(new Insets(12, 0, 0, 0));

        // 1) Xem hóa đơn liên quan
        if (!g.getInvoiceIds().isEmpty()) {
            Button btnInv = new Button("📄 Hoá đơn liên quan");
            btnInv.setOnAction(e -> {
                // dùng mainController mới implement openInvoiceTabs
                mainController.openInvoiceTab(g.getInvoiceIds());
            });
            btnBox.getChildren().add(btnInv);
        }

        // 2) Xem phiếu thuê liên quan
        if (!g.getRentalFormIds().isEmpty()) {
            Button btnRent = new Button("🏠 Phiếu thuê liên quan");
            btnRent.setOnAction(e -> {
                mainController.openRentalFormTab(g.getRentalFormIds());
            });
            btnBox.getChildren().add(btnRent);
        }

        // 3) Xem xác nhận đặt phòng liên quan
        if (!g.getBookingConfirmationFormIds().isEmpty()) {
            Button btnBookConf = new Button("✅ Xác nhận liên quan");
            btnBookConf.setOnAction(e -> {
                mainController.openBookingConfirmationFormTab(g.getBookingConfirmationFormIds());
            });
            btnBox.getChildren().add(btnBookConf);
        }

        detailPane.getChildren().addAll(title, grid, btnBox);

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));
        Button btnEdit = new Button("✏️ Sửa"); btnEdit.setOnAction(e -> showEditForm(g));
        Button btnDel  = new Button("🗑️ Xóa"); btnDel.setOnAction(e -> deleteGuest(g));
        actions.getChildren().addAll(btnEdit, btnDel);
        detailPane.getChildren().add(actions);
    }



    private void deleteGuest(ResponseGuestDto g) {
        Alert a=new Alert(Alert.AlertType.CONFIRMATION,"Xóa guest này?", ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait().filter(b->b==ButtonType.OK).ifPresent(b->{
            try { ApiHttpClientCaller.call("guest/"+g.getId(), ApiHttpClientCaller.Method.DELETE,null);
                showInfoAlert("Đã xóa","Guest ID:"+g.getId()); loadGuests();
            } catch(Exception ex){ex.printStackTrace(); showErrorAlert("Lỗi","Không thể xóa guest.");}
        });
    }

    private void showEditForm(ResponseGuestDto g) {
        detailPane.getChildren().clear();
        Label title = new Label("» Chỉnh sửa Guest – ID:"+g.getId());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:14; -fx-padding:0 0 8 0;");
        GridPane grid=new GridPane(); grid.setVgap(8); grid.setHgap(8); grid.setPadding(new Insets(8));
        java.util.function.Function<String,Label> lb = txt->{Label l=new Label(txt);l.setStyle("-fx-font-weight:bold;");return l;};

        grid.add(lb.apply("Họ & Tên:"),0,0); TextField tfName=new TextField(g.getName()); grid.add(tfName,1,0);
        grid.add(lb.apply("Giới tính:"),0,1); ComboBox<Sex> cbSex=new ComboBox<>(FXCollections.observableArrayList(Sex.values())); cbSex.setValue(g.getSex()); grid.add(cbSex,1,1);
        grid.add(lb.apply("Tuổi:"),0,2); TextField tfAge=new TextField(String.valueOf(g.getAge())); grid.add(tfAge,1,2);
        grid.add(lb.apply("CMND/CCCD:"),0,3); TextField tfId=new TextField(g.getIdentificationNumber()); grid.add(tfId,1,3);
        grid.add(lb.apply("SĐT:"),0,4); TextField tfPhone=new TextField(g.getPhoneNumber()); grid.add(tfPhone,1,4);
        grid.add(lb.apply("Email:"),0,5); TextField tfEmail=new TextField(g.getEmail()); grid.add(tfEmail,1,5);

        HBox btns=new HBox(10); btns.setPadding(new Insets(10,0,0,0));
        Button save=new Button("💾 Lưu"), cancel=new Button("❌ Hủy");
        cancel.setOnAction(e-> showGuestDetail(g));
        save.setOnAction(e->{
            try{
                GuestDto dto = GuestDto.builder()
                        .name(tfName.getText().trim())
                        .sex(cbSex.getValue())
                        .age(Short.parseShort(tfAge.getText().trim()))
                        .identificationNumber(tfId.getText().trim())
                        .phoneNumber(tfPhone.getText().trim())
                        .email(tfEmail.getText().trim())
                        .build();
                ApiHttpClientCaller.call("guest/"+g.getId(), ApiHttpClientCaller.Method.PUT, dto);
                showInfoAlert("Cập nhật","Guest đã được cập nhật"); loadGuests();
            } catch(Exception ex){ex.printStackTrace(); showErrorAlert("Lỗi","Không thể cập nhật guest.");}
        });
        btns.getChildren().addAll(save,cancel);
        detailPane.getChildren().addAll(title,grid,btns);
    }

    private void showCreateForm() {
        detailPane.getChildren().clear();
        Label title=new Label("» Tạo Guest mới"); title.setStyle("-fx-font-weight:bold; -fx-font-size:14; -fx-padding:0 0 8 0;");
        GridPane grid=new GridPane(); grid.setVgap(8); grid.setHgap(8); grid.setPadding(new Insets(8));
        java.util.function.Function<String,Label> lb=txt->{Label l=new Label(txt);l.setStyle("-fx-font-weight:bold;");return l;};

        grid.add(lb.apply("Họ & Tên:"),0,0); TextField tfName=new TextField(); grid.add(tfName,1,0);
        grid.add(lb.apply("Giới tính:"),0,1); ComboBox<Sex> cbSex=new ComboBox<>(FXCollections.observableArrayList(Sex.values())); grid.add(cbSex,1,1);
        grid.add(lb.apply("Tuổi:"),0,2); TextField tfAge=new TextField(); grid.add(tfAge,1,2);
        grid.add(lb.apply("CMND/CCCD:"),0,3); TextField tfId=new TextField(); grid.add(tfId,1,3);
        grid.add(lb.apply("SĐT:"),0,4); TextField tfPhone=new TextField(); grid.add(tfPhone,1,4);
        grid.add(lb.apply("Email:"),0,5); TextField tfEmail=new TextField(); grid.add(tfEmail,1,5);

        HBox btns=new HBox(10); btns.setPadding(new Insets(10,0,0,0));
        Button save=new Button("💾 Lưu"), cancel=new Button("❌ Hủy"); cancel.setOnAction(e-> detailPane.getChildren().clear());
        save.setOnAction(e->{
            try{
                GuestDto dto = GuestDto.builder()
                        .name(tfName.getText().trim())
                        .sex(cbSex.getValue())
                        .age(Short.parseShort(tfAge.getText().trim()))
                        .identificationNumber(emptyToNull(tfId.getText()))
                        .phoneNumber(emptyToNull(tfPhone.getText()))
                        .email(emptyToNull(tfEmail.getText()))
                        .build();
                ApiHttpClientCaller.call("guest", ApiHttpClientCaller.Method.POST, dto);
                showInfoAlert("Tạo thành công","Guest mới đã được thêm"); loadGuests();
            } catch(Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Lỗi","Không thể tạo guest.");
            }
        });
        btns.getChildren().addAll(save,cancel);
        detailPane.getChildren().addAll(title,grid,btns);
    }

    private void showInfoAlert(String h, String c){
        Alert a=new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(h);
        a.setContentText(c);
        a.showAndWait();
    }
    private void showErrorAlert(String h, String c){
        Alert a=new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(h);
        a.setContentText(c);
        a.showAndWait(); }

    private String emptyToNull(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        return input.trim();
    }
}
