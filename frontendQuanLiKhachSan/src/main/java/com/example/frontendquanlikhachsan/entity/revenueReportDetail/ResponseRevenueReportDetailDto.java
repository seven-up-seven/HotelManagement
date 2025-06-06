package com.example.frontendquanlikhachsan.entity.revenueReportDetail;

import com.example.frontendquanlikhachsan.entity.revenueReport.RevenueReport;
import com.example.frontendquanlikhachsan.entity.roomType.RoomType;
import lombok.Data;

@Data
public class ResponseRevenueReportDetailDto {
    private Integer id;

    private Double totalRoomRevenue;

    private int revenueReportId;

    private int roomTypeId;
    private String roomTypeName;
}
