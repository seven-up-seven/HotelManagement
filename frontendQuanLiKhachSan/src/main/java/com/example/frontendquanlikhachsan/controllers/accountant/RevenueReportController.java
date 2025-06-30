package com.example.frontendquanlikhachsan.controllers.accountant;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.revenueReport.RevenueReportDto;
import com.example.frontendquanlikhachsan.entity.revenueReportDetail.ResponseRevenueReportDetailDto;
import com.example.frontendquanlikhachsan.entity.revenueReport.ResponseRevenueReportDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import com.itextpdf.layout.element.Paragraph;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;


import javax.imageio.ImageIO;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.frontendquanlikhachsan.ApiHttpClientCaller.Method.*;

public class RevenueReportController {

    @FXML private ComboBox<Short>      cbYear;
    @FXML private Button btnExportPdf;
    @FXML private Button btnExportExcel;
    @FXML private Button btnBuilder;
    @FXML private BarChart<String, Number> barChart;
    @FXML private Pane comboChartContainer;
    @FXML private PieChart annualPieChart;
    @FXML private Label lblAnnualTotal;

    private SwingNode swingNode;

    @FXML private TableView<ResponseRevenueReportDto> tblReport;
    @FXML private TableColumn<ResponseRevenueReportDto,Integer> colReportId;
    @FXML private TableColumn<ResponseRevenueReportDto,Short>   colReportYear;
    @FXML private TableColumn<ResponseRevenueReportDto,Byte>    colReportMonth;
    @FXML private TableColumn<ResponseRevenueReportDto,Double>  colReportTotal;

    @FXML private PieChart    pieChart;
    @FXML private VBox detailContainer;

    @FXML private Button btnExportMonthPdf;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String token = "";

    @FXML
    public void initialize() {

        btnExportMonthPdf.setOnAction(e -> exportDetailPdf());
        // trong initialize()
        for(short y=2020; y<=2030; y++) cbYear.getItems().add(y);
        cbYear.getSelectionModel().select((short)2025);
        cbYear.valueProperty().addListener((o,ov,nv)-> loadReportsByYear(nv));
        loadReportsByYear(cbYear.getValue());

        barChart.setLegendVisible(false);

        btnExportPdf.setOnAction(e -> exportAnnualPdf());

        btnExportExcel.setOnAction(e -> {
            try {
                exportAnnualExcel();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnBuilder.setOnAction(e -> doSth());
        // 2) Thiết lập cột báo cáo
        colReportId   .setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
        colReportYear .setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getYear()));
        colReportMonth.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getMonth()));
        colReportTotal.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalMonthRevenue()));

        // 4) Khi đổi combo chỉ load danh sách hiện có
//        cbYear.valueProperty().addListener((o,ov,nv)-> loadReports());

        // 5) Khi click vào 1 dòng => hiển thị detail
        tblReport.getSelectionModel().selectedItemProperty().addListener((obs,old,sel)-> {
            boolean has = sel != null;
            if(has) showDetail(sel);
        });

        // 6) Lần đầu chỉ load, không tạo
//        loadReports();

        tblReport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void exportDetailPdf() {
        // 1) Lấy mục đang chọn
        ResponseRevenueReportDto sel = tblReport.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Chưa chọn tháng", "Bạn phải chọn 1 dòng trong báo cáo để xuất PDF chi tiết.");
            return;
        }

        // 2) Chọn nơi lưu
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu PDF chi tiết tháng " + sel.getMonth());
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        File pdfFile = chooser.showSaveDialog(tblReport.getScene().getWindow());
        if (pdfFile == null) return;

        try {
            // 3) Snapshot PieChart của detail
            WritableImage pieFx = pieChart.snapshot(new SnapshotParameters(), null);
            File pieImgFile = File.createTempFile("detailPie", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(pieFx, null), "png", pieImgFile);

            // 4) Tạo PDF
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            doc.setMargins(36, 36, 36, 36);

            // Font Unicode
            String fontPath = Paths.get(getClass()
                    .getResource("/com/example/frontendquanlikhachsan/assets/fonts/times.ttf").toURI()).toString();

            PdfFont vnFont = PdfFontFactory.createFont(
                    fontPath,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );

            // Header

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event e) {
                    PdfDocumentEvent ev = (PdfDocumentEvent) e;
                    PdfPage page = ev.getPage();
                    int pageNum = pdf.getPageNumber(page);
                    new com.itextpdf.layout.Canvas(
                            ev.getPage(), ev.getPage().getPageSize()
                    ).showTextAligned(
                            new Paragraph(String.format("Trang %d", pageNum))
                                    .setFont(vnFont).setFontSize(9),
                            ev.getPage().getPageSize().getWidth() / 2,
                            20, TextAlignment.CENTER
                    ).close();
                }
            });

            // Thêm handler vẽ header (copyright) ở đầu mỗi trang
            pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event e) {
                    PdfDocumentEvent ev = (PdfDocumentEvent) e;
                    PdfPage page = ev.getPage();
                    Rectangle pageSize = page.getPageSize();
                    // Tạo Canvas để vẽ
                    new com.itextpdf.layout.Canvas(page, pageSize)
                            .showTextAligned(
                                    // Dùng createStyledParagraph nếu có định dạng bold, nhưng ở đây text thường cũng được
                                    new Paragraph("© 2025 Công ty The Space. All rights reserved.")
                                            .setFont(vnFont)
                                            .setFontSize(9)
                                            .setTextAlignment(TextAlignment.RIGHT),
                                    // Vị trí: cách mép trên 20px, cách mép phải 36 (bằng right margin)
                                    pageSize.getRight() - 36,
                                    pageSize.getTop() - 20,
                                    TextAlignment.RIGHT
                            )
                            .close();
                }
            });

            doc.add(createStyledParagraph("Công Ty The Space", vnFont, 22f).setTextAlignment(TextAlignment.CENTER).setBold().setFontColor(
                    ColorConstants.BLUE
            ));
            doc.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(5)
                    .setMarginBottom(5)
            );
            doc.add(new Paragraph("BÁO CÁO CHI TIẾT THÁNG " + sel.getMonth() + " NĂM " + sel.getYear())
                    .setFont(vnFont).setFontSize(18f).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Tổng doanh thu: " + String.format("%,.0f₫", sel.getTotalMonthRevenue()))
                    .setFont(vnFont).setFontSize(14f).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("*Báo cáo được tạo bởi hệ thống quản lí khách sạn Roomify").setFont(vnFont).setFontSize(10f).setItalic());
            doc.add(new Paragraph("Ngày xuất: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(vnFont).setFontSize(12f).setTextAlignment(TextAlignment.CENTER));

            // Pie
            doc.add(new Paragraph("I. Cơ cấu doanh thu theo loại phòng").setBold().setFont(vnFont));
            Image pieImg = new Image(ImageDataFactory.create(pieImgFile.getAbsolutePath()))
                    .scaleToFit(400, 300).setMarginBottom(10);
            doc.add(pieImg);

            // Bảng chi tiết
            doc.add(new Paragraph("II. Bảng chi tiết doanh thu tháng " + sel.getMonth()).setBold().setFont(vnFont));
            Table tbl = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4}))
                    .useAllAvailableWidth();
            tbl.addHeaderCell(new Cell().add(new Paragraph("Loại phòng").setBold().setFont(vnFont)));
            tbl.addHeaderCell(new Cell().add(new Paragraph("Doanh thu (₫)").setBold().setFont(vnFont)));
            tbl.addHeaderCell(new Cell().add(new Paragraph("Tỷ lệ (%)").setBold().setFont(vnFont)));

            // Lấy detail list
            List<ResponseRevenueReportDetailDto> details = sel.getRevenueReportDetailIds().stream()
                    .map(id -> {
                        try {
                            String j = ApiHttpClientCaller.call(
                                    "revenue-report-detail/" + id, GET, null, token);
                            return mapper.readValue(j, ResponseRevenueReportDetailDto.class);
                        } catch (Exception ex) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            double total = details.stream()
                    .mapToDouble(ResponseRevenueReportDetailDto::getTotalRoomRevenue)
                    .sum();

            // Fill rows
            for (var d : details) {
                tbl.addCell(new Cell().add(new Paragraph(d.getRoomTypeName())));
                tbl.addCell(new Cell().add(new Paragraph(String.format("%,.0f", d.getTotalRoomRevenue()))));
                double pct = total == 0 ? 0 : d.getTotalRoomRevenue() / total * 100;
                tbl.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", pct))));
            }
            // Dòng tổng
            tbl.addCell(new Cell().add(new Paragraph("Tổng").setBold().setFont(vnFont)));
            tbl.addCell(new Cell().add(new Paragraph(String.format("%,.0f₫", total)).setBold().setFont(vnFont)));
            tbl.addCell(new Cell().add(new Paragraph("100%").setBold().setFont(vnFont)));

            doc.add(tbl.setFont(vnFont));

            doc.close();
            showInfo("Thành công", "Đã xuất PDF chi tiết tháng " + sel.getMonth());
        } catch (Exception e) {
            showError("Lỗi khi tạo PDF chi tiết", e.getMessage());
        }
    }


    private String buildCurrentYear(){
        var year = cbYear.getValue();
        Map<String, Number> monthly   = fetchMonthlyRevenue((short)year);
        Map<String, Double>  roomType = fetchRoomTypeRevenue((short)year);
        return buildCurrentYearPrompt(
                (short)year, monthly, roomType
        );
    }

    private String buildYearComparison(){
        var year = cbYear.getValue();
        Pair<Map<String, Number>, Map<String, Number>> cmp =
                fetchComparisonData((short)(year-1), (short)year);
        return buildYearComparisonPrompt(
                (short)(year-1), cmp.getKey(),
                (short)year, cmp.getValue()
        );
    }

    /**
     * Calls the Gemini AI API with a specific prompt and returns the response
     * @param prompt The prompt to send to the AI
     * @return The AI's response as a string
     */
    private String callAI(String prompt) {
        try {
            String apiKey = "AIzaSyBf0xyHSQW2A4Y2Tf6d-0R0GD_8XRz0WcE";
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-1.5-flash:generateContent?key=" + apiKey;

            // 1) Tạo phần text object
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);

            // 2) Đưa vào mảng parts
            JsonArray parts = new JsonArray();
            parts.add(part);

            // 3) Tạo contents element
            JsonObject contentObj = new JsonObject();
            contentObj.add("parts", parts);

            // 4) Đưa contents element vào mảng contents
            JsonArray contents = new JsonArray();
            contents.add(contentObj);

            // 5) Build request body
            JsonObject body = new JsonObject();
            body.add("contents", contents);

            // --- Gửi HTTP POST như trước ---
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            InputStream in = code==200 ? conn.getInputStream() : conn.getErrorStream();
            String resp = new Scanner(in, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            if (code != 200) return "Error: " + code + " " + resp;

            // 6) Parse response
            JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
            JsonArray cands = json.getAsJsonArray("candidates");
            if (cands.size() == 0) return "No response";
            String text = cands.get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
            return text;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling AI: " + e.getMessage();
        }
    }

    /**
     * Tạo Paragraph từ chuỗi Markdown-style **bold**,
     * trong đó đoạn nằm giữa ** sẽ được setBold().
     */
    private Paragraph createStyledParagraph(String markdown, PdfFont font, float fontSize) {
        Paragraph p = new Paragraph().setFont(font).setFontSize(fontSize);
        // regex tìm **...**
        Pattern pattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
        Matcher matcher = pattern.matcher(markdown);

        int lastEnd = 0;
        while (matcher.find()) {
            // text trước dấu **
            if (matcher.start() > lastEnd) {
                String normalText = markdown.substring(lastEnd, matcher.start());
                p.add(new Text(normalText));
            }
            // text in đậm
            String boldText = matcher.group(1);
            p.add(new Text(boldText).setBold());
            lastEnd = matcher.end();
        }
        // còn lại sau dấu ** cuối
        if (lastEnd < markdown.length()) {
            p.add(new Text(markdown.substring(lastEnd)));
        }
        return p;
    }


    private void doSth(){
        // In your existing methods or UI event handlers:
        String currentYearAnalysis = callAI(buildCurrentYear());
        String yearComparisonAnalysis = callAI(buildYearComparison());
        System.out.print(currentYearAnalysis);
        System.out.print(yearComparisonAnalysis);
    }

    /**
     * Lấy doanh thu từng tháng của 1 năm.
     * @param year Năm cần lấy dữ liệu
     * @return Map<Tháng, Doanh thu>
     */
    private Map<String, Number> fetchMonthlyRevenue(short year) {
        Map<String, Number> monthly = new LinkedHashMap<>();
        try {
            String json = ApiHttpClientCaller.call(
                    "revenue-report/revenue-report-by-year/" + year,
                    GET, null, token
            );
            List<ResponseRevenueReportDto> reports = mapper.readValue(
                    json, new TypeReference<List<ResponseRevenueReportDto>>() {}
            );
            reports.sort(Comparator.comparing(ResponseRevenueReportDto::getMonth));
            for (ResponseRevenueReportDto rpt : reports) {
                monthly.put(
                        String.valueOf(rpt.getMonth()),
                        rpt.getTotalMonthRevenue()
                );
            }
        } catch (Exception e) {
            // xử lý lỗi tuỳ ý, hoặc giữ map rỗng
            e.printStackTrace();
        }
        return monthly;
    }

    /**
     * Lấy cơ cấu doanh thu theo loại phòng cho cả năm.
     * @param year Năm cần lấy dữ liệu
     * @return Map<Loại phòng, Tổng doanh thu>
     */
    private Map<String, Double> fetchRoomTypeRevenue(short year) {
        Map<String, Double> byType = new LinkedHashMap<>();
        try {
            // gọi lại list report tháng-năm
            String json = ApiHttpClientCaller.call(
                    "revenue-report/revenue-report-by-year/" + year,
                    GET, null, token
            );
            List<ResponseRevenueReportDto> reports = mapper.readValue(
                    json, new TypeReference<List<ResponseRevenueReportDto>>() {}
            );
            // gom detail
            for (ResponseRevenueReportDto rpt : reports) {
                for (Integer detId : rpt.getRevenueReportDetailIds()) {
                    try {
                        String detJson = ApiHttpClientCaller.call(
                                "revenue-report-detail/" + detId, GET, null, token
                        );
                        ResponseRevenueReportDetailDto d = mapper.readValue(
                                detJson, ResponseRevenueReportDetailDto.class
                        );
                        byType.merge(
                                d.getRoomTypeName(),
                                d.getTotalRoomRevenue(),
                                Double::sum
                        );
                    } catch (Exception ignored) { }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byType;
    }

    /**
     * Lấy 2 map doanh thu tháng của 2 năm để so sánh.
     * @param year1 Năm cũ
     * @param year2 Năm mới
     * @return Pair với key=map năm cũ, value=map năm mới
     */
    private Pair<Map<String, Number>, Map<String, Number>> fetchComparisonData(
            short year1, short year2) {
        Map<String, Number> oldYear  = fetchMonthlyRevenue(year1);
        Map<String, Number> newYear  = fetchMonthlyRevenue(year2);
        return new Pair<>(oldYear, newYear);
    }


    /**
     * Xây dựng prompt phân tích riêng năm hiện tại:
     * A) Phân tích sự tăng giảm doanh thu theo tháng, kết hợp bối cảnh thời gian.
     * B) Phân tích cơ cấu doanh thu theo loại phòng.
     * Cuối cùng: đúc kết đánh giá cho năm.
     */
    private String buildCurrentYearPrompt(short year,
                                          Map<String, Number> monthlyRevenue,
                                          Map<String, Double> roomTypeRevenue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tất cả số liệu giá cả được tính theo đơn vị VIỆT NAM ĐỒNG chứ không phải nghìn đồng hay triệu đồng, ví dụ khi tôi đưa số liệu doanh thu tháng 1 là 1400 thì phải hiểu là 1400 đồng chứ KHÔNG ĐƯỢC PHÉP hiểu thành 1400 nghìn đồng hay 1400 triệu đồng, tỉ lệ thì là thập phân 2 chữ số, không được phép đọc hoặc nhận định sai số liệu. Viết Báo cáo phân tích tài chính khách sạn bằng tiếng Việt, giọng chuyên nghiệp, formal như báo cáo ngành Du lịch – Khách sạn. Output chia theo mục 1.a -> 1.e (Giới thiệu, Phân tích doanh thu theo tháng, Phân tích cơ cấu doanh thu, Nhận định chính, Khuyến nghị), không cần header hay giới thiệu, output chỉ được phép đúng 5 mục như trên . Mỗi mục gồm 2–3 đoạn ngắn, sử dụng câu chủ động, thuật ngữ tài chính (tỷ suất lợi nhuận gộp, biên EBITDA, v.v.), không lặp ý, không dùng đại từ nhân xưng. Số liệu phải trình bày trực tiếp, rõ ràng. Nếu liệt kê dữ liệu hãy liệt kê dạng gạch đầu dòng thay vì dạng bảng.\n")
                .append("Dữ liệu đầu vào:\n")
                .append("- Năm: ").append(year).append("\n");

        boolean noMonth = monthlyRevenue.isEmpty() ||
                monthlyRevenue.values().stream()
                        .mapToDouble(Number::doubleValue)
                        .allMatch(v -> v == 0);
        if (noMonth) {
            sb.append("- Doanh thu từng tháng (Tháng: Giá trị): Trống (không có dữ liệu)\\n");
        } else {
            sb.append("- Doanh thu từng tháng (Tháng: Giá trị):\\n");
            monthlyRevenue.forEach((m, v) ->
                    sb.append("  • Tháng ").append(m).append(": ")
                            .append(String.format("%,.0f₫", v)).append("\n")
            );
        }

        boolean noRoom = roomTypeRevenue.isEmpty() ||
                roomTypeRevenue.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .allMatch(v -> v == 0);
        if (noRoom) {
            sb.append("- Cơ cấu doanh thu theo loại phòng: Trống (không có dữ liệu)\\n");
        } else {
            sb.append("- Cơ cấu doanh thu theo loại phòng:\\n");
            roomTypeRevenue.forEach((type, rev) ->
                    sb.append("  • ").append(type).append(": ")
                            .append(String.format("%,.0f₫", rev)).append("\n")
            );
        }


        sb.append("\nYêu cầu:\n")
                .append("1. Phân tích sự tăng giảm doanh thu của các tháng trong năm ")
                .append(year)
                .append(", đánh giá mức độ và liên hệ với bối cảnh thời gian (mùa cao điểm, lễ tết, mùa thấp điểm).\n")
                .append("2. Phân tích cơ cấu doanh thu theo loại phòng, nhận xét về tỷ trọng và xu hướng đóng góp.\n")
                .append("3. Đúc kết các nhận định chính cho năm ").append(year)
                .append(", nêu rõ điểm mạnh, điểm cần cải thiện.\n")
                .append("Văn phong khách quan, cấu trúc rõ ràng, formal, phù hợp báo cáo chuyên nghiệp.");
        return sb.toString();
    }

    /**
     * Xây dựng prompt so sánh doanh thu giữa năm hiện tại và năm trước:
     * - So sánh tổng doanh thu, tỷ lệ tăng/giảm.
     * - Phân tích chi tiết theo tháng.
     * - Đề xuất điểm cần lưu ý.
     */
    private String buildYearComparisonPrompt(short lastYear,
                                             Map<String, Number> lastYearMonthly,
                                             short currentYear,
                                             Map<String, Number> currentYearMonthly) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tất cả số liệu giá cả được tính theo đơn vị VIỆT NAM ĐỒNG chứ không phải nghìn đồng hay triệu đồng, ví dụ khi tôi đưa số liệu doanh thu tháng 1 là 1400 thì phải hiểu là 1400 đồng chứ KHÔNG ĐƯỢC PHÉP hiểu thành 1400 nghìn đồng hay 1400 triệu đồng, tỉ lệ thì là thập phân 2 chữ số, không được phép đọc hoặc nhận định sai số liệu. So sánh phân tích tài chính khách sạn giữa hai năm bằng tiếng Việt, giọng chuyên nghiệp, formal như báo cáo ngành Du lịch – Khách sạn. Nội dung chia theo các mục (4 mục) 2.a -> 2.d, không cần header hay giới thiệu, output chỉ được phép đúng 4 mục như trên. Phân tích tổng quan doanh thu hai năm (so sánh tổng doanh thu, % tăng/giảm), \n" +
                        "Phân tích cơ cấu doanh thu theo loại phòng (so sánh tỷ trọng từng loại), Nhận định và khuyến nghị (Nhận định, Khuyến nghị), Kết luận. \n" +
                        "Dùng câu chủ động, thuật ngữ tài chính (tỷ suất lợi nhuận gộp, biên EBITDA, ROA…), không lặp ý, không dùng đại từ nhân xưng, số liệu trình bày trực tiếp, rõ ràng.\n" + "OUTPUT RULE: Nếu liệt kê dữ liệu hãy liệt kê dạng gạch đầu dòng, không được vẽ dạng bảng.\n")
                .append("So sánh dữ liệu doanh thu giữa hai năm:\n");
        // Sau sb.append("So sánh dữ liệu doanh thu giữa hai năm:\n");
        boolean noLast = lastYearMonthly.isEmpty() ||
                lastYearMonthly.values().stream()
                        .mapToDouble(Number::doubleValue)
                        .allMatch(v -> v == 0);
        if (noLast) {
            sb.append("- Năm ").append(lastYear).append(": Trống (không có dữ liệu)\\n");
        } else {
            sb.append("- Năm ").append(lastYear).append(": tổng doanh thu và chi tiết: \\n");
            lastYearMonthly.forEach((m, v) ->
                    sb.append("  • Tháng ").append(m).append(": ")
                            .append(String.format("%,.0f₫", v)).append("\n")
            );
        }
        boolean noCurrent = currentYearMonthly.isEmpty() ||
                currentYearMonthly.values().stream()
                        .mapToDouble(Number::doubleValue)
                        .allMatch(v -> v == 0);
        if (noCurrent) {
            sb.append("- Năm ").append(currentYear).append(": Trống (không có dữ liệu)\n");
        } else {
            sb.append("- Năm ").append(currentYear).append(": tổng doanh thu và chi tiết theo tháng:\n");
            currentYearMonthly.forEach((m, v) ->
                    sb.append("  • Tháng ").append(m).append(": ")
                            .append(String.format("%,.0f₫", v)).append("\n")
            );
        }

        sb.append("\nYêu cầu:\n")
                .append("1. Tính tổng doanh thu từng năm, so sánh sự tăng/giảm tuyệt đối và phần trăm giữa ")
                .append(lastYear).append(" và ").append(currentYear).append(".\n")
                .append("2. Phân tích chi tiết biến động theo tháng: những tháng tăng mạnh, tháng giảm sâu và ")
                .append("liên hệ nguyên nhân (mùa vụ, chiến dịch marketing, …).\n")
                .append("3. Đúc kết điểm khác biệt chính, nêu rõ cơ hội và rủi ro, đề xuất hướng cải thiện.\n")
                .append("Văn phong khách quan, logic, formal, phù hợp chuẩn báo cáo tài chính.");
        return sb.toString();
    }


    private void exportAnnualPdf() {
        Short year = cbYear.getValue();
        if (year == null) {
            showError("Chưa chọn năm", "Bạn phải chọn năm để xuất PDF.");
            return;
        }

        // Lấy nội dung phân tích từ AI
        String currentYearAnalysis = callAI(buildCurrentYear());
        String yearComparisonAnalysis = callAI(buildYearComparison());

        // 1. Chọn nơi lưu file
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu Báo Cáo PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        File pdfFile = chooser.showSaveDialog(cbYear.getScene().getWindow());
        if (pdfFile == null) return;

        try {
            // 2. Snapshot JavaFX charts ra ảnh tạm
            File barImgFile = File.createTempFile("barChart", ".png");
            WritableImage barFx = barChart.snapshot(new SnapshotParameters(), null);
            ImageIO.write(SwingFXUtils.fromFXImage(barFx, null), "png", barImgFile);

            File pieImgFile = File.createTempFile("pieChart", ".png");
            WritableImage pieFx = annualPieChart.snapshot(new SnapshotParameters(), null);
            ImageIO.write(SwingFXUtils.fromFXImage(pieFx, null), "png", pieImgFile);

            // 3. Khởi tạo PDF + font Unicode
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            doc.setMargins(50, 36, 50, 36);

            String fontPath = Paths.get(getClass()
                    .getResource("/com/example/frontendquanlikhachsan/assets/fonts/times.ttf").toURI()).toString();

            PdfFont vnFont = PdfFontFactory.createFont(
                    fontPath,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
            doc.setFont(vnFont);

            // 4. Thêm handler đánh số trang ở footer
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event e) {
                    PdfDocumentEvent ev = (PdfDocumentEvent) e;
                    PdfPage page = ev.getPage();
                    int pageNum = pdf.getPageNumber(page);
                    new com.itextpdf.layout.Canvas(
                            ev.getPage(), ev.getPage().getPageSize()
                    ).showTextAligned(
                            new Paragraph(String.format("Trang %d", pageNum))
                                    .setFont(vnFont).setFontSize(9),
                            ev.getPage().getPageSize().getWidth() / 2,
                            20, TextAlignment.CENTER
                    ).close();
                }
            });

            // Thêm handler vẽ header (copyright) ở đầu mỗi trang
            pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event e) {
                    PdfDocumentEvent ev = (PdfDocumentEvent) e;
                    PdfPage page = ev.getPage();
                    Rectangle pageSize = page.getPageSize();
                    // Tạo Canvas để vẽ
                    new com.itextpdf.layout.Canvas(page, pageSize)
                            .showTextAligned(
                                    // Dùng createStyledParagraph nếu có định dạng bold, nhưng ở đây text thường cũng được
                                    new Paragraph("© 2025 Công ty The Space. All rights reserved.")
                                            .setFont(vnFont)
                                            .setFontSize(9)
                                            .setTextAlignment(TextAlignment.RIGHT),
                                    // Vị trí: cách mép trên 20px, cách mép phải 36 (bằng right margin)
                                    pageSize.getRight() - 36,
                                    pageSize.getTop() - 20,
                                    TextAlignment.RIGHT
                            )
                            .close();
                }
            });


            try {
                String logoPath = getClass().getResource("/com/example/frontendquanlikhachsan/assets/images/logo-image.png").toURI().toString();
                Image logoImage = new Image(ImageDataFactory.create(logoPath))
                        .setWidth(250)  // Adjust width as needed
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                        .setMarginBottom(20);
                doc.add(logoImage);
            } catch (Exception e) {
                // Continue without logo if there's an issue
                System.err.println("Could not add logo: " + e.getMessage());
            }

            doc.add(createStyledParagraph("Công Ty The Space", vnFont, 22f).setTextAlignment(TextAlignment.CENTER).setBold().setFontColor(
                            ColorConstants.BLUE
                    ));
            doc.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(5)
                    .setMarginBottom(5)
            );
            doc.add(createStyledParagraph("BÁO CÁO DOANH THU NĂM " + year, vnFont, 18f)
                    .setTextAlignment(TextAlignment.CENTER).setBold());
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            doc.add(new Paragraph("Ngày lập báo cáo: " + today)
                    .setFont(vnFont).setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("*Được lập tự động bởi hệ thống quản lý khách sạn Roomify")
                    .setFont(vnFont).setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());

            doc.add(new AreaBreak()); // ngắt trang

            // A: Label đầu section A
            doc.add(createStyledParagraph("I. Biểu đồ và số liệu", vnFont, 14f).setBold());

            // A1: Biểu đồ BarChart
            doc.add(createStyledParagraph("1. Tổng quan doanh thu các tháng trong năm", vnFont, 12f).setMarginTop(5).setBold().setMarginLeft(20));
            Image barImg = new Image(ImageDataFactory.create(barImgFile.getAbsolutePath()))
                    .scaleToFit(500, 300).setAutoScale(true).setMarginTop(10).setMarginBottom(10);
            doc.add(barImg);

            // A2: Biểu đồ PieChart
            doc.add(createStyledParagraph("2. Cơ cấu doanh thu năm theo loại phòng", vnFont, 12f).setMarginTop(5).setBold().setMarginLeft(20));
            Image pieImg = new Image(ImageDataFactory.create(pieImgFile.getAbsolutePath()))
                    .scaleToFit(400, 400).setAutoScale(true).setMarginTop(10).setMarginBottom(10);
            doc.add(pieImg);

            // A3: Bảng chi tiết doanh thu
            doc.add(createStyledParagraph("3. Bảng chi tiết doanh thu", vnFont, 12f).setMarginTop(5).setBold().setMarginLeft(20));
            Map<String, Number> data = new LinkedHashMap<>();
            barChart.getData().get(0).getData()
                    .forEach(d -> data.put(d.getXValue(), d.getYValue()));

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2}))
                    .useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Tháng").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Doanh thu (₫)").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Tỉ lệ").setBold()));

            double total = data.values().stream()
                    .mapToDouble(Number::doubleValue).sum();
            data.forEach((m, rev) -> {
                table.addCell(new Cell().add(new Paragraph(m)));
                table.addCell(new Cell().add(new Paragraph(String.format("%,.0f", rev))));
                double pct = total == 0 ? 0 : rev.doubleValue() / total * 100;
                table.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", pct))));
            });
            // Dòng tổng
            table.addCell(new Cell(1, 1).add(new Paragraph("Tổng").setBold()));
            table.addCell(new Cell(1, 1).add(new Paragraph(String.format("%,.0f₫", total)).setBold()));
            table.addCell(new Cell(1, 1).add(new Paragraph("100%").setBold()));
            doc.add(table);

            // B: Label đầu section B
            doc.add(createStyledParagraph("II. Phân tích và đánh giá", vnFont, 14f).setBold().setMarginTop(10));

            // B1: Phân tích số liệu năm
            doc.add(createStyledParagraph("1. Phân tích số liệu", vnFont, 12f).setMarginTop(5).setBold().setMarginLeft(20));
            doc.add(createStyledParagraph(currentYearAnalysis, vnFont, 12f).setMarginLeft(20));

            // B2: So sánh tương quan năm trước
            doc.add(createStyledParagraph("2. So sánh tương quan", vnFont, 12f).setMarginTop(5).setBold().setMarginLeft(20));
            doc.add(createStyledParagraph(yearComparisonAnalysis, vnFont, 12f).setMarginLeft(20));

            // 10. Đóng tài liệu
            doc.close();

            Platform.runLater(() ->
                    showInfo("Thành công", "Đã xuất PDF: " + pdfFile.getName())
            );
        } catch (Exception ex) {
            Platform.runLater(() ->
                    showError("Lỗi khi tạo PDF", ex.getMessage())
            );
        }
    }

    public void exportAnnualExcel() throws IOException {
        XSSFColor labelBgColor = new XSSFColor(
                new java.awt.Color(0x15, 0x3D, 0x64),
                new DefaultIndexedColorMap()
        );
//  Data rows: #a6c9ec
        XSSFColor dataBgColor = new XSSFColor(
                new java.awt.Color(0xA6, 0xC9, 0xEC),
                new DefaultIndexedColorMap()
        );


        Short year = cbYear.getValue();
        if (year == null) return;

        // 1. Lấy dữ liệu
        Map<String, Number> monthly   = fetchMonthlyRevenue(year);
        Map<String, Double> roomType  = fetchRoomTypeRevenue(year);
        for (int m = 1; m <= 12; m++) {
            String key = String.valueOf(m);
            monthly.putIfAbsent(key, 0);
        }

        // 2. Tạo Workbook và Styles chung
        XSSFWorkbook wb = new XSSFWorkbook();
        CreationHelper helper = wb.getCreationHelper();

        // Header style (thêm border)
        XSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short)12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(org. apache. poi. ss. usermodel. HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Money style + border
        XSSFCellStyle moneyStyle = wb.createCellStyle();
        moneyStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0"));
        moneyStyle.setBorderTop(BorderStyle.THIN);
        moneyStyle.setBorderBottom(BorderStyle.THIN);
        moneyStyle.setBorderLeft(BorderStyle.THIN);
        moneyStyle.setBorderRight(BorderStyle.THIN);

        // Percent style (2 chữ số thập phân) + border
        XSSFCellStyle percentStyle = wb.createCellStyle();
        percentStyle.setDataFormat(helper.createDataFormat().getFormat("0.00%"));
        percentStyle.setBorderTop(BorderStyle.THIN);
        percentStyle.setBorderBottom(BorderStyle.THIN);
        percentStyle.setBorderLeft(BorderStyle.THIN);
        percentStyle.setBorderRight(BorderStyle.THIN);
/// //////
        XSSFCellStyle table1HeaderStyle = wb.createCellStyle();
        table1HeaderStyle.cloneStyleFrom(headerStyle);
        table1HeaderStyle.setFillForegroundColor(labelBgColor);
        table1HeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// Data row background: Dark Blue, Text2 Lighter 75% (approx bằng LIGHT_BLUE)
        XSSFCellStyle table1DataBgStyle = wb.createCellStyle();
        table1DataBgStyle.cloneStyleFrom(moneyStyle);
        table1DataBgStyle.setFillForegroundColor(dataBgColor);
        table1DataBgStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle monthStyle = wb.createCellStyle();
        monthStyle.cloneStyleFrom(table1DataBgStyle);
        monthStyle.setAlignment(org. apache. poi. ss. usermodel.HorizontalAlignment.LEFT);
// Fonts cho các cột
        XSSFFont revFont = wb.createFont();
        revFont.setBold(true);
        revFont.setColor(IndexedColors.YELLOW.getIndex());

        XSSFFont pctPosFont = wb.createFont();
        pctPosFont.setBold(true);
        pctPosFont.setColor(IndexedColors.DARK_GREEN.getIndex());

        XSSFFont pctNegFont = wb.createFont();
        pctNegFont.setBold(true);
        pctNegFont.setColor(IndexedColors.RED.getIndex());

/// /////////
        // Styles riêng cho header sheet Tổng kết
        XSSFCellStyle headlineStyle = wb.createCellStyle();
        XSSFFont headlineFont = wb.createFont();
        headlineFont.setBold(true);
        headlineFont.setFontHeightInPoints((short)20);
        headlineFont.setColor(IndexedColors.BLUE.getIndex());
        headlineStyle.setFont(headlineFont);
        headlineStyle.setAlignment(org. apache. poi. ss. usermodel. HorizontalAlignment.CENTER);

        XSSFCellStyle dateStyle = wb.createCellStyle();
        XSSFFont dateFont = wb.createFont();
        dateFont.setItalic(true);
        dateFont.setFontHeightInPoints((short)12);
        dateStyle.setFont(dateFont);
        dateStyle.setAlignment(
                org. apache. poi. ss. usermodel.HorizontalAlignment.CENTER);

        XSSFCellStyle sumStyle = wb.createCellStyle();
        XSSFFont sumFont = wb.createFont();
        sumFont.setBold(true);
        sumFont.setFontHeightInPoints((short)14);
        sumFont.setColor(IndexedColors.RED.getIndex());
        sumStyle.setFont(sumFont);
        sumStyle.setAlignment(org. apache. poi. ss. usermodel. HorizontalAlignment.LEFT);
        sumStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        sumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);



        XSSFColor tbl2HdrColor = new XSSFColor(
                new java.awt.Color(0xE9, 0x71, 0x32),
                new DefaultIndexedColorMap()
        );
        XSSFCellStyle tbl2HdrStyle = wb.createCellStyle();
        tbl2HdrStyle.cloneStyleFrom(headerStyle);
        tbl2HdrStyle.setFillForegroundColor(tbl2HdrColor);
        tbl2HdrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFColor tbl2DataColor = new XSSFColor(
                new java.awt.Color(0xF7, 0xC7, 0xAC),
                new DefaultIndexedColorMap()
        );
        XSSFCellStyle tbl2DataBgStyle = wb.createCellStyle();
        tbl2DataBgStyle.cloneStyleFrom(moneyStyle);
        tbl2DataBgStyle.setFillForegroundColor(tbl2DataColor);
        tbl2DataBgStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

// Font cho cột Cơ cấu (hex #394B77, bold)
        XSSFColor structColor = new XSSFColor(
                new java.awt.Color(0x39, 0x4B, 0x77),
                new DefaultIndexedColorMap()
        );
        XSSFFont structFont = wb.createFont();
        structFont.setBold(true);
        structFont.setColor(structColor);


        // 3. Sheet “Tổng kết” duy nhất
        XSSFSheet sheet = wb.createSheet("Tổng kết");
        sheet.setDefaultColumnWidth(20);
        // Ví dụ sheet là sumSheet


// 1) Auto-size để có số cơ bản
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);

// 2) Sau đó nới rộng thêm 5 ký tự nữa
        int extra = 5 * 256;
        sheet.setColumnWidth(2, sheet.getColumnWidth(2) + extra);
        sheet.setColumnWidth(3, sheet.getColumnWidth(3) + extra);



        // 3.1 Tiêu đề + mở rộng row height để khỏi che chữ
        Row r0 = sheet.createRow(0);
        r0.setHeightInPoints(30f);

        org. apache. poi. ss. usermodel.Cell c0 = r0.createCell(1); // cột B = index 1
        c0.setCellValue("BÁO CÁO TÓM TẮT DOANH THU NĂM " + year);
        c0.setCellStyle(headlineStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 3));

        // 3.2 Ngày lập
        Row r1 = sheet.createRow(1);

        org. apache. poi. ss. usermodel.Cell c1 = r1.createCell(1);
        c1.setCellValue("Ngày lập báo cáo: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        c1.setCellStyle(dateStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));

        // 3.3 Tổng doanh thu
        double totalCur = monthly.values().stream()
                .mapToDouble(Number::doubleValue)
                .sum();
        Row r3 = sheet.createRow(3);

        org. apache. poi. ss. usermodel.Cell c3 = r3.createCell(1);
        c3.setCellValue("Tổng doanh thu: " + String.format("%,.0f₫", totalCur));
        c3.setCellStyle(sumStyle);
        for (int col = 1; col <= 3; col++) sheet.autoSizeColumn(col);

        // 4. Bảng “Theo tháng” từ dòng 5, cột B–D
        int rowIdx = 5;

// 4.1 Định nghĩa lại style ngay trước khi vẽ bảng

// 4.2 Header bảng tháng
        Row thHdr = sheet.createRow(rowIdx++);
        String[] headers = {"Tháng", "Doanh thu", "Tỉ lệ tăng trưởng tháng (%)"};
        for (int i = 0; i < headers.length; i++) {
            org. apache. poi. ss. usermodel.Cell h = thHdr.createCell(1 + i);
            h.setCellValue(headers[i]);
            h.setCellStyle(table1HeaderStyle);
        }

// 4.3 Dữ liệu và % tăng
        double prevVal = 0;
        for (int m = 1; m <= 12; m++) {
            double val = monthly.getOrDefault(String.valueOf(m), 0).doubleValue();
            double pct = prevVal > 0 ? (val - prevVal) / prevVal : 0;

            Row r = sheet.createRow(rowIdx++);
            // Tháng (left-align)
            org. apache. poi. ss. usermodel.Cell cMonth = r.createCell(1);
            cMonth.setCellValue(m);
            cMonth.setCellStyle(monthStyle);

            // Doanh thu (vàng, bold)
            org. apache. poi. ss. usermodel.Cell cRev = r.createCell(2);
            XSSFCellStyle revStyle = wb.createCellStyle();
            revStyle.cloneStyleFrom(table1DataBgStyle);
            revStyle.setFont(revFont);
            cRev.setCellValue(val);
            cRev.setCellStyle(revStyle);

            // Tỉ lệ (green nếu >0, red nếu ≤0)
            org. apache. poi. ss. usermodel.Cell cPct = r.createCell(3);
            XSSFCellStyle pctStyleCell = wb.createCellStyle();
            pctStyleCell.cloneStyleFrom(table1DataBgStyle);
            pctStyleCell.setDataFormat(percentStyle.getDataFormat());
            pctStyleCell.setFont(pct > 0 ? pctPosFont : pctNegFont);
            cPct.setCellValue(pct);
            cPct.setCellStyle(pctStyleCell);

            prevVal = val;
        }

// 4.4 Auto-size lại cột B–D
        for (int col = 1; col <= 3; col++) sheet.autoSizeColumn(col);


        // 5. Bar+Line chart
        XSSFDrawing draw = sheet.createDrawingPatriarch();
        XSSFClientAnchor anc1 = draw.createAnchor(
                0,0,0,0,
                5, 5,
                11, 18
        );
        anc1.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        XSSFChart chart1 = draw.createChart(anc1);
        chart1.setTitleText("Doanh thu và tỉ lệ tăng trưởng theo tháng");
        chart1.getOrAddLegend().setPosition(LegendPosition.TOP_RIGHT);
        // trục
        XDDFCategoryAxis ca = chart1.createCategoryAxis(AxisPosition.BOTTOM);
        ca.setTitle("Tháng");
        XDDFValueAxis vaL = chart1.createValueAxis(AxisPosition.LEFT);
        vaL.setTitle("Doanh thu");
        XDDFValueAxis vaR = chart1.createValueAxis(AxisPosition.RIGHT);
        vaR.setTitle("% Tăng");
        vaR.setCrosses(AxisCrosses.MAX);
        // data
        int startRow = 6, endRow = 6+12-1;
        XDDFDataSource<String> cats = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(startRow, endRow, 1, 1));
        XDDFNumericalDataSource<Double> vals = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(startRow, endRow, 2, 2));
        XDDFNumericalDataSource<Double> pcts = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(startRow, endRow, 3, 3));
        XDDFChartData bar = chart1.createData(ChartTypes.BAR, ca, vaL);
        ((XDDFBarChartData)bar).setBarDirection(BarDirection.COL);
        bar.addSeries(cats, vals).setTitle("Doanh thu", null);
        chart1.plot(bar);
        XDDFChartData line = chart1.createData(ChartTypes.LINE, ca, vaR);
        line.addSeries(cats, pcts).setTitle("% Tăng", null);
        chart1.plot(line);

        // 6. Bảng “Cơ cấu phòng” và pie chart
        int roomStart = 20;
        Row phHdr = sheet.createRow(roomStart++);
        phHdr.createCell(1).setCellValue("Loại phòng");
        phHdr.createCell(2).setCellValue("Doanh thu");
        phHdr.createCell(3).setCellValue("Cơ cấu (%)");
        phHdr.getCell(1).setCellStyle(tbl2HdrStyle);
        phHdr.getCell(2).setCellStyle(tbl2HdrStyle);
        phHdr.getCell(3).setCellStyle(tbl2HdrStyle);

        double totalRoom = roomType.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        for (Map.Entry<String, Double> e : roomType.entrySet()) {
            Row r = sheet.createRow(roomStart++);

            // Loại phòng (nền data, default monthStyle)

            org. apache. poi. ss. usermodel.Cell cA = r.createCell(1);
            cA.setCellValue(e.getKey());
            cA.setCellStyle(tbl2DataBgStyle);

            // Doanh thu (nền data + font vàng)

            org. apache. poi. ss. usermodel.Cell cB = r.createCell(2);
            XSSFCellStyle revStyle2 = wb.createCellStyle();
            revStyle2.cloneStyleFrom(tbl2DataBgStyle);
            revStyle2.setFont(revFont);
            cB.setCellValue(e.getValue());
            cB.setCellStyle(revStyle2);

            // Cơ cấu (%) (nền data + font #394B77, bold)
            double pct = totalRoom == 0 ? 0 : e.getValue() / totalRoom;

            org. apache. poi. ss. usermodel.Cell cC = r.createCell(3);
            XSSFCellStyle structStyle = wb.createCellStyle();
            structStyle.cloneStyleFrom(tbl2DataBgStyle);
            structStyle.setDataFormat(percentStyle.getDataFormat());
            structStyle.setFont(structFont);
            cC.setCellValue(pct);
            cC.setCellStyle(structStyle);
        }

