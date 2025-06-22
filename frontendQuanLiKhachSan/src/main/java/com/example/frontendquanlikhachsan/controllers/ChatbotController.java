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

    private final String systemPrompt = "Bạn là chatbot hỗ trợ cho phần mềm quản lý khách sạn, được tạo bởi Nhóm 5 Java. Chỉ sử dụng dữ liệu được cung cấp dưới đây để trả lời câu hỏi, không được bịa đặt thông tin. Nếu không tìm thấy thông tin liên quan, trả lời 'Không có dữ liệu phù hợp.'";
    private final String apiKey = "AIzaSyBf0xyHSQW2A4Y2Tf6d-0R0GD_8XRz0WcE";
    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Map<String, String> knowledgeSections = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        addChatBubble("🤖 Chatbot sẵn sàng trả lời câu hỏi!", false);
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
                ? "-fx-background-color: #D6EAF8; -fx-padding: 10; -fx-background-radius: 12; -fx-font-size: 13px;"
                : "-fx-background-color: #E8F8F5; -fx-padding: 10; -fx-background-radius: 12; -fx-font-size: 13px;");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRelevantSections(String question) {
        StringBuilder relevant = new StringBuilder();
        String q = question.toLowerCase();

        if (q.contains("phòng")) relevant.append(knowledgeSections.getOrDefault("rooms", ""));
        if (q.contains("tầng")) relevant.append(knowledgeSections.getOrDefault("floors", ""));
        if (q.contains("khách")) relevant.append(knowledgeSections.getOrDefault("guests", ""));
        if (q.contains("đặt phòng")) relevant.append(knowledgeSections.getOrDefault("booking", ""));
        if (q.contains("nhân viên")) relevant.append(knowledgeSections.getOrDefault("staff", ""));
        if (q.contains("hóa đơn")) {
            relevant.append(knowledgeSections.getOrDefault("invoices", ""));
            relevant.append(knowledgeSections.getOrDefault("invoiceDetails", ""));
        }
        if (q.contains("doanh thu")) {
            relevant.append(knowledgeSections.getOrDefault("revenue", ""));
            relevant.append(knowledgeSections.getOrDefault("revenueDetails", ""));
        }
        if (q.contains("gia hạn")) relevant.append(knowledgeSections.getOrDefault("rentalExtensions", ""));
        if (q.contains("phiếu thuê") || q.contains("thuê phòng")) relevant.append(knowledgeSections.getOrDefault("rentalForms", ""));
        if (q.contains("loại phòng")) relevant.append(knowledgeSections.getOrDefault("roomtypes", ""));
        if (q.contains("block") || q.contains("tòa")) relevant.append(knowledgeSections.getOrDefault("blocks", ""));
        if (q.contains("tài khoản")) relevant.append(knowledgeSections.getOrDefault("accounts", ""));

        if (relevant.length() == 0) {
            relevant.append(knowledgeSections.getOrDefault("rooms", ""));
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

    private void showError(String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText("Có lỗi xảy ra");
        alert.setContentText(error);
        alert.showAndWait();
    }
}