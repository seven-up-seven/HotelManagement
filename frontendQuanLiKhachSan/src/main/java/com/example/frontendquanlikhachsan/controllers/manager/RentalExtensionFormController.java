package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.RentalExtensionFormDto;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.ResponseRentalExtensionFormDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RentalExtensionFormController {

    @FXML private TableView<ResponseRentalExtensionFormDto> tableExtension;
    @FXML private TableColumn<ResponseRentalExtensionFormDto, Integer> colId;
    @FXML private TableColumn<ResponseRentalExtensionFormDto, Integer> colRentalFormId;
    @FXML private TableColumn<ResponseRentalExtensionFormDto, String> colRoomName;
    @FXML private TableColumn<ResponseRentalExtensionFormDto, Short> colDays;
    @FXML private TableColumn<ResponseRentalExtensionFormDto, String> colStaffName;
    @FXML private TableColumn<ResponseRentalExtensionFormDto, Integer> colDayRemains;
    @FXML private VBox detailPane;

    private final ObservableList<ResponseRentalExtensionFormDto> extList = FXCollections.observableArrayList();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String token = ""; // TODO: gán token

    @FXML public void initialize() {
        colId.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colRentalFormId.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getRentalFormId()));
        colRoomName.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getRentalFormRoomName()));
        colDays.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getNumberOfRentalDays()));
        colStaffName.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getStaffName()));
        colDayRemains.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getDayRemains()));

        loadExtensions();
        tableExtension.setItems(extList);
        tableExtension.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{ if(n!=null) showDetail(n); });
    }

    @FXML private void onCreateExtension() {
        showCreateForm();
    }

    private void loadExtensions() {
        try {
            String json = ApiHttpClientCaller.call("rental-extension-form", ApiHttpClientCaller.Method.GET, null, token);
            List<ResponseRentalExtensionFormDto> list = mapper.readValue(json, new TypeReference<>(){});
            extList.setAll(list);
        } catch(Exception e) {
            e.printStackTrace(); showErrorAlert("Lỗi tải","Không thể tải gia hạn.");
        }
    }

    private void showDetail(ResponseRentalExtensionFormDto dto) {
        detailPane.getChildren().clear();
        Label title=new Label("» Chi tiết Gia hạn – ID:"+dto.getId());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-padding:0 0 8 0;");

        GridPane grid=new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String,Label> lb=txt->{Label l=new Label(txt);l.setStyle("-fx-font-weight:bold;");return l;};
        grid.add(lb.apply("ID Phiếu thuê:"),0,0);
        grid.add(new Label(String.valueOf(dto.getRentalFormId())),1,0);
        grid.add(lb.apply("Tên Phòng:"),0,1);
        grid.add(new Label(dto.getRentalFormRoomName()),1,1);
        grid.add(lb.apply("Số ngày gia hạn:"),0,2);
        grid.add(new Label(String.valueOf(dto.getNumberOfRentalDays())),1,2);
        grid.add(lb.apply("Nhân viên xử lý:"),0,3);
        grid.add(new Label(dto.getStaffName()),1,3);
        grid.add(lb.apply("Ngày còn lại:"),0,4);
        grid.add(new Label(String.valueOf(dto.getDayRemains())),1,4);

        HBox actions=new HBox(10); actions.setPadding(new Insets(12,0,0,0));
        Button btnDel=new Button("🗑️ Xóa"); btnDel.setOnAction(e-> deleteExtension(dto));
        actions.getChildren().add(btnDel);

        detailPane.getChildren().addAll(title,grid,actions);
    }

    private void deleteExtension(ResponseRentalExtensionFormDto dto) {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION,"Xóa gia hạn này?",ButtonType.OK,ButtonType.CANCEL);
        c.showAndWait().filter(b->b==ButtonType.OK).ifPresent(b->{
            try{
                ApiHttpClientCaller.call("rental-extension-form/"+dto.getId(),ApiHttpClientCaller.Method.DELETE,null,token);
                loadExtensions();
                showInfoAlert("Đã xóa","Gia hạn ID:"+dto.getId());
            } catch(Exception ex){ex.printStackTrace();showErrorAlert("Lỗi","Không thể xóa.");}
        });
    }

    private void showCreateForm() {
        detailPane.getChildren().clear();
        Label title=new Label("» Tạo Gia hạn mới");
        title.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-padding:0 0 8 0;");

        GridPane grid=new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String,Label> lb=txt->{Label l=new Label(txt);l.setStyle("-fx-font-weight:bold;");return l;};

        grid.add(lb.apply("ID Phiếu thuê:"),0,0);
        TextField tfFormId=new TextField(); grid.add(tfFormId,1,0);
        grid.add(lb.apply("Số ngày gia hạn:"),0,1);
        TextField tfDays=new TextField(); grid.add(tfDays,1,1);
        grid.add(lb.apply("ID Nhân viên:"),0,2);
        TextField tfStaffId=new TextField(); grid.add(tfStaffId,1,2);

        HBox btns=new HBox(10); btns.setPadding(new Insets(12,0,0,0));
        Button save=new Button("💾 Lưu"),cancel=new Button("❌ Hủy");
        cancel.setOnAction(e-> detailPane.getChildren().clear());
        save.setOnAction(e->{
            try{
                int formId = Integer.parseInt(tfFormId.getText().trim());
                short days    = Short.parseShort(tfDays.getText().trim());
                int staffId   = Integer.parseInt(tfStaffId.getText().trim());
                // call day-remains endpoint
                String jsonRem = ApiHttpClientCaller.call(
                        "rental-extension-form/day-remains/"+formId,
                        ApiHttpClientCaller.Method.GET, null, token
                );
                int remains = new ObjectMapper().readValue(jsonRem, Integer.class);
                if (days >= remains) {
                    showErrorAlert("Lỗi","Số ngày gia hạn phải nhỏ hơn " + remains + " ngày.");
                    return;
                }
                RentalExtensionFormDto dto = RentalExtensionFormDto.builder()
                        .rentalFormId(formId)
                        .numberOfRentalDays(days)
                        .staffId(staffId)
                        .build();
                ApiHttpClientCaller.call("rental-extension-form",ApiHttpClientCaller.Method.POST,dto,token);
                showInfoAlert("Tạo thành công","Đã tạo gia hạn.");
                loadExtensions();
            }catch(NumberFormatException nfe){
                showErrorAlert("Lỗi","Vui lòng nhập đúng định dạng số.");
            }catch(Exception ex){
                ex.printStackTrace();
                showErrorAlert("Lỗi","Không thể tạo gia hạn.");
            }
        });
        btns.getChildren().addAll(save,cancel);
        detailPane.getChildren().addAll(title,grid,btns);
    }

    private void showInfoAlert(String h,String c){ Alert a=new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(h); a.setContentText(c); a.showAndWait(); }
    private void showErrorAlert(String h,String c){ Alert a=new Alert(Alert.AlertType.ERROR); a.setHeaderText(h); a.setContentText(c); a.showAndWait(); }
}