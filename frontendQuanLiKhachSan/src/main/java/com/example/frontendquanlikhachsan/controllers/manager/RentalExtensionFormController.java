package com.example.frontendquanlikhachsan.controllers.manager;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.RentalExtensionFormDto;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.ResponseRentalExtensionFormDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
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
    @FXML private VBox detailPane;

    @FXML private TextField tfFilterExtId;
    @FXML private TextField tfFilterFormId;
    @FXML private TextField tfFilterRoomName;

    private final ObservableList<ResponseRentalExtensionFormDto> extList = FXCollections.observableArrayList();
    private FilteredList<ResponseRentalExtensionFormDto> filtered;
    private final ObjectMapper mapper = new ObjectMapper();
    private List<Integer> multiFilterIds = null;
    private ResponseRentalExtensionFormDto editingDto = null;

    @FXML public void initialize() {

        // 1) Column bindings
        colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colRentalFormId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getRentalFormId()));
        colRoomName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getRentalFormRoomName()));
        colDays.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getNumberOfRentalDays()));
        colStaffName.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStaffName()));

        // 2) Wrap into FilteredList + SortedList
        filtered = new FilteredList<>(extList, e -> true);
        SortedList<ResponseRentalExtensionFormDto> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableExtension.comparatorProperty());
        tableExtension.setItems(sorted);

        // 3) Listeners on filter inputs
        tfFilterExtId.textProperty().addListener((o,oldV,newV)->applyFilters());
        tfFilterFormId.textProperty().addListener((o,oldV,newV)->applyFilters());
        tfFilterRoomName.textProperty().addListener((o,oldV,newV)->applyFilters());

        // 4) Load data & row selection
        loadExtensions();
        tableExtension.getSelectionModel().selectedItemProperty()
                .addListener((obs,o,n)-> { if(n!=null) showDetail(n); });

        tableExtension.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML private void onResetFilter() {
        multiFilterIds = null;
        tfFilterExtId.clear();
        tfFilterFormId.clear();
        tfFilterRoomName.clear();
        applyFilters();
        // chuyển selectionMode về đơn lẻ nếu cần
        tableExtension.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @FXML private void onCreateOrEditExtension() {
        showCreateForm();
    }

    private void applyFilters() {
        if (multiFilterIds != null) {
            filtered.setPredicate(dto -> multiFilterIds.contains(dto.getId()));
            return;
        }

        String extIdText    = tfFilterExtId.getText().trim();
        String formIdText   = tfFilterFormId.getText().trim();
        String roomNameText = tfFilterRoomName.getText().trim().toLowerCase();

        filtered.setPredicate(dto -> {
            // filter by extension ID
            if (!extIdText.isEmpty()) {
                // chỉ filter khi extIdText chỉ toàn chữ số
                if (extIdText.matches("\\d+")) {
                    String idStr = String.valueOf(dto.getId());
                    if (!idStr.contains(extIdText)) return false;
                }
            }
            // filter by rentalFormId
            if (!formIdText.isEmpty()) {
                try {
                    if (dto.getRentalFormId() != Integer.parseInt(formIdText)) return false;
                } catch(NumberFormatException e){ return false; }
            }
            // filter by room name contains
            if (!roomNameText.isEmpty()
                    && !dto.getRentalFormRoomName().toLowerCase().contains(roomNameText)) {
                return false;
            }
            return true;
        });
    }

    private void loadExtensions() {
        try {
            String json = ApiHttpClientCaller.call(
                    "rental-extension-form",
                    ApiHttpClientCaller.Method.GET,
                    null
            );
            List<ResponseRentalExtensionFormDto> list =
                    mapper.readValue(json, new TypeReference<>() {});
            extList.setAll(list);
        } catch(Exception e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải", "Không thể tải danh sách gia hạn.");
        }
    }

    public void selectExtensionsByIds(List<Integer> ids) {
        multiFilterIds = new ArrayList<>(ids);
        tableExtension.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        applyFilters();
        // chọn và show detail bản ghi đầu
        if (!ids.isEmpty()) {
            tableExtension.getItems().stream()
                    .filter(d -> d.getId() == ids.get(0))
                    .findFirst()
                    .ifPresent(this::showDetail);
        }
    }

    private void showDetail(ResponseRentalExtensionFormDto dto) {
        detailPane.getChildren().clear();
        Label title = new Label("» Chi tiết Gia hạn – ID: " + dto.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String,Label> lb = txt->{ Label l=new Label(txt); l.setStyle("-fx-font-weight:bold;"); return l; };

        grid.add(lb.apply("ID Phiếu thuê:"),0,0);
        grid.add(new Label(String.valueOf(dto.getRentalFormId())),1,0);
        grid.add(lb.apply("Tên Phòng:"),0,1);
        grid.add(new Label(dto.getRentalFormRoomName()),1,1);
        grid.add(lb.apply("Số ngày gia hạn:"),0,2);
        grid.add(new Label(String.valueOf(dto.getNumberOfRentalDays())),1,2);
        grid.add(lb.apply("Nhân viên xử lý:"),0,3);
        grid.add(new Label(dto.getStaffName()),1,3);

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(12,0,0,0));
        Button btnEdit = new Button("Sửa"); btnEdit.setOnAction(e-> showEditForm(dto));
        Button btnDel  = new Button("Xóa"); btnDel.setOnAction(e-> deleteExtension(dto));
        actions.getChildren().addAll(btnEdit, btnDel);

        detailPane.getChildren().addAll(title, grid, actions);
    }

    private void deleteExtension(ResponseRentalExtensionFormDto dto) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Xóa gia hạn này?", ButtonType.OK, ButtonType.CANCEL);
        c.showAndWait().filter(b->b==ButtonType.OK).ifPresent(b->{
            try{
                ApiHttpClientCaller.call("rental-extension-form/"+dto.getId(),ApiHttpClientCaller.Method.DELETE,null);
                showInfoAlert("Đã xóa", "Gia hạn ID:"+dto.getId());
                loadExtensions();
            }catch(Exception ex){ex.printStackTrace(); showErrorAlert("Lỗi","Không thể xóa.");}
        });
    }

    private void showCreateForm() {
        editingDto = null;
        detailPane.getChildren().clear();
        Label title = new Label("» Tạo/Sửa Gia hạn");
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String,Label> lb = txt->{ Label l=new Label(txt); l.setStyle("-fx-font-weight:bold;"); return l; };

        // Bước 1: Nhập rentalFormId và Check
        grid.add(lb.apply("ID Phiếu thuê:"),0,0);
        TextField tfFormId = new TextField(); grid.add(tfFormId,1,0);
        Button btnCheck = new Button("Kiểm tra"); grid.add(btnCheck,2,0);
        Label lblInfo = new Label(); grid.add(lblInfo,1,1,2,1);

        // Container cho bước 2
        VBox step2 = new VBox(8);
        step2.setPadding(new Insets(10,0,0,0));

        btnCheck.setOnAction(e->{
            try {
                int formId = Integer.parseInt(tfFormId.getText().trim());
                String jsonRem = ApiHttpClientCaller.call(
                        "rental-extension-form/day-remains/"+formId,
                        ApiHttpClientCaller.Method.GET, null
                );
                int remains = new ObjectMapper().readValue(jsonRem, Integer.class);
                if(remains<=0) {
                    lblInfo.setText("Không thể gia hạn thêm");
                    step2.getChildren().clear();
                } else if(remains>5) {
                    lblInfo.setText("Tối đa được gia hạn 5 ngày");
                    showDaysInput(step2, formId, (short)5);
                    lblInfo.setText("");
                } else {
                    lblInfo.setText("");
                    showDaysInput(step2, formId, (short)remains);
                }
            } catch(NumberFormatException ex) {
                lblInfo.setText("ID phải là số nguyên");
                step2.getChildren().clear();
            } catch(Exception ex) {
                ex.printStackTrace(); lblInfo.setText("Lỗi kết nối");
                step2.getChildren().clear();
            }
        });

        detailPane.getChildren().addAll(title, grid, step2);
    }

    private void showDaysInput(VBox container, int formId, short maxDays) {
        container.getChildren().clear();
        GridPane g2 = new GridPane(); g2.setHgap(10); g2.setVgap(10);
        Function<String,Label> lb = txt->{ Label l=new Label(txt); l.setStyle("-fx-font-weight:bold;"); return l; };
        g2.add(lb.apply("Số ngày (<= " + maxDays + "):"),0,0);
        TextField tfDays = new TextField(); g2.add(tfDays,1,0);
        g2.add(lb.apply("ID Nhân viên:"),0,1);
        TextField tfStaff = new TextField(); g2.add(tfStaff,1,1);

        Button btnSave = new Button("💾 Lưu");
        btnSave.setOnAction(e->{
            try {
                short days = Short.parseShort(tfDays.getText().trim());
                if(days<1 || days>maxDays) {
                    showErrorAlert("Lỗi","Số ngày phải từ 1 đến " + maxDays);
                    return;
                }
                int staffId = Integer.parseInt(tfStaff.getText().trim());
                RentalExtensionFormDto dto = RentalExtensionFormDto.builder()
                        .rentalFormId(formId)
                        .numberOfRentalDays(days)
                        .staffId(staffId)
                        .build();
                ApiHttpClientCaller.call("rental-extension-form",ApiHttpClientCaller.Method.POST,dto);
                showInfoAlert("Tạo thành công","Đã tạo gia hạn.");
                loadExtensions();
            } catch(NumberFormatException ex) {
                showErrorAlert("Lỗi","Nhập đúng định dạng số.");
            } catch(Exception ex) {
                ex.printStackTrace(); showErrorAlert("Lỗi","Không thể tạo gia hạn.");
            }
        });

        container.getChildren().addAll(g2, btnSave);
    }

    private void showEditForm(ResponseRentalExtensionFormDto dto) {
        editingDto = dto;
        detailPane.getChildren().clear();
        Label title = new Label("» Sửa Gia hạn – ID: " + dto.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-padding:0 0 8 0;");

        // Tương tự create nhưng tải sẵn values
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(8));
        Function<String,Label> lb = txt->{ Label l=new Label(txt); l.setStyle("-fx-font-weight:bold;"); return l; };
        grid.add(lb.apply("Số ngày gia hạn:"),0,0);
        TextField tfDays = new TextField(String.valueOf(dto.getNumberOfRentalDays())); grid.add(tfDays,1,0);
        grid.add(lb.apply("ID Nhân viên:"),0,1);
        TextField tfStaff = new TextField(String.valueOf(dto.getStaffId())); grid.add(tfStaff,1,1);

        Button btnSave = new Button("💾 Lưu");
        btnSave.setOnAction(e->{
            try {
                short days = Short.parseShort(tfDays.getText().trim());
                int staffId = Integer.parseInt(tfStaff.getText().trim());
                dto.setNumberOfRentalDays(days);
                dto.setStaffId(staffId);
                ApiHttpClientCaller.call("rental-extension-form/"+dto.getId(),
                        ApiHttpClientCaller.Method.PUT, dto);
                showInfoAlert("Cập nhật thành công","Gia hạn đã được cập nhật");
                loadExtensions();
            } catch(Exception ex) {
                ex.printStackTrace(); showErrorAlert("Lỗi","Không thể cập nhật.");
            }
        });
        Button btnCancel = new Button("❌ Hủy"); btnCancel.setOnAction(e-> showDetail(dto));

        HBox btns = new HBox(10, btnSave, btnCancel); btns.setPadding(new Insets(12,0,0,0));
        detailPane.getChildren().addAll(title, grid, btns);
    }

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
    private void showInfoAlert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.setContentText(content);

        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }
}