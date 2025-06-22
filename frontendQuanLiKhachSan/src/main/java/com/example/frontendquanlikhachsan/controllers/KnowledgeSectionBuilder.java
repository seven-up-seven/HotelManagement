package com.example.frontendquanlikhachsan.controllers;

import com.example.frontendquanlikhachsan.ApiHttpClientCaller;
import com.example.frontendquanlikhachsan.entity.account.ResponseAccountDto;
import com.example.frontendquanlikhachsan.entity.block.ResponseBlockDto;
import com.example.frontendquanlikhachsan.entity.bookingconfirmationform.ResponseBookingConfirmationFormDto;
import com.example.frontendquanlikhachsan.entity.floor.ResponseFloorDto;
import com.example.frontendquanlikhachsan.entity.guest.ResponseGuestDto;
import com.example.frontendquanlikhachsan.entity.invoice.ResponseInvoiceDto;
import com.example.frontendquanlikhachsan.entity.revenueReport.ResponseRevenueReportDto;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.example.frontendquanlikhachsan.entity.roomType.ResponseRoomTypeDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class KnowledgeSectionBuilder {
    public static String buildAccounts(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Tài khoản người dùng\n");
        var json = ApiHttpClientCaller.call("account/get-all-no-page", ApiHttpClientCaller.Method.GET, null);
        List<ResponseAccountDto> accounts = Arrays.asList(mapper.readValue(json, ResponseAccountDto[].class));
        kb.append("- Số lượng tài khoản: ").append(accounts.size()).append("\n");
        for (ResponseAccountDto acc : accounts) {
            kb.append("  * ID: ").append(acc.getId()).append(", Username: ").append(acc.getUsername())
                    .append(", Role: ").append(acc.getUserRoleName()).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildBlocks(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Danh sách tòa\n");
        var json = ApiHttpClientCaller.call("block", ApiHttpClientCaller.Method.GET, null);
        List<ResponseBlockDto> blocks = Arrays.asList(mapper.readValue(json, ResponseBlockDto[].class));
        kb.append("- Có ").append(blocks.size()).append(" tòa\n");
        for (ResponseBlockDto b : blocks) {
            kb.append("  * ID: ").append(b.getId()).append(", Tên: ").append(b.getName())
                    .append(", Số tầng: ").append(b.getFloorIds() != null ? b.getFloorIds().size() : 0).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildBookingForms(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Phiếu xác nhận đặt phòng\n");
        var json = ApiHttpClientCaller.call("booking-confirmation-form", ApiHttpClientCaller.Method.GET, null);
        List<ResponseBookingConfirmationFormDto> list = Arrays.asList(mapper.readValue(json, ResponseBookingConfirmationFormDto[].class));
        kb.append("- Tổng phiếu: ").append(list.size()).append("\n");
        for (ResponseBookingConfirmationFormDto dto : list) {
            kb.append("  * ID: ").append(dto.getId()).append(", Trạng thái: ").append(dto.getBookingState())
                    .append(", Khách: ").append(dto.getGuestName()).append(", Phòng: ").append(dto.getRoomName())
                    .append(", Ngày đặt: ").append(dto.getBookingDate()).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildFloors(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Danh sách tầng\n");
        var json = ApiHttpClientCaller.call("floor", ApiHttpClientCaller.Method.GET, null);
        List<ResponseFloorDto> floors = Arrays.asList(mapper.readValue(json, ResponseFloorDto[].class));
        kb.append("- Tổng số tầng: ").append(floors.size()).append("\n");
        for (ResponseFloorDto f : floors) {
            kb.append("  * ID: ").append(f.getId()).append(", Tên: ").append(f.getName())
                    .append(", Tòa: ").append(f.getBlockName()).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildGuests(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Danh sách khách hàng\n");
        var json = ApiHttpClientCaller.call("guest", ApiHttpClientCaller.Method.GET, null);
        List<ResponseGuestDto> list = Arrays.asList(mapper.readValue(json, ResponseGuestDto[].class));
        kb.append("- Số lượng khách: ").append(list.size()).append("\n");
        for (ResponseGuestDto g : list) {
            kb.append("  * ID: ").append(g.getId()).append(", Tên: ").append(g.getName()).append(", SĐT: ").append(g.getPhoneNumber()).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildInvoices(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Hóa đơn\n");
        var json = ApiHttpClientCaller.call("invoice", ApiHttpClientCaller.Method.GET, null);
        List<ResponseInvoiceDto> list = Arrays.asList(mapper.readValue(json, ResponseInvoiceDto[].class));
        double total = list.stream().mapToDouble(ResponseInvoiceDto::getTotalReservationCost).sum();
        kb.append("- Số lượng hóa đơn: ").append(list.size()).append(", Tổng tiền: ").append(String.format("%,.0f", total)).append(" VND\n");
        for (ResponseInvoiceDto i : list) {
            kb.append("  * ID: ").append(i.getId()).append(", Tổng tiền: ").append(String.format("%,.0f", i.getTotalReservationCost())).append(" VND")
                    .append(", Khách: ").append(i.getPayingGuestName()).append(", Ngày tạo: ").append(i.getCreatedAt()).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRooms(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Danh sách phòng\n");
        var json = ApiHttpClientCaller.call("room", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRoomDto> rooms = Arrays.asList(mapper.readValue(json, ResponseRoomDto[].class));
        kb.append("- Số lượng phòng: ").append(rooms.size()).append("\n");
        for (ResponseRoomDto r : rooms) {
            kb.append("  * ID: ").append(r.getId()).append(", Tên: ").append(r.getName()).append(", Trạng thái: ").append(r.getRoomState())
                    .append(", Loại phòng: ").append(r.getRoomTypeName()).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRoomTypes(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Loại phòng\n");
        var json = ApiHttpClientCaller.call("room-type", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRoomTypeDto> list = Arrays.asList(mapper.readValue(json, ResponseRoomTypeDto[].class));
        kb.append("- Số loại phòng: ").append(list.size()).append("\n");
        for (ResponseRoomTypeDto rt : list) {
            kb.append("  * ID: ").append(rt.getId()).append(", Tên: ").append(rt.getName()).append(", Giá: ").append(String.format("%,.0f", rt.getPrice())).append(" VND\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildStaff(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Nhân viên\n");
        var json = ApiHttpClientCaller.call("staff", ApiHttpClientCaller.Method.GET, null);
        List<ResponseStaffDto> staff = Arrays.asList(mapper.readValue(json, ResponseStaffDto[].class));
        kb.append("- Số lượng nhân viên: ").append(staff.size()).append("\n");
        for (ResponseStaffDto s : staff) {
            kb.append("  * ID: ").append(s.getId()).append(", Tên: ").append(s.getFullName()).append(", Vị trí: ").append(s.getPositionName()).append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRevenueReports(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Báo cáo doanh thu\n");
        var json = ApiHttpClientCaller.call("revenue-report", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRevenueReportDto> reports = Arrays.asList(mapper.readValue(json, ResponseRevenueReportDto[].class));
        kb.append("- Số lượng báo cáo: ").append(reports.size()).append("\n");
        ResponseRevenueReportDto latest = reports.stream().max(Comparator.comparing(r -> r.getYear() * 100 + r.getMonth())).orElse(null);
        if (latest != null) {
            kb.append("  + Báo cáo mới nhất: Tháng ").append(latest.getMonth()).append("/").append(latest.getYear())
                    .append(", Doanh thu: ").append(String.format("%,.0f", latest.getTotalMonthRevenue())).append(" VND\n");
        }
        return kb.append("\n").toString();
    }
}
