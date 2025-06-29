package com.example.frontendquanlikhachsan.controllers.admin;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.variable.ResponseVariableDto;
import com.example.frontendquanlikhachsan.entity.variable.VariableDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.List;

public class RuleVariableController {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @FXML
    private TextField tfId;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfValue;
    @FXML
    private TextArea taDescription;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnSave;

    @FXML
    private TableView<ResponseVariableDto> tableVariables;
    @FXML
    private TableColumn<ResponseVariableDto, Integer> colId;
    @FXML
    private TableColumn<ResponseVariableDto, String> colName;
    @FXML
    private TableColumn<ResponseVariableDto, Double> colValue;
    @FXML
    private TableColumn<ResponseVariableDto, String> colDescription;

    private ResponseVariableDto selectedVariable;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));
        colValue.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getValue()));
        colDescription.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescription()));

        loadVariables();

        tableVariables.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedVariable = newVal;
            showVariableDetail(newVal);
        });

        tableVariables.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        btnEdit.setOnAction(e -> enableEditing(true));

        btnSave.setOnAction(e -> {
            if (selectedVariable != null) {
                updateVariable();
            }
        });

        enableEditing(false);
    }

    private void loadVariables() {
        try {
            String json = ApiHttpClientCaller.call("variable", ApiHttpClientCaller.Method.GET, null);
            List<ResponseVariableDto> variables = Arrays.asList(mapper.readValue(json, ResponseVariableDto[].class));
            tableVariables.setItems(FXCollections.observableArrayList(variables));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải danh sách biến.");
        }
    }

    private void showVariableDetail(ResponseVariableDto variable) {
        if (variable == null) return;
        tfId.setText(String.valueOf(variable.getId()));
        tfName.setText(variable.getName());
        tfValue.setText(String.valueOf(variable.getValue()));
        taDescription.setText(variable.getDescription());
        enableEditing(false);
    }

    private void enableEditing(boolean enable) {
        tfValue.setEditable(enable);
        taDescription.setEditable(enable);
        btnSave.setDisable(!enable);
    }

    private void updateVariable() {
        try {
            VariableDto dto = VariableDto.builder()
                    .name(tfName.getText().trim())
                    .value(Double.parseDouble(tfValue.getText().trim()))
                    .description(taDescription.getText().trim())
                    .build();

            ApiHttpClientCaller.call("variable/" + selectedVariable.getId(), ApiHttpClientCaller.Method.PUT, dto);

            showInfo("Cập nhật thành công.");
            loadVariables(); // reload lại danh sách
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi cập nhật biến.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Lỗi");
        alert.setContentText(message);

        // Thêm stylesheet cho DialogPane
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/frontendquanlikhachsan/assets/css/alert.css").toExternalForm()
        );
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Thông báo");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
