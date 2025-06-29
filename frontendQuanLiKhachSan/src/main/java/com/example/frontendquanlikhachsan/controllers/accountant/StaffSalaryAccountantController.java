package com.example.frontendquanlikhachsan.controllers.accountant;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.position.PositionDropdownChoice;
import com.example.frontendquanlikhachsan.entity.position.ResponsePositionDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.staff.StaffDto;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.*;
import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.mapper;

public class StaffSalaryAccountantController {

    @FXML private TextField     tfFilterId;
    @FXML private TextField     tfFilterName;
    @FXML private ComboBox<String> cbFilterPosition;
    @FXML private TextField     tfFilterMulMin;
    @FXML private TextField     tfFilterMulMax;
    @FXML private TextField     tfFilterSalMin;
    @FXML private TextField     tfFilterSalMax;

    @FXML private TableView<ResponseStaffDto> tableStaff;
    @FXML private TableColumn<ResponseStaffDto,Integer> colId;
    @FXML private TableColumn<ResponseStaffDto,String>  colFullName;
    @FXML private TableColumn<ResponseStaffDto,String>  colPosition;
    @FXML private TableColumn<ResponseStaffDto,String>  colMultiplier;
    @FXML private TableColumn<ResponseStaffDto, Double>  colSalary;

    private final Map<Integer, Double> salaryMap = new HashMap<>();
    private final ObservableList<ResponseStaffDto> staffList = FXCollections.observableArrayList();
    private FilteredList<ResponseStaffDto> filteredStaff;
    @FXML private VBox detailPane;

    private final String token = ""; // gán token thực