// Cuối cùng auto-size cột B–D
        for (int col = 1; col <= 3; col++) sheet.autoSizeColumn(col);

        // pie chart
        XSSFClientAnchor anc2 = draw.createAnchor(
                0,0,0,0,
                5, 20,
                11, 30
        );
        anc2.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        XSSFChart chart2 = draw.createChart(anc2);
        chart2.setTitleText("Cơ cấu doanh thu theo loại phòng \n\n");
        chart2.getOrAddLegend().setPosition(LegendPosition.RIGHT);
        XDDFDataSource<String> pts = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(21, roomStart-1, 1, 1));
        XDDFNumericalDataSource<Double> pvs = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(21, roomStart-1, 2, 2));
        XDDFPieChartData pieData = (XDDFPieChartData)chart2.createData(ChartTypes.PIE, null, null);
        pieData.addSeries(pts, pvs);
        chart2.plot(pieData);

        // 7. Lưu & thông báo
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu Báo Cáo Excel");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        chooser.setInitialFileName("report_" + year + ".xlsx");
        File excelFile = chooser.showSaveDialog(cbYear.getScene().getWindow());
        if (excelFile == null) return;

        try (FileOutputStream out = new FileOutputStream(excelFile)) {
            wb.write(out);
        }
        wb.close();
        showInfo("Thành công", "Đã xuất Excel: " + excelFile.getName());
    }

    private void loadReportsByYear(Short year) {
        if (year == null) return;
        new Thread(() -> {
            try {
                // 1) gọi API lấy tất cả báo cáo trong năm
                String json = ApiHttpClientCaller.call(
                        "revenue-report/revenue-report-by-year/" + year, GET, null, token
                );
                List<ResponseRevenueReportDto> reports =
                        mapper.readValue(json, new TypeReference<>() {});

                // 2) chuẩn bị dữ liệu cho BarChart
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                Map<String, Double> sumByRoomType = new HashMap<>();

                for (ResponseRevenueReportDto rpt : reports) {
                    series.getData().add(
                            new XYChart.Data<>(
                                    String.valueOf(rpt.getMonth()),
                                    rpt.getTotalMonthRevenue()
                            )
                    );
                    // gom doanh thu theo loại phòng cho PieChart cả năm
                    for (Integer detId : rpt.getRevenueReportDetailIds()) {
                        try {
                            String detJson = ApiHttpClientCaller.call(
                                    "revenue-report-detail/" + detId, GET, null, token
                            );
                            ResponseRevenueReportDetailDto d = mapper.readValue(
                                    detJson, ResponseRevenueReportDetailDto.class
                            );
                            sumByRoomType.merge(
                                    d.getRoomTypeName(),
                                    d.getTotalRoomRevenue(),
                                    Double::sum
                            );
                        } catch (Exception ignored) { }
                    }
                }

                // 3) update UI
                Platform.runLater(() -> {
                    // a) Table
                    tblReport.setItems(FXCollections.observableArrayList(reports));
                    if (!reports.isEmpty()) {
                        tblReport.getSelectionModel().selectFirst();
                    }
                    barChart.setAnimated(false);
                    // b) BarChart
                    barChart.getData().setAll(series);

                    Platform.runLater(() -> {
                        barChart.applyCss();      // re-apply CSS
                        barChart.layout();        // ép layout ngay
                    });

                    // c) PieChart cả năm
                    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                    sumByRoomType.forEach((type, rev) ->
                            pieData.add(new PieChart.Data(type, rev))
                    );

                    // tính tổng doanh thu để tính %
                    double totalRev = sumByRoomType.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .sum();

                    // đổi name thành "Loại\nGiá₫\n(%)" để PieChart tự show label
                    for (PieChart.Data data : pieData) {
                        double value = data.getPieValue();
                        double pct   = totalRev == 0 ? 0 : value / totalRev * 100;
                        String label = String.format(
                                "%s\n%,.0f₫\n(%.1f%%)",
                                data.getName(), value, pct
                        );
                        data.setName(label);
                    }

                    annualPieChart.setData(pieData);
                    annualPieChart.setLabelsVisible(true);
                    annualPieChart.setLegendVisible(true);
                    annualPieChart.setLegendSide(Side.BOTTOM);
                    lblAnnualTotal.setText(String.format("Tổng: %,.0f₫", totalRev));
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError(
                        "Lỗi tải báo cáo năm " + year,
                        e.getMessage()
                ));
            }
        }).start();
    }


//    private void loadReports() {
//        new Thread(() -> {
//            try {
//                String json = ApiHttpClientCaller.call("revenue-report", GET, null, token);
//                List<ResponseRevenueReportDto> all =
//                        mapper.readValue(json, new TypeReference<List<ResponseRevenueReportDto>>(){});
//                Short year  = cbYear.getValue();
//                Byte  month = cbMonth.getValue();
//                List<ResponseRevenueReportDto> filtered = all.stream()
//                        .filter(r -> r.getYear().equals(year) && r.getMonth().equals(month))
//                        .toList();
//                Platform.runLater(() -> {
//                    tblReport.setItems(FXCollections.observableArrayList(filtered));
//                    if (!filtered.isEmpty()) tblReport.getSelectionModel().selectFirst();
//                });
//            } catch(Exception e) {
//                Platform.runLater(() -> showError("Lỗi tải báo cáo", e.getMessage()));
//            }
//        }).start();
//    }

//    private void generateAndReload() {
//        Short year  = cbYear.getValue();
//        if (year==null) return;
//
//        // 1) Gọi API tạo/generate báo cáo tháng-năm
//        Platform.runLater(() -> {
//            try {
//                RevenueReportDto req = new RevenueReportDto();
//                req.setYear(year);
//                ApiHttpClientCaller.call("revenue-report", POST, req, token);
//            } catch(Exception ignored) {}
//            // 2) Load danh sách báo cáo
//            loadReports();
//        });
//    }

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
        Label h4 = new Label("");
        h1.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        h2.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        h3.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        h4.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        grid.add(h1, 0, 0);
        grid.add(h2, 1, 0);
        grid.add(h3, 2, 0);
        grid.add(h4, 3, 0);

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

            Region bar = new Region();
            bar.setPrefHeight(16);
            double maxW = 300;                        // chiều dài tối đa
            bar.setPrefWidth(maxW * pct / 100.0);
            bar.setStyle(
                    "-fx-background-color: linear-gradient(to right, #6baed6, #3182bd);"
                            + "-fx-border-color: #ccc; -fx-border-width:1;"
            );
            HBox barCell = new HBox(bar);
            barCell.setPadding(new Insets(0, 0, 0, 4));

            grid.add(c1, 0, row);
            grid.add(c2, 1, row);
            grid.add(c3, 2, row);
            grid.add(barCell, 3, row);
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
