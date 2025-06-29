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
import com.example.frontendquanlikhachsan.entity.revenueReport.ResponseRevenueReportDto;
import com.example.frontendquanlikhachsan.entity.revenueReportDetail.ResponseRevenueReportDetailDto;
import com.example.frontendquanlikhachsan.entity.room.ResponseRoomDto;
import com.example.frontendquanlikhachsan.entity.roomType.ResponseRoomTypeDto;
import com.example.frontendquanlikhachsan.entity.staff.ResponseStaffDto;
import com.example.frontendquanlikhachsan.entity.userRole.ResponseUserRoleDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public static String buildHistory(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Lịch sử hoạt động\n");
        var json = ApiHttpClientCaller.call("history", ApiHttpClientCaller.Method.GET, null);
        List<ResponseHistoryDto> historyList = Arrays.asList(mapper.readValue(json, ResponseHistoryDto[].class));
        kb.append("- Số lượng hành động: ").append(historyList.size()).append("\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (ResponseHistoryDto his : historyList) {
            kb.append("  * ID: ").append(his.getId())
                    .append(", Thời gian: ").append(his.getExecuteAt().format(formatter))
                    .append(", Người thực hiện: ").append(his.getImpactor())
                    .append(" (ID: ").append(his.getImpactorId()).append(")")
                    .append("\n    Đối tượng bị ảnh hưởng: ").append(his.getAffectedObject())
                    .append(" (ID: ").append(his.getAffectedObjectId()).append(")")
                    .append(", Hành động: ").append(his.getAction())
                    .append("\n    Nội dung: ").append(his.getContent())
                    .append("\n");
        }

        return kb.append("\n").toString();
    }

    public static String buildPermission(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Danh sách quyền hệ thống\n");

        var json = ApiHttpClientCaller.call("permission", ApiHttpClientCaller.Method.GET, null);
        List<ResponsePermissionDto> permissions = Arrays.asList(mapper.readValue(json, ResponsePermissionDto[].class));

        kb.append("- Tổng số quyền: ").append(permissions.size()).append("\n");

        for (ResponsePermissionDto perm : permissions) {
            kb.append("  * ID: ").append(perm.getId())
                    .append(", Tên quyền: ").append(perm.getName()).append("\n");

            if (perm.getUserRoleNames() != null && !perm.getUserRoleNames().isEmpty()) {
                kb.append("    Áp dụng cho vai trò: ").append(String.join(", ", perm.getUserRoleNames())).append("\n");
            } else {
                kb.append("    Không áp dụng cho vai trò nào.\n");
            }
        }

        return kb.append("\n").toString();
    }

    public static String buildPosition(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Danh sách chức vụ trong hệ thống\n");

        var json = ApiHttpClientCaller.call("position", ApiHttpClientCaller.Method.GET, null);
        List<ResponsePositionDto> positions = Arrays.asList(mapper.readValue(json, ResponsePositionDto[].class));

        kb.append("- Tổng số chức vụ: ").append(positions.size()).append("\n");

        for (ResponsePositionDto pos : positions) {
            kb.append("  * ID: ").append(pos.getId())
                    .append(", Tên chức vụ: ").append(pos.getName())
                    .append(", Lương cơ bản: ").append(String.format("%,.0f VND", pos.getBaseSalary()))
                    .append("\n");

            if (pos.getStaffNames() != null && !pos.getStaffNames().isEmpty()) {
                kb.append("    Nhân viên đảm nhận: ").append(String.join(", ", pos.getStaffNames())).append("\n");
            } else {
                kb.append("    Hiện chưa có nhân viên nào đảm nhận.\n");
            }
        }

        return kb.append("\n").toString();
    }

    public static String buildRole(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Danh sách vai trò người dùng\n");

        var json = ApiHttpClientCaller.call("user-role", ApiHttpClientCaller.Method.GET, null);
        List<ResponseUserRoleDto> roles = Arrays.asList(mapper.readValue(json, ResponseUserRoleDto[].class));

        kb.append("- Tổng số vai trò: ").append(roles.size()).append("\n");

        for (ResponseUserRoleDto role : roles) {
            kb.append("  * ID: ").append(role.getId())
                    .append(", Tên vai trò: ").append(role.getName())
                    .append("\n");

            if (role.getPermissionNames() != null && !role.getPermissionNames().isEmpty()) {
                kb.append("  Quyền được cấp: ")
                        .append(String.join(", ", role.getPermissionNames()))
                        .append("\n");
            } else {
                kb.append("  Vai trò này chưa được cấp quyền nào.\n");
            }
        }

        return kb.append("\n").toString();
    }

    public static String buildBookingForms(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Phiếu xác nhận đặt phòng\n");
        var json = ApiHttpClientCaller.call("booking-confirmation-form", ApiHttpClientCaller.Method.GET, null);
        List<ResponseBookingConfirmationFormDto> list = Arrays.asList(mapper.readValue(json, ResponseBookingConfirmationFormDto[].class));
        kb.append("- Tổng phiếu: ").append(list.size()).append("\n");
        for (ResponseBookingConfirmationFormDto dto : list) {
            kb.append("  * ID: ").append(dto.getId())
                    .append(", Trạng thái: ").append(dto.getBookingState())
                    .append(", Ngày tạo: ").append(dto.getCreatedAt())
                    .append(", Ngày đặt: ").append(dto.getBookingDate())
                    .append(", Số ngày thuê: ").append(dto.getRentalDays())
                    .append("\n    Khách: ").append(dto.getGuestName())
                    .append(" (ID: ").append(dto.getGuestId()).append(")")
                    .append(", Email: ").append(dto.getGuestEmail())
                    .append(", SĐT: ").append(dto.getGuestPhoneNumber())
                    .append(", CCCD: ").append(dto.getGuestIdentificationNumber())
                    .append("\n    Phòng: ").append(dto.getRoomName())
                    .append(" (ID: ").append(dto.getRoomId()).append(")")
                    .append(", Loại phòng: ").append(dto.getRoomTypeName())
                    .append("\n");
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
            kb.append("  * ID: ").append(g.getId())
                    .append(", Tên: ").append(g.getName())
                    .append(", Giới tính: ").append(g.getSex())
                    .append(", Tuổi: ").append(g.getAge())
                    .append(", CCCD: ").append(g.getIdentificationNumber())
                    .append(", SĐT: ").append(g.getPhoneNumber())
                    .append(", Email: ").append(g.getEmail())
                    .append("\n    - Số phiếu thuê: ").append(g.getRentalFormIds() != null ? g.getRentalFormIds().size() : 0)
                    .append(", Số hóa đơn: ").append(g.getInvoiceIds() != null ? g.getInvoiceIds().size() : 0)
                    .append(", Số phiếu đặt phòng: ").append(g.getBookingConfirmationFormIds() != null ? g.getBookingConfirmationFormIds().size() : 0)
                    .append("\n");
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

        Map<String, Integer> roomTypeCount = new HashMap<>();
        for (ResponseRoomDto r : rooms) {
            String roomType = r.getRoomTypeName();
            roomTypeCount.put(roomType, roomTypeCount.getOrDefault(roomType, 0) + 1);
        }

        Map<String, Integer> roomStateCount = new HashMap<>();
        for (ResponseRoomDto r : rooms) {
            String roomState = r.getRoomState().name(); 
            roomStateCount.put(roomState, roomStateCount.getOrDefault(roomState, 0) + 1);
        }

        kb.append("- Tổng số phòng: ").append(rooms.size()).append("\n");
        kb.append("- Số lượng theo loại phòng:\n");
        for (Map.Entry<String, Integer> entry : roomTypeCount.entrySet()) {
            kb.append("  * ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" phòng\n");
        }
        kb.append("- Số lượng theo trạng thái phòng:\n");
        for (Map.Entry<String, Integer> entry : roomStateCount.entrySet()) {
            kb.append("  * ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" phòng\n");
        }

        for (ResponseRoomDto r : rooms) {
            kb.append("  * ID: ").append(r.getId())
                    .append(", Tên: ").append(r.getName())
                    .append(", Trạng thái: ").append(r.getRoomState().name())
                    .append(", Ghi chú: ").append(r.getNote())
                    .append(", Loại phòng: ").append(r.getRoomTypeName()).append(" (ID: ").append(r.getRoomTypeId()).append(")")
                    .append(", Tầng: ").append(r.getFloorName()).append(" (ID: ").append(r.getFloorId()).append(")")
                    .append("\n    - Số phiếu đặt phòng: ").append(r.getBookingConfirmationFormIds() != null ? r.getBookingConfirmationFormIds().size() : 0)
                    .append(", Số phiếu thuê: ").append(r.getRentalFormIds() != null ? r.getRentalFormIds().size() : 0)
                    .append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRoomTypes(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Loại phòng\n");
        var json = ApiHttpClientCaller.call("room-type", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRoomTypeDto> list = Arrays.asList(mapper.readValue(json, ResponseRoomTypeDto[].class));
        kb.append("- Số loại phòng: ").append(list.size()).append("\n");
        for (ResponseRoomTypeDto rt : list) {
            kb.append("  * ID: ").append(rt.getId())
                    .append(", Tên: ").append(rt.getName())
                    .append(", Giá: ").append(String.format("%,.0f", rt.getPrice())).append(" VND")
                    .append("\n    - Số phòng thuộc loại: ").append(rt.getRoomIds() != null ? rt.getRoomIds().size() : 0)
                    .append(", Số báo cáo liên quan: ").append(rt.getRevenueReportDetailIds() != null ? rt.getRevenueReportDetailIds().size() : 0)
                    .append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildStaff(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Nhân viên\n");
        var json = ApiHttpClientCaller.call("staff", ApiHttpClientCaller.Method.GET, null);
        List<ResponseStaffDto> staff = Arrays.asList(mapper.readValue(json, ResponseStaffDto[].class));
        kb.append("- Số lượng nhân viên: ").append(staff.size()).append("\n");
        for (ResponseStaffDto s : staff) {
            kb.append("  * ID: ").append(s.getId())
                    .append(", Họ tên: ").append(s.getFullName())
                    .append(", Giới tính: ").append(s.getSex())
                    .append(", Tuổi: ").append(s.getAge())
                    .append(", CCCD: ").append(s.getIdentificationNumber())
                    .append(", Địa chỉ: ").append(s.getAddress())
                    .append(", Lương hệ số: ").append(s.getSalaryMultiplier())
                    .append(", Vị trí: ").append(s.getPositionName()).append(" (ID: ").append(s.getPositionId()).append(")")
                    .append("\n    - Tài khoản: ").append(s.getAccountUsername()).append(" (ID: ").append(s.getAccountId()).append(")")
                    .append(", Hóa đơn: ").append(s.getInvoiceIds() != null ? s.getInvoiceIds().size() : 0)
                    .append(", Phiếu thuê: ").append(s.getRentalFormIds() != null ? s.getRentalFormIds().size() : 0)
                    .append(", Gia hạn: ").append(s.getRentalExtensionFormIds() != null ? s.getRentalExtensionFormIds().size() : 0)
                    .append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRevenueReports(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Báo cáo doanh thu\n");
        var json = ApiHttpClientCaller.call("revenue-report", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRevenueReportDto> reports = Arrays.asList(mapper.readValue(json, ResponseRevenueReportDto[].class));
        kb.append("- Số lượng báo cáo: ").append(reports.size()).append("\n");
        for (ResponseRevenueReportDto r : reports) {
            kb.append("  * ID: ").append(r.getId())
                    .append(", Tháng: ").append(r.getMonth())
                    .append("/").append(r.getYear())
                    .append(", Doanh thu: ").append(String.format("%,.0f", r.getTotalMonthRevenue())).append(" VND")
                    .append(", Báo cáo chi tiết: ").append(r.getRevenueReportDetailIds() != null ? r.getRevenueReportDetailIds().size() : 0)
                    .append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRentalForms(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Phiếu thuê phòng\n");
        var json = ApiHttpClientCaller.call("rental-form", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRentalFormDto> list = Arrays.asList(mapper.readValue(json, ResponseRentalFormDto[].class));
        kb.append("- Số lượng phiếu thuê: ").append(list.size()).append("\n");
        for (ResponseRentalFormDto dto : list) {
            kb.append("  * ID: ").append(dto.getId())
                    .append(", Ngày thuê: ").append(dto.getRentalDate())
                    .append(", Số ngày: ").append(dto.getNumberOfRentalDays())
                    .append(", Ngày thanh toán: ").append(dto.getIsPaidAt())
                    .append(", Ghi chú: ").append(dto.getNote())
                    .append("\n    - Phòng: ").append(dto.getRoomName()).append(" (ID: ").append(dto.getRoomId()).append(")")
                    .append(", Nhân viên: ").append(dto.getStaffName()).append(" (ID: ").append(dto.getStaffId()).append(")")
                    .append("\n    - Chi tiết thuê: ").append(dto.getRentalFormDetailIds() != null ? dto.getRentalFormDetailIds().size() : 0)
                    .append(", Gia hạn: ").append(dto.getRentalExtensionFormIds() != null ? dto.getRentalExtensionFormIds().size() : 0)
                    .append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRentalExtensionForms(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Phiếu gia hạn thuê phòng\n");
        var json = ApiHttpClientCaller.call("rental-extension-form", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRentalExtensionFormDto> list = Arrays.asList(mapper.readValue(json, ResponseRentalExtensionFormDto[].class));
        kb.append("- Số phiếu gia hạn: ").append(list.size()).append("\n");
        for (ResponseRentalExtensionFormDto dto : list) {
            kb.append("  * ID: ").append(dto.getId())
                    .append(", Số ngày gia hạn: ").append(dto.getNumberOfRentalDays())
                    .append(", Còn lại: ").append(dto.getDayRemains() != null ? dto.getDayRemains() : "N/A")
                    .append("\n    - Phòng: ").append(dto.getRentalFormRoomName())
                    .append(", Phiếu thuê: ").append(dto.getRentalFormId())
                    .append(", Nhân viên xử lý: ").append(dto.getStaffName()).append(" (ID: ").append(dto.getStaffId()).append(")")
                    .append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildInvoiceDetails(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Chi tiết hóa đơn\n");
        var json = ApiHttpClientCaller.call("invoice-detail", ApiHttpClientCaller.Method.GET, null);
        List<ResponseInvoiceDetailDto> list = Arrays.asList(mapper.readValue(json, ResponseInvoiceDetailDto[].class));
        kb.append("- Số lượng chi tiết hóa đơn: ").append(list.size()).append("\n");
        for (ResponseInvoiceDetailDto dto : list) {
            kb.append("  * ID: ").append(dto.getId())
                    .append(", Số ngày thuê: ").append(dto.getNumberOfRentalDays())
                    .append(", Tổng tiền: ").append(String.format("%,.0f", dto.getReservationCost())).append(" VND")
                    .append("\n    - Hóa đơn: ").append(dto.getInvoiceId())
                    .append(", Phiếu thuê: ").append(dto.getRentalFormId())
                    .append(", Phòng: ").append(dto.getRoomId())
                    .append("\n");
        }
        return kb.append("\n").toString();
    }

    public static String buildRevenueReportDetails(ObjectMapper mapper) throws Exception {
        StringBuilder kb = new StringBuilder("# Chi tiết báo cáo doanh thu\n");
        var json = ApiHttpClientCaller.call("revenue-report-detail", ApiHttpClientCaller.Method.GET, null);
        List<ResponseRevenueReportDetailDto> list = Arrays.asList(mapper.readValue(json, ResponseRevenueReportDetailDto[].class));
        kb.append("- Số lượng chi tiết báo cáo: ").append(list.size()).append("\n");
        for (ResponseRevenueReportDetailDto dto : list) {
            kb.append("  * ID: ").append(dto.getId())
                    .append(", Doanh thu phòng: ").append(String.format("%,.0f", dto.getTotalRoomRevenue())).append(" VND")
                    .append("\n    - Báo cáo: ").append(dto.getRevenueReportId())
                    .append(", Loại phòng: ").append(dto.getRoomTypeName()).append(" (ID: ").append(dto.getRoomTypeId()).append(")")
                    .append("\n");
        }
        return kb.append("\n").toString();
    }
}
