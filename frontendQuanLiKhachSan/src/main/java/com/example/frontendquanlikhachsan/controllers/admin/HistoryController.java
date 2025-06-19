package com.example.frontendquanlikhachsan.controllers.admin;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.history.ResponseHistoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryController {
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @FXML
    private TableView<ResponseHistoryDto> tableHistory;

    @FXML
    private TableColumn<ResponseHistoryDto, Integer> colId;
    @FXML
    private TableColumn<ResponseHistoryDto, String> colImpactor;
    @FXML
    private TableColumn<ResponseHistoryDto, String> colAction;
    @FXML
    private TableColumn<ResponseHistoryDto, String> colObject;
    @FXML
    private TableColumn<ResponseHistoryDto, String> colTime;

    @FXML
    private VBox historyDetailContainer;

    @FXML
    private Button ResetButton;

    @FXML
    private TextField tfFilterImpactor;
    @FXML
    private TextField tfFilterImpactorId;
    @FXML
    private TextField tfFilterObject;
    @FXML
    private TextField tfFilterObjectId;
    @FXML
    private DatePicker dpFilterFrom;
    @FXML
    private DatePicker dpFilterTo;

    private List<ResponseHistoryDto> fullHistoryList;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colImpactor.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getImpactor()));
        colAction.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getAction().toString()));
        colObject.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getAffectedObject()));
        colTime.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                data.getValue().getExecuteAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        tableHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadHistories();

        tableHistory.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) showHistoryDetail(newSel);
        });

        tfFilterImpactor.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        tfFilterImpactorId.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        tfFilterObject.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        tfFilterObjectId.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        dpFilterFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        dpFilterTo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        ResetButton.setOnAction(e -> resetFilters());
    }

    private void loadHistories() {
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("history", ApiHttpClientCaller.Method.GET, null);
                List<ResponseHistoryDto> list = Arrays.asList(mapper.readValue(
                        json, ResponseHistoryDto[].class));
                fullHistoryList = list;

                Platform.runLater(() -> tableHistory.getItems().setAll(list));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi tải dữ liệu lịch sử: " + e.getMessage()));
            }
        }).start();
    }

    private void applyFilter() {
        if (fullHistoryList == null) return;

        String impactor = tfFilterImpactor.getText().toLowerCase().trim();
        String impactorId = tfFilterImpactorId.getText().trim();
        String object = tfFilterObject.getText().toLowerCase().trim();
        String objectId = tfFilterObjectId.getText().trim();
        LocalDate from = dpFilterFrom.getValue();
        LocalDate to = dpFilterTo.getValue();

        List<ResponseHistoryDto> filtered = fullHistoryList.stream()
                .filter(h -> impactor.isEmpty() || h.getImpactor().toLowerCase().contains(impactor))
                .filter(h -> impactorId.isEmpty() || (h.getImpactorId() != null && h.getImpactorId().toString().equals(impactorId)))
                .filter(h -> object.isEmpty() || h.getAffectedObject().toLowerCase().contains(object))
                .filter(h -> objectId.isEmpty() || (h.getAffectedObjectId() != null && h.getAffectedObjectId().toString().equals(objectId)))
                .filter(h -> {
                    if (from == null && to == null) return true;
                    LocalDateTime time = h.getExecuteAt();
                    if (from != null && time.isBefore(from.atStartOfDay())) return false;
                    if (to != null && time.isAfter(to.atTime(LocalTime.MAX))) return false;
                    return true;
                })
                .collect(Collectors.toList());

        tableHistory.getItems().setAll(filtered);
    }

    private void resetFilters() {
        tfFilterImpactor.clear();
        tfFilterImpactorId.clear();
        tfFilterObject.clear();
        tfFilterObjectId.clear();
        dpFilterFrom.setValue(null);
        dpFilterTo.setValue(null);
        tableHistory.getItems().setAll(fullHistoryList);
    }

    private void showHistoryDetail(ResponseHistoryDto history) {
        historyDetailContainer.getChildren().clear();

        historyDetailContainer.getChildren().addAll(
                createDetailLabel("ID lịch sử: " + history.getId()),
                createDetailLabel("Người tác động: " + history.getImpactor()),
                createDetailLabel("ID người tác động: " + history.getImpactorId()),
                createDetailLabel("Đối tượng ảnh hưởng: " + history.getAffectedObject()),
                createDetailLabel("ID đối tượng ảnh hưởng: " + history.getAffectedObjectId()),
                createDetailLabel("Hành động: " + history.getAction()),
                createDetailLabel("Thời gian: " + history.getExecuteAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
                createDetailLabel("Nội dung: " + history.getContent())
        );
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px;");
        return label;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Lỗi");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
