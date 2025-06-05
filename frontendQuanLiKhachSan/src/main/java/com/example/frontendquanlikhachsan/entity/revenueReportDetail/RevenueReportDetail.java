package com.example.frontendquanlikhachsan.entity.revenueReportDetail;

import com.example.frontendquanlikhachsan.entity.revenueReport.RevenueReport;
import com.example.frontendquanlikhachsan.entity.roomType.RoomType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class RevenueReportDetail {
    private int id;

    private double totalRoomRevenue;

    private int revenueReportId;

    private RevenueReport revenueReport;

    private int roomTypeId;

    private RoomType roomType;
}
