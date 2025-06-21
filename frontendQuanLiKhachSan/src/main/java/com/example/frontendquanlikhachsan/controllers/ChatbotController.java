package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.block.ResponseBlockDto;
import com.example.frontendquanlikhachsan.entity.bookingconfirmationform.ResponseBookingConfirmationFormDto;
import com.example.frontendquanlikhachsan.entity.floor.ResponseFloorDto;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.history.ResponseHistoryDto;
import com.example.frontendquanlikhachsan.entity.invoice.ResponseInvoiceDto;
import com.example.frontendquanlikhachsan.entity.invoicedetail.ResponseInvoiceDetailDto;
import com.example.frontendquanlikhachsan.entity.permission.ResponsePermissionDto;
import com.example.frontendquanlikhachsan.entity.position.ResponsePositionDto;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.ResponseRentalExtensionFormDto;
import com.example.frontendquanlikhachsan.entity.rentalForm.ResponseRentalFormDto;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.ResponseRentalFormDetailDto;
import com.example.frontendquanlikhachsan.entity.revenueReport.ResponseRevenueReportDto;
import com.example.frontendquanlikhachsan.entity.revenueReportDetail.ResponseRevenueReportDetailDto;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.example.frontendquanlikhachsan.entity.roomType.ResponseRoomTypeDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.userRole.ResponseUserRoleDto;
import com.example.frontendquanlikhachsan.entity.userRolePermission.ResponseUserRolePermissionDto;
import com.example.frontendquanlikhachsan.entity.variable.ResponseVariableDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ChatbotController {
    @FXML private ListView<String> chatListView;
    @FXML private TextArea inputArea;

    private final String systemPrompt = "Bạn là chatbot hỗ trợ cho phần mềm quản lý khách sạn, được tạo bởi Nhóm 5 Java. Chỉ sử dụng dữ liệu được cung cấp dưới đây để trả lời câu hỏi, không được bịa đặt thông tin. Dữ liệu được cung cấp bao gồm thông tin chi tiết về khách sạn. Hãy trả lời câu hỏi một cách ngắn gọn, chính xác và sử dụng dữ liệu phù hợp. Nếu không tìm thấy thông tin liên quan, trả lời 'Không có dữ liệu phù hợp.'";
    private final String apiKey = "AIzaSyBf0xyHSQW2A4Y2Tf6d-0R0GD_8XRz0WcE";
    private final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

    private String knowledgeBase = "";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        chatListView.getItems().add("🤖 Chatbot sẵn sàng trả lời câu hỏi!");
        loadKnowledgeBase();
        // Debug: Log knowledgeBase size
        System.out.println("KnowledgeBase size: " + knowledgeBase.length() + " characters");
    }

    @FXML
    public void handleSend() {
        String question = inputArea.getText().trim();
        if (question.isEmpty()) return;

        chatListView.getItems().add("🧑‍💼 " + question);
        inputArea.clear();

        new Thread(() -> {
            String response = callAI(question);
            Platform.runLater(() -> chatListView.getItems().add("🤖 " + response));
        }).start();
    }

    private void loadKnowledgeBase() {
        StringBuilder kb = new StringBuilder();
        try {
            kb.append("# Thông tin cơ sở dữ liệu khách sạn\n\n");

            // Account
            var json = ApiHttpClientCaller.call("account/get-all-no-page", ApiHttpClientCaller.Method.GET, null);
            List<ResponseAccountDto> accounts = Arrays.asList(mapper.readValue(json, ResponseAccountDto[].class));
            kb.append("- Có ").append(accounts.size()).append(" tài khoản người dùng\n");
            kb.append("  + Quản trị viên: ").append(accounts.stream().filter(a -> a.getUserRoleName() != null && a.getUserRoleName().equalsIgnoreCase("Admin")).count()).append(" tài khoản\n");
            kb.append("  + Nhân viên: ").append(accounts.stream().filter(a -> a.getUserRoleName() != null && a.getUserRoleName().equalsIgnoreCase("Staff")).count()).append(" tài khoản\n");
            kb.append("  + Top 3 tài khoản:\n");
            for (ResponseAccountDto account : accounts.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(account.getId())
                        .append(", Tên đăng nhập: ").append(account.getUsername() != null ? account.getUsername() : "N/A")
                        .append(", Vai trò: ").append(account.getUserRoleName() != null ? account.getUserRoleName() : "N/A")
                        .append("\n");
            }
            kb.append("\n");

            // Block
            json = ApiHttpClientCaller.call("block", ApiHttpClientCaller.Method.GET, null);
            List<ResponseBlockDto> blocks = Arrays.asList(mapper.readValue(json, ResponseBlockDto[].class));
            kb.append("- Có ").append(blocks.size()).append(" tòa trong khách sạn\n");
            int totalFloors = blocks.stream().mapToInt(b -> b.getFloorIds() != null ? b.getFloorIds().size() : 0).sum();
            kb.append("  + Tổng số tầng: ").append(totalFloors).append("\n");
            for (ResponseBlockDto block : blocks.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(block.getId())
                        .append(", Tên: ").append(block.getName() != null ? block.getName() : "N/A")
                        .append(", Số tầng: ").append(block.getFloorIds() != null ? block.getFloorIds().size() : 0)
                        .append("\n");
            }
            kb.append("\n");

            // BookingConfirmationForm
            json = ApiHttpClientCaller.call("booking-confirmation-form", ApiHttpClientCaller.Method.GET, null);
            List<ResponseBookingConfirmationFormDto> bookingForms = Arrays.asList(mapper.readValue(json, ResponseBookingConfirmationFormDto[].class));
            kb.append("- Có ").append(bookingForms.size()).append(" phiếu xác nhận đặt phòng\n");
            long pendingBookings = bookingForms.stream().filter(f -> f.getBookingState() != null && f.getBookingState().equals("PENDING")).count();
            kb.append("  + Đang chờ xác nhận: ").append(pendingBookings).append(" phiếu\n");
            kb.append("  + Đã xác nhận: ").append(bookingForms.size() - pendingBookings).append(" phiếu\n");
            kb.append("  + 3 phiếu gần đây:\n");
            for (ResponseBookingConfirmationFormDto form : bookingForms.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(form.getId())
                        .append(", Trạng thái: ").append(form.getBookingState() != null ? form.getBookingState() : "N/A")
                        .append(", Khách: ").append(form.getGuestName() != null ? form.getGuestName() : "N/A")
                        .append(", Phòng: ").append(form.getRoomName() != null ? form.getRoomName() : "N/A")
                        .append(", Ngày đặt: ").append(form.getBookingDate() != null ? form.getBookingDate() : "N/A")
                        .append("\n");
            }
            kb.append("\n");

            // Floor
            json = ApiHttpClientCaller.call("floor", ApiHttpClientCaller.Method.GET, null);
            List<ResponseFloorDto> floors = Arrays.asList(mapper.readValue(json, ResponseFloorDto[].class));
            kb.append("- Có ").append(floors.size()).append(" tầng trong khách sạn\n");
            int totalRooms = floors.stream().mapToInt(f -> f.getRoomIds() != null ? f.getRoomIds().size() : 0).sum();
            kb.append("  + Tổng số phòng: ").append(totalRooms).append("\n");
            for (ResponseFloorDto floor : floors.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(floor.getId())
                        .append(", Tên: ").append(floor.getName() != null ? floor.getName() : "N/A")
                        .append(", Tòa: ").append(floor.getBlockName() != null ? floor.getBlockName() : "N/A")
                        .append("\n");
            }
            kb.append("\n");

            // Guest
            json = ApiHttpClientCaller.call("guest", ApiHttpClientCaller.Method.GET, null);
            List<ResponseGuestDto> guests = Arrays.asList(mapper.readValue(json, ResponseGuestDto[].class));
            kb.append("- Có ").append(guests.size()).append(" khách hàng\n");
            for (ResponseGuestDto guest : guests.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(guest.getId())
                        .append(", Tên: ").append(guest.getName() != null ? guest.getName() : "N/A")
                        .append(", SĐT: ").append(guest.getPhoneNumber() != null ? guest.getPhoneNumber() : "N/A")
                        .append("\n");
            }
            kb.append("\n");

            // Invoice
            json = ApiHttpClientCaller.call("invoice", ApiHttpClientCaller.Method.GET, null);
            List<ResponseInvoiceDto> invoices = Arrays.asList(mapper.readValue(json, ResponseInvoiceDto[].class));
            kb.append("- Có ").append(invoices.size()).append(" hóa đơn\n");
            double totalRevenue = invoices.stream().mapToDouble(i -> i.getTotalReservationCost()).sum();
            kb.append("  + Tổng doanh thu: ").append(String.format("%,.0f", totalRevenue)).append(" VND\n");
            kb.append("  + 3 hóa đơn gần đây:\n");
            for (ResponseInvoiceDto invoice : invoices.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(invoice.getId())
                        .append(", Tổng tiền: ").append(String.format("%,.0f", invoice.getTotalReservationCost())).append(" VND")
                        .append(", Khách: ").append(invoice.getPayingGuestName() != null ? invoice.getPayingGuestName() : "N/A")
                        .append(", Ngày tạo: ").append(invoice.getCreatedAt() != null ? invoice.getCreatedAt() : "N/A")
                        .append("\n");
            }
            kb.append("\n");

            // Room
            json = ApiHttpClientCaller.call("room", ApiHttpClientCaller.Method.GET, null);
            List<ResponseRoomDto> rooms = Arrays.asList(mapper.readValue(json, ResponseRoomDto[].class));
            kb.append("- Có ").append(rooms.size()).append(" phòng trong khách sạn\n");
            long availableRooms = rooms.stream().filter(r -> r.getRoomState() != null && r.getRoomState().equals("AVAILABLE")).count();
            kb.append("  + Phòng trống: ").append(availableRooms).append("\n");
            kb.append("  + Phòng đang sử dụng: ").append(rooms.size() - availableRooms).append("\n");
            kb.append("  + 3 phòng mẫu:\n");
            for (ResponseRoomDto room : rooms.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(room.getId())
                        .append(", Tên: ").append(room.getName() != null ? room.getName() : "N/A")
                        .append(", Trạng thái: ").append(room.getRoomState() != null ? room.getRoomState() : "N/A")
                        .append(", Loại: ").append(room.getRoomTypeName() != null ? room.getRoomTypeName() : "N/A")
                        .append("\n");
            }
            kb.append("\n");

            // RoomType
            json = ApiHttpClientCaller.call("room-type", ApiHttpClientCaller.Method.GET, null);
            List<ResponseRoomTypeDto> roomTypes = Arrays.asList(mapper.readValue(json, ResponseRoomTypeDto[].class));
            kb.append("- Có ").append(roomTypes.size()).append(" loại phòng\n");
            double avgRoomPrice = roomTypes.stream().mapToDouble(rt -> rt.getPrice()).average().orElse(0);
            kb.append("  + Giá trung bình: ").append(String.format("%,.0f", avgRoomPrice)).append(" VND/đêm\n");
            kb.append("  + Các loại phòng:\n");
            for (ResponseRoomTypeDto roomType : roomTypes.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(roomType.getId())
                        .append(", Tên: ").append(roomType.getName() != null ? roomType.getName() : "N/A")
                        .append(", Giá: ").append(String.format("%,.0f", roomType.getPrice())).append(" VND")
                        .append("\n");
            }
            kb.append("\n");

            // Staff
            json = ApiHttpClientCaller.call("staff", ApiHttpClientCaller.Method.GET, null);
            List<ResponseStaffDto> staff = Arrays.asList(mapper.readValue(json, ResponseStaffDto[].class));
            kb.append("- Có ").append(staff.size()).append(" nhân viên\n");
            for (ResponseStaffDto staffMember : staff.stream().limit(3).collect(Collectors.toList())) {
                kb.append("    * ID: ").append(staffMember.getId())
                        .append(", Tên: ").append(staffMember.getFullName() != null ? staffMember.getFullName() : "N/A")
                        .append(", Vị trí: ").append(staffMember.getPositionName() != null ? staffMember.getPositionName() : "N/A")
                        .append("\n");
            }
            kb.append("\n");

            // RevenueReport
            json = ApiHttpClientCaller.call("revenue-report", ApiHttpClientCaller.Method.GET, null);
            List<ResponseRevenueReportDto> revenueReports = Arrays.asList(mapper.readValue(json, ResponseRevenueReportDto[].class));
            kb.append("- Có ").append(revenueReports.size()).append(" báo cáo doanh thu\n");
            ResponseRevenueReportDto latestReport = revenueReports.stream()
                    .max(Comparator.comparing(r -> r.getYear() * 100 + r.getMonth()))
                    .orElse(null);
            if (latestReport != null) {
                kb.append("  + Báo cáo mới nhất: Tháng ").append(latestReport.getMonth())
                        .append("/").append(latestReport.getYear())
                        .append(", Doanh thu: ").append(String.format("%,.0f", latestReport.getTotalMonthRevenue())).append(" VND\n");
            }
            kb.append("\n");

            // Financial Overview
            kb.append("# Tổng quan tài chính\n");
            kb.append("- Tổng doanh thu từ hóa đơn: ").append(String.format("%,.0f", totalRevenue)).append(" VND\n");
            double totalRoomTypeRevenue = roomTypes.stream().mapToDouble(rt -> rt.getPrice() * rooms.stream().filter(r -> r.getRoomTypeName() != null && r.getRoomTypeName().equals(rt.getName())).count()).sum();
            kb.append("- Doanh thu tiềm năng từ loại phòng: ").append(String.format("%,.0f", totalRoomTypeRevenue)).append(" VND\n");
            kb.append("- Số phòng trống: ").append(availableRooms).append("\n");
            kb.append("- Số phiếu đặt phòng đang chờ: ").append(pendingBookings).append("\n");

            knowledgeBase = kb.toString();
            System.out.println("KnowledgeBase size: " + knowledgeBase.length() + " characters");
        } catch (Exception e) {
            System.err.println("Error loading knowledge base: " + e.getMessage());
            e.printStackTrace();
            knowledgeBase = "Lỗi khi tải dữ liệu: " + e.getMessage();
        }
    }

    private String callAI(String question) {
        try {
            JsonObject json = new JsonObject();
            JsonArray contents = new JsonArray();

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");

            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", systemPrompt + "\n\nDữ liệu:\n" + knowledgeBase + "\n\nCâu hỏi: " + question);
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

            // Debug: Log the request payload
            System.out.println("API Request Payload: " + json.toString());

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            // Debug: Log the response code
            System.out.println("API Response Code: " + responseCode);

            if (responseCode == 200) {
                JsonObject response = JsonParser.parseReader(new java.io.InputStreamReader(connection.getInputStream())).getAsJsonObject();
                // Debug: Log the full response
                System.out.println("API Response: " + response.toString());

                return Optional.of(response)
                        .map(r -> r.getAsJsonArray("candidates"))
                        .filter(arr -> arr.size() > 0)
                        .map(arr -> arr.get(0).getAsJsonObject().get("content"))
                        .map(content -> content.getAsJsonObject().getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString())
                        .orElse("Không có phản hồi từ API.");
            } else {
                String errorMessage = "Lỗi gọi API: HTTP " + responseCode;
                System.err.println(errorMessage);
                return errorMessage;
            }

        } catch (Exception ex) {
            String errorMessage = "Lỗi khi gọi AI: " + ex.getMessage();
            System.err.println(errorMessage);
            ex.printStackTrace();
            return errorMessage;
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