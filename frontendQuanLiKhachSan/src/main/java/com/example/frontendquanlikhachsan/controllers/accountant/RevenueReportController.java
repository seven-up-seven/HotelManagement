package com.example.frontendquanlikhachsan.controllers.accountant;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.revenueReport.RevenueReportDto;
import com.example.frontendquanlikhachsan.entity.revenueReportDetail.ResponseRevenueReportDetailDto;
import com.example.frontendquanlikhachsan.entity.revenueReport.ResponseRevenueReportDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.*;

public class RevenueReportController {

    @FXML private Button btnGenerate, btnDelete;

    @FXML private ComboBox<Short>      cbYear;
    @FXML private ComboBox<Byte>       cbMonth;
    @FXML private TableView<ResponseRevenueReportDto> tblReport;
    @FXML private TableColumn<ResponseRevenueReportDto,Integer> colReportId;
    @FXML private TableColumn<ResponseRevenueReportDto,Short>   colReportYear;
    @FXML private TableColumn<ResponseRevenueReportDto,Byte>    colReportMonth;
    @FXML private TableColumn<ResponseRevenueReportDto,Double>  colReportTotal;

    @FXML private PieChart    pieChart;
    @FXML private VBox detailContainer;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String token = "";

    @FXML
    public void initialize() {
        // 1) Khởi tạo Year/Month combo
        for(short y = 2020; y <= 2030; y++) cbYear.getItems().add(y);
        for(byte  m =   1; m <= 12;   m++) cbMonth.getItems().add(m);
        cbYear.getSelectionModel().select((short)2025);
        cbMonth.getSelectionModel().select((byte)6);

        // 2) Thiết lập cột báo cáo
        colReportId   .setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
        colReportYear .setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getYear()));
        colReportMonth.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getMonth()));
        colReportTotal.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalMonthRevenue()));

        // 3) Khi nhấn nút Tạo mới báo cáo
        btnGenerate.setOnAction(e -> {
            Short year  = cbYear.getValue();
            Byte  month = cbMonth.getValue();
            if (year==null||month==null) return;
            Alert cf = new Alert(Alert.AlertType.CONFIRMATION,
                    "Tạo báo cáo "+month+"/"+year+"?", ButtonType.OK, ButtonType.CANCEL
            );
            cf.showAndWait().filter(b->b==ButtonType.OK).ifPresent(b-> {
                try {
                    RevenueReportDto req = new RevenueReportDto();
                    req.setYear(year); req.setMonth(month);
                    ApiHttpClientCaller.call("revenue-report", POST, req, token);
                } catch(Exception ex) {
                    showError("Lỗi tạo", ex.getMessage());
                }
                loadReports();
            });
        });

        btnDelete.setOnAction(e -> {
            ResponseRevenueReportDto sel = tblReport.getSelectionModel().getSelectedItem();
            if (sel==null) return;
            Alert cf = new Alert(Alert.AlertType.CONFIRMATION,
                    "Xóa báo cáo #" + sel.getId() + "?", ButtonType.OK, ButtonType.CANCEL
            );
            cf.showAndWait().filter(b->b==ButtonType.OK).ifPresent(b-> {
                try {
                    ApiHttpClientCaller.call(
                            "revenue-report/" + sel.getId(), DELETE, null, token
                    );
                } catch(Exception ex) {
                    showError("Lỗi xóa", ex.getMessage());
                }
                loadReports();
            });
        });

        // 4) Khi đổi combo chỉ load danh sách hiện có
        cbYear.valueProperty().addListener((o,ov,nv)-> loadReports());
        cbMonth.valueProperty().addListener((o,ov,nv)-> loadReports());

        // 5) Khi click vào 1 dòng => hiển thị detail
        tblReport.getSelectionModel().selectedItemProperty().addListener((obs,old,sel)-> {
            boolean has = sel != null;
            btnDelete.setDisable(!has);
            if(has) showDetail(sel);
        });

        // 6) Lần đầu chỉ load, không tạo
        loadReports();

        tblReport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadReports() {
        new Thread(() -> {
            try {
                String json = ApiHttpClientCaller.call("revenue-report", GET, null, token);
                List<ResponseRevenueReportDto> all =
                        mapper.readValue(json, new TypeReference<List<ResponseRevenueReportDto>>(){});
                Short year  = cbYear.getValue();
                Byte  month = cbMonth.getValue();
                List<ResponseRevenueReportDto> filtered = all.stream()
                        .filter(r -> r.getYear().equals(year) && r.getMonth().equals(month))
                        .toList();
                Platform.runLater(() -> {
                    tblReport.setItems(FXCollections.observableArrayList(filtered));
                    if (!filtered.isEmpty()) tblReport.getSelectionModel().selectFirst();
                });
            } catch(Exception e) {
                Platform.runLater(() -> showError("Lỗi tải báo cáo", e.getMessage()));
            }
        }).start();
    }

    private void generateAndReload() {
        Short year  = cbYear.getValue();
        Byte  month = cbMonth.getValue();
        if (year==null || month==null) return;

        // 1) Gọi API tạo/generate báo cáo tháng-năm
        Platform.runLater(() -> {
            try {
                RevenueReportDto req = new RevenueReportDto();
                req.setYear(year);
                req.setMonth(month);
                ApiHttpClientCaller.call("revenue-report", POST, req, token);
            } catch(Exception ignored) {}
            // 2) Load danh sách báo cáo
            loadReports();
        });
    }

    private void showDetail(ResponseRevenueReportDto rpt) {
        // Xóa nội dung cũ
        detailContainer.getChildren().clear();

        // --- 1) PieChart ---
        pieChart.getData().clear();
        for(Integer detId : rpt.getRevenueReportDetailIds()) {
            try {
                String j = ApiHttpClientCaller.call(
                        "revenue-report-detail/" + detId, GET, null, token
                );
                ResponseRevenueReportDetailDto d = mapper.readValue(
                        j, ResponseRevenueReportDetailDto.class
                );
                pieChart.getData().add(
                        new PieChart.Data(d.getRoomTypeName(), d.getTotalRoomRevenue())
                );
            } catch(Exception ex) {
                // ignore
            }
        }
        // Tăng cỡ chữ legend
        pieChart.setLegendSide(Side.BOTTOM);
        pieChart.lookupAll(".chart-legend-item").forEach(n -> n.setStyle("-fx-font-size:14px;"));

        // --- 2) Bảng thống kê với GridPane ---
        // Lấy danh sách detail
        List<ResponseRevenueReportDetailDto> details = rpt.getRevenueReportDetailIds().stream()
                .map(id -> {
                    try {
                        String j = ApiHttpClientCaller.call(
                                "revenue-report-detail/" + id, GET, null, token
                        );
                        return mapper.readValue(j, ResponseRevenueReportDetailDto.class);
                    } catch (Exception e) { return null; }
                })
                .filter(d -> d != null)
                .toList();

        double total = details.stream()
                .mapToDouble(ResponseRevenueReportDetailDto::getTotalRoomRevenue)
                .sum();

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        // Header
        Label h1 = new Label("Loại phòng");
        Label h2 = new Label("Doanh thu (VNĐ)");
        Label h3 = new Label("Tỷ lệ (%)");
        h1.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        h2.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        h3.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        grid.add(h1, 0, 0);
        grid.add(h2, 1, 0);
        grid.add(h3, 2, 0);

        // Nội dung
        var fmt = new java.text.DecimalFormat("#,##0");
        int row = 1;
        for (var d : details) {
            double rev = d.getTotalRoomRevenue();
            double pct = total == 0 ? 0 : (rev / total) * 100.0;

            Label c1 = new Label(d.getRoomTypeName());
            Label c2 = new Label(fmt.format(rev) + "₫");
            Label c3 = new Label(String.format("%.1f%%", pct));

            c1.setStyle("-fx-font-size:13px;");
            c2.setStyle("-fx-font-size:13px;");
            c3.setStyle("-fx-font-size:13px;");

            grid.add(c1, 0, row);
            grid.add(c2, 1, row);
            grid.add(c3, 2, row);
            row++;
        }

        // Dòng tổng
        Label sumLabel = new Label("Tổng tháng:");
        Label sumValue = new Label(fmt.format(total) + "₫");
        sumLabel.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-padding:6 0 0 0;");
        sumValue.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-padding:6 0 0 0;");

        grid.add(sumLabel, 0, row);
        grid.add(sumValue, 1, row);

        // --- 3) Thả GridPane vào container ---
        detailContainer.getChildren().add(grid);
    }


    private void showError(String h, String c) {
        Alert a = new Alert(Alert.AlertType.ERROR, c, ButtonType.OK);
        a.setHeaderText(h);
        a.showAndWait();
    }
}