    @FXML
    public void initialize() {
        colId        .setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
        colFullName  .setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getFullName()));
        colPosition  .setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPositionName()));
        colMultiplier  .setCellValueFactory(cd -> {
            Double u = Double.valueOf(cd.getValue().getSalaryMultiplier());
            return new ReadOnlyObjectWrapper<>(u).asString();
        });
        colMultiplier.setCellFactory(column -> new TableCell<ResponseStaffDto, String>() {
            @Override
            protected void updateItem(String multiplier, boolean empty) {
                super.updateItem(multiplier, empty);
                if (empty || multiplier == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", Double.parseDouble(multiplier)));
                }
            }
        });
        colSalary   .setCellValueFactory(cd -> {
            Double sal = salaryMap.get(cd.getValue().getId());
            return new ReadOnlyObjectWrapper<>(sal != null ? sal : 0.0);
        });
        colSalary.setCellFactory(column -> new TableCell<ResponseStaffDto, Double>() {
            @Override
            protected void updateItem(Double salary, boolean empty) {
                super.updateItem(salary, empty);
                if (empty || salary == null) {
                    setText(null);
                } else {
                    setText(String.format("%.3f", salary));
                }
            }
        });

        loadStaffs();

        filteredStaff = new FilteredList<>(staffList, p -> true);
        SortedList<ResponseStaffDto> sorted =
                new SortedList<>(filteredStaff);
        sorted.comparatorProperty().bind(tableStaff.comparatorProperty());
        tableStaff.setItems(sorted);

        // Sau khi setItems(sorted):
        tableStaff.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            detailPane.getChildren().clear();
            if (sel != null) {
                showStaffDetail(sel);
            }
        });


        // 4) Populate position filter (bao gồm lựa chọn "Tất cả")
        cbFilterPosition.getItems().add("Tất cả");
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("position", GET, null, token);
                List<ResponsePositionDto> pos = mapper.readValue(json, new TypeReference<>(){});
                List<String> names = pos.stream()
                        .map(ResponsePositionDto::getName)
                        .sorted()
                        .toList();
                Platform.runLater(() -> cbFilterPosition.getItems().addAll(names));
            } catch (Exception e) {
                // ignore hoặc log
            }
        }).start();
        cbFilterPosition.getSelectionModel().selectFirst();

        // 5) Khi bất kỳ filter nào thay đổi, cập nhật predicate
        Runnable apply = this::applyFilters;
        tfFilterId.textProperty()      .addListener((o,ov,nv)->apply.run());
        tfFilterName.textProperty()    .addListener((o,ov,nv)->apply.run());
        cbFilterPosition.valueProperty().addListener((o,ov,nv)->apply.run());
        tfFilterMulMin.textProperty()  .addListener((o,ov,nv)->apply.run());
        tfFilterMulMax.textProperty()  .addListener((o,ov,nv)->apply.run());
        tfFilterSalMin.textProperty()  .addListener((o,ov,nv)->apply.run());
        tfFilterSalMax.textProperty()  .addListener((o,ov,nv)->apply.run());

        tableStaff.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadStaffs() {
        // 1) load all staff
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("staff", GET, null, token);
                List<ResponseStaffDto> list = mapper.readValue(
                        json, new TypeReference<>() {});
                Platform.runLater(() -> {
                    staffList.setAll(list);
                    // 2) sau khi load xong, fetch salary cho từng nhân viên
                    list.forEach(s -> loadSalaryAsync(s.getId()));
                });
            } catch(Exception e) {
                Platform.runLater(() -> showError("Lỗi tải", "Không thể tải nhân viên"));
            }
        }).start();
    }

    private void loadSalaryAsync(int staffId) {
        new Thread(() -> {
            try {
                String res = ApiHttpClientCaller.call(
                        "staff/" + staffId + "/salary", GET, null, token);
                double sal = Double.parseDouble(res);
                Platform.runLater(() -> {
                    salaryMap.put(staffId, sal);
                    tableStaff.refresh();
                });
            } catch(Exception ignored){}
        }).start();
    }

    private void applyFilters() {
        String idText    = tfFilterId.getText().trim();
        String nameText  = tfFilterName.getText().trim().toLowerCase();
        String posText = cbFilterPosition.getValue();
        String mulMinStr = tfFilterMulMin.getText().trim();
        String mulMaxStr = tfFilterMulMax.getText().trim();
        String salMinStr = tfFilterSalMin.getText().trim();
        String salMaxStr = tfFilterSalMax.getText().trim();

        filteredStaff.setPredicate(staff -> {
            // ID
            if (!idText.isEmpty()) {
                try {
                    if (staff.getId() != Integer.parseInt(idText)) return false;
                } catch (NumberFormatException e) { return false; }
            }
            // Họ tên
            if (!nameText.isEmpty()
                    && !staff.getFullName().toLowerCase().contains(nameText)) return false;
            // Chức vụ
            if (posText != null && !"Tất cả".equals(posText) &&
                    !posText.equals(staff.getPositionName())) return false;
            // Hệ số lương
            float mul = staff.getSalaryMultiplier();
            if (!mulMinStr.isEmpty()) {
                try { if (mul < Float.parseFloat(mulMinStr)) return false; }
                catch(NumberFormatException e){ return false; }
            }
            if (!mulMaxStr.isEmpty()) {
                try { if (mul > Float.parseFloat(mulMaxStr)) return false; }
                catch(NumberFormatException e){ return false; }
            }
            // Lương
            Double sal = salaryMap.getOrDefault(staff.getId(), 0.0);
            if (!salMinStr.isEmpty()) {
                try { if (sal < Double.parseDouble(salMinStr)) return false; }
                catch(NumberFormatException e){ return false; }
            }
            if (!salMaxStr.isEmpty()) {
                try { if (sal > Double.parseDouble(salMaxStr)) return false; }
                catch(NumberFormatException e){ return false; }
            }
            return true;
        });
    }

    private void showStaffDetail(ResponseStaffDto staff) {
        detailPane.getChildren().clear();

        Label title = new Label("» Nhân viên #" + staff.getId() + " – " + staff.getFullName());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        Label lblMul = new Label("Hệ số lương: " + staff.getSalaryMultiplier());
        lblMul.setStyle("-fx-font-size:14px;");

        Button btnEditMul = new Button("✏️ Chỉnh sửa hệ số lương");
        btnEditMul.setOnAction(e -> showEditSalaryForm(staff));

        detailPane.getChildren().addAll(title, lblMul, btnEditMul);
    }

    private void showEditSalaryForm(ResponseStaffDto staff) {
        detailPane.getChildren().clear();

        Label title = new Label("✏️ Chỉnh sửa hệ số lương – NV#" + staff.getId());
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(8));

        form.add(new Label("Hệ số lương mới:"), 0, 0);
        TextField tfMul = new TextField(String.valueOf(staff.getSalaryMultiplier()));
        form.add(tfMul, 1, 0);

        Button btnSave   = new Button("💾 Lưu");
        Button btnCancel = new Button("❌ Hủy");

        btnSave.setOnAction(e -> {
            try {
                float mul = Float.parseFloat(tfMul.getText().trim());
                ApiHttpClientCaller.call(
                        "staff/" + staff.getId() + "/salary-multiplier?salaryMultiplier=" + mul,
                        PATCH,
                        null,
                        token
                );
                showInfo("Cập nhật thành công", "Hệ số lương đã được cập nhật");
                loadStaffs();                  // reload danh sách mới
                detailPane.getChildren().clear();
            } catch (Exception ex) {
                showError("Lỗi lưu", ex.getMessage());
            }
        });
        btnCancel.setOnAction(e -> showStaffDetail(staff));

        HBox actions = new HBox(10, btnSave, btnCancel);
        actions.setPadding(new Insets(12, 0, 0, 0));

        detailPane.getChildren().addAll(title, form, actions);
    }


    private void showError(String h, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(h);
        a.setContentText(c);

        a.getDialogPane().getStylesheets().add(getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }
    private void showInfo(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.setContentText(content);

        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        a.showAndWait();
    }
}
