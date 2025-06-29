package com.example.frontendquanlikhachsan.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ChatbotController {
    @FXML private VBox chatListView;
    @FXML private TextArea inputArea;
    @FXML private ScrollPane scrollPane;

    private final String systemPrompt = """
        Bạn là chatbot hỗ trợ phần mềm quản lý khách sạn, tạo bởi Nhóm 5 Java. 
        Chỉ dùng data được cung cấp để trả lời. 
        Trả lời tuân theo những chỉ dẫn bắt buộc sau:
        - Nếu không tìm thấy thông tin liên quan, trả lời: 'Không có dữ liệu phù hợp.'
        - Một phòng được coi là trống khi:
            + Trạng thái là READY_TO_SERVE
            + Và phiếu thuê của phòng tương ứng gần nhất đã được thanh toán (tức là ngày thanh toán khác null).
        - Trạng thái phòng không được suy luận từ bất kì thứ gì khác, tuân theo dữ liệu được đưa.
        - Một phòng có thể có nhiều phiếu thuê, cứ mỗi lần có khách thuê thì có phiếu thuê, tương tự phiếu đặt.  
        - Nếu một thực thể có tên, ưu tiên sử dụng tên để trả lời thay vì ID.
        - Phải quét và xử lý toàn bộ danh sách khi được hỏi, không được bỏ sót.
        """;
    private final String apiKey = "AIzaSyBri6h5g70u-OiAVNjXMFrbsXnlVAFwMgo";
    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Map<String, String> knowledgeSections = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        addChatBubble("Chatbot sẵn sàng trả lời câu hỏi!", false);
        loadKnowledgeSections();
    }

    @FXML
    public void handleSend() {
        String question = inputArea.getText().trim();
        if (question.isEmpty()) return;

        addChatBubble(question, true);
        inputArea.clear();

        new Thread(() -> {
            String response = callAI(question);
            Platform.runLater(() -> addChatBubble(response, false));
        }).start();
    }

    private void addChatBubble(String message, boolean isUser) {
        HBox bubble = new HBox();
        bubble.setPadding(new Insets(5));
        bubble.setSpacing(10);
        bubble.setMaxWidth(Double.MAX_VALUE);
        bubble.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label msgLabel = new Label((isUser ? "🧑‍💼 " : "🤖 ") + message);
        msgLabel.setWrapText(true);
        msgLabel.setStyle(isUser
                ? "-fx-background-color: #9ACEF5; -fx-padding: 10; -fx-background-radius: 12; -fx-font-size: 13px;"
                : "-fx-background-color: #96D8CA; -fx-padding: 10; -fx-background-radius: 12; -fx-font-size: 13px;");

        bubble.getChildren().add(msgLabel);
        chatListView.getChildren().add(bubble);
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private void loadKnowledgeSections() {
        try {
            knowledgeSections.put("accounts", KnowledgeSectionBuilder.buildAccounts(mapper));
            knowledgeSections.put("blocks", KnowledgeSectionBuilder.buildBlocks(mapper));
            knowledgeSections.put("booking", KnowledgeSectionBuilder.buildBookingForms(mapper));
            knowledgeSections.put("floors", KnowledgeSectionBuilder.buildFloors(mapper));
            knowledgeSections.put("guests", KnowledgeSectionBuilder.buildGuests(mapper));
            knowledgeSections.put("invoices", KnowledgeSectionBuilder.buildInvoices(mapper));
            knowledgeSections.put("rooms", KnowledgeSectionBuilder.buildRooms(mapper));
            knowledgeSections.put("roomtypes", KnowledgeSectionBuilder.buildRoomTypes(mapper));
            knowledgeSections.put("staff", KnowledgeSectionBuilder.buildStaff(mapper));
            knowledgeSections.put("revenue", KnowledgeSectionBuilder.buildRevenueReports(mapper));
            knowledgeSections.put("rentalForms", KnowledgeSectionBuilder.buildRentalForms(mapper));
            knowledgeSections.put("rentalExtensions", KnowledgeSectionBuilder.buildRentalExtensionForms(mapper));
            knowledgeSections.put("invoiceDetails", KnowledgeSectionBuilder.buildInvoiceDetails(mapper));
            knowledgeSections.put("revenueDetails", KnowledgeSectionBuilder.buildRevenueReportDetails(mapper));
            knowledgeSections.put("histories", KnowledgeSectionBuilder.buildHistory(mapper));
            knowledgeSections.put("permissions", KnowledgeSectionBuilder.buildPermission(mapper));
            knowledgeSections.put("positions", KnowledgeSectionBuilder.buildPosition(mapper));
            knowledgeSections.put("roles", KnowledgeSectionBuilder.buildRole(mapper));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRelevantSections(String question) {
        Set<String> keys = new LinkedHashSet<>();
        String q = question.toLowerCase();

        // Ưu tiên: quyền, tài khoản, vai trò
        if (q.contains("quyền") || q.contains("vai trò") || q.contains("tài khoản")) {
            keys.add("permissions");
            keys.add("accounts");
            keys.add("roles");
        }

        // Vị trí làm việc
        if (q.contains("vị trí") || q.contains("position")) {
            keys.add("positions");
        }

        // Nhân viên
        if (q.contains("nhân viên") || q.contains("staff")) {
            keys.add("staff");
        }

        // Khách thuê
        if (q.contains("khách") || q.contains("guest")) {
            keys.add("guests");
        }

        // Đặt phòng
        if (q.contains("đặt phòng")) {
            keys.add("booking");
        }

        // Phiếu thuê
        if (q.contains("phiếu") || q.contains("thuê")) {
            keys.add("rentalForms");
            keys.add("rentalExtensions");
            keys.add("invoices");
            keys.add("invoiceDetails");
        }

        // Phòng
        if (q.contains("phòng") || q.contains("room")) {
            keys.add("rooms");
            keys.add("roomtypes");
        }

        // Tầng / Tòa / Block
        if (q.contains("tầng") || q.contains("floor") || q.contains("tòa") || q.contains("block")) {
            keys.add("floors");
            keys.add("blocks");
        }

        // Doanh thu
        if (q.contains("doanh thu") || q.contains("báo cáo") || q.contains("tháng") || q.contains("tiền")) {
            keys.add("revenue");
            keys.add("revenueDetails");
        }

        // Fallback nếu không có từ khóa nào khớp
        if (keys.isEmpty()) {
            keys.add("rooms");
        }

        StringBuilder relevant = new StringBuilder();
        for (String key : keys) {
            relevant.append(knowledgeSections.getOrDefault(key, ""));
        }

        return relevant.toString();
    }

    private String callAI(String question) {
        try {
            String relevantData = getRelevantSections(question);

            JsonObject json = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");

            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", systemPrompt + "\n\nDữ liệu:\n" + relevantData + "\n\nCâu hỏi: " + question);
            parts.add(part);

            message.add("parts", parts);
            contents.add(message);

            json.add("contents", contents);

            JsonObject config = new JsonObject();
            config.addProperty("temperature", 0.7);
            config.addProperty("topK", 40);
            config.addProperty("topP", 0.9);
            config.addProperty("maxOutputTokens", 800);
            config.add("stopSequences", new JsonArray());

            json.add("generationConfig", config);

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                JsonObject response = JsonParser.parseReader(new java.io.InputStreamReader(connection.getInputStream())).getAsJsonObject();
                return Optional.of(response)
                        .map(r -> r.getAsJsonArray("candidates"))
                        .filter(arr -> arr.size() > 0)
                        .map(arr -> arr.get(0).getAsJsonObject().get("content"))
                        .map(content -> content.getAsJsonObject().getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString())
                        .orElse("Không có phản hồi từ API.");
            } else {
                return "Lỗi gọi API: HTTP " + responseCode;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Lỗi khi gọi AI: " + ex.getMessage();
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
}