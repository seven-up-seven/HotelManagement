package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.position.ResponsePositionDto;
import com.example.frontendquanlikhachsan.entity.position.PositionDto;
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

public class PositionController {

    @FXML private TableView<ResponsePositionDto> tablePosition;
    @FXML private TableColumn<ResponsePositionDto,Integer> colId;
    @FXML private TableColumn<ResponsePositionDto,String> colName;
    @FXML private TableColumn<ResponsePositionDto,Double> colSalary;
    @FXML private VBox detailPane;

    private final ObservableList<ResponsePositionDto> positionList = FXCollections.observableArrayList();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String token = ""; // TODO: set actual token

    @FXML public void initialize() {
        colId.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colName.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getName()));
        colSalary.setCellValueFactory(cd->new ReadOnlyObjectWrapper<>(cd.getValue().getBaseSalary()));

        loadPositions();
        tablePosition.setItems(positionList);
        tablePosition.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{ if(n!=null) showPositionDetail(n); });
        tablePosition.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML private void onCreatePosition() { showCreateForm(); }

    private void loadPositions() {
        try {
            String json = ApiHttpClientCaller.call("position", ApiHttpClientCaller.Method.GET, null);
            List<ResponsePositionDto> list = mapper.readValue(json, new TypeReference<>(){});
            positionList.setAll(list);
        } catch(Exception e) {
            e.printStackTrace(); showErrorAlert("Lỗi tải dữ liệu","Không thể tải danh sách chức vụ.");
        }
    }

    private void showPositionDetail(ResponsePositionDto pos) {
        detailPane.getChildren().clear();

        // Tiêu đề
        Label title = new Label("» Chi tiết chức vụ – ID: " + pos.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        // Thông tin cơ bản
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8));
        Function<String, Label> lb = txt -> {
            Label l = new Label(txt);
            l.setStyle("-fx-font-weight:bold;");
            return l;
        };
        grid.add(lb.apply("Tên:"), 0, 0);
        grid.add(new Label(pos.getName()), 1, 0);
        grid.add(lb.apply("Lương cơ bản:"), 0, 1);
        grid.add(new Label(String.valueOf(pos.getBaseSalary())), 1, 1);

        // Chỉ hiển thị số lượng nhân viên
        int staffCount = pos.getStaffIds() == null ? 0 : pos.getStaffIds().size();
        Label countLabel = new Label("Số nhân viên: " + staffCount);
        countLabel.setStyle("-fx-padding:8 0 8 0; -fx-font-style:italic;");

        // Nút hành động
        HBox actions = new HBox(12);
        actions.setPadding(new Insets(12,0,0,0));
        Button btnEdit = new Button("✏️ Sửa");
        btnEdit.setOnAction(e -> showEditForm(pos));
        Button btnDel  = new Button("🗑️ Xóa");
        btnDel .setOnAction(e -> deletePosition(pos));
        actions.getChildren().addAll(btnEdit, btnDel);

        detailPane.getChildren().addAll(title, grid, countLabel, actions);
    }


    private void deletePosition(ResponsePositionDto pos) {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION,"Xóa chức vụ này?",ButtonType.OK,ButtonType.CANCEL);
        c.showAndWait().filter(b->b==ButtonType.OK).ifPresent(b->{
            try{ ApiHttpClientCaller.call("position/"+pos.getId(),ApiHttpClientCaller.Method.DELETE,null);
                showInfoAlert("Đã xóa","Chức vụ ID:"+pos.getId()); loadPositions();
            }catch(Exception e){e.printStackTrace(); showErrorAlert("Lỗi","Không thể xóa chức vụ.");}
        });
    }

    private void showEditForm(ResponsePositionDto pos){
        detailPane.getChildren().clear();
        Label title=new Label("» Sửa chức vụ – ID:"+pos.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");
        GridPane grid=new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(8));
        Function<String,Label> lb=txt->{Label l=new Label(txt);l.setStyle("-fx-font-weight:bold;");return l;};

        grid.add(lb.apply("Tên:"),0,0);
        TextField tfName=new TextField(pos.getName()); grid.add(tfName,1,0);
        grid.add(lb.apply("Lương cơ bản:"),0,1);
        TextField tfSal=new TextField(String.valueOf(pos.getBaseSalary())); grid.add(tfSal,1,1);

        HBox btns=new HBox(12); btns.setPadding(new Insets(12,0,0,0));
        Button save=new Button("💾 Lưu"),cancel=new Button("❌ Hủy");
        cancel.setOnAction(e->showPositionDetail(pos));
        save.setOnAction(e->{
            String name=tfName.getText().trim();
            if(positionList.stream().anyMatch(r->r.getName().equalsIgnoreCase(name) && r.getId()!=pos.getId())){
                showErrorAlert("Lỗi","Tên chức vụ đã tồn tại!");return;
            }
            try{
                PositionDto dto=PositionDto.builder().name(name)
                        .baseSalary(Double.parseDouble(tfSal.getText().trim())).build();
                ApiHttpClientCaller.call("position/"+pos.getId(),ApiHttpClientCaller.Method.PUT,dto);
                showInfoAlert("Cập nhật","Chức vụ đã được cập nhật"); loadPositions();
            }catch(Exception ex){ex.printStackTrace(); showErrorAlert("Lỗi","Không thể cập nhật.");}
        });
        btns.getChildren().addAll(save,cancel);
        detailPane.getChildren().addAll(title,grid,btns);
    }

    private void showCreateForm(){
        detailPane.getChildren().clear();
        Label title=new Label("» Tạo mới chức vụ");
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");
        GridPane grid=new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(8));
        Function<String,Label> lb=txt->{Label l=new Label(txt);l.setStyle("-fx-font-weight:bold;");return l;};

        grid.add(lb.apply("Tên:"),0,0);
        TextField tfName=new TextField(); grid.add(tfName,1,0);
        grid.add(lb.apply("Lương cơ bản:"),0,1);
        TextField tfSal=new TextField(); grid.add(tfSal,1,1);

        HBox btns=new HBox(12); btns.setPadding(new Insets(12,0,0,0));
        Button save=new Button("💾 Lưu"),cancel=new Button("❌ Hủy");
        cancel.setOnAction(e->detailPane.getChildren().clear());
        save.setOnAction(e->{
            String name=tfName.getText().trim();
            if(positionList.stream().anyMatch(r->r.getName().equalsIgnoreCase(name))){
                showErrorAlert("Lỗi","Tên chức vụ đã tồn tại!");return;
            }
            try{
                PositionDto dto=PositionDto.builder().name(name)
                        .baseSalary(Double.parseDouble(tfSal.getText().trim())).build();
                ApiHttpClientCaller.call("position",ApiHttpClientCaller.Method.POST,dto);
                showInfoAlert("Tạo thành công","Chức vụ mới đã được thêm"); loadPositions();
            }catch(Exception ex){ex.printStackTrace(); showErrorAlert("Lỗi","Không thể tạo.");}
        });
        btns.getChildren().addAll(save,cancel);
        detailPane.getChildren().addAll(title,grid,btns);
    }

    private void showInfoAlert(String h,String c){ Alert a=new Alert(Alert.AlertType.INFORMATION);a.setHeaderText(h);a.setContentText(c);a.showAndWait(); }
    private void showErrorAlert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(header);
        a.setContentText(content);

        // Thêm stylesheet cho DialogPane
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }
}
