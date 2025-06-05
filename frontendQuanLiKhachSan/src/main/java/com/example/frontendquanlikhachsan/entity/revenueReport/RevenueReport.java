package com.example.frontendquanlikhachsan.entity.revenueReport;

import com.example.frontendquanlikhachsan.entity.revenueReportDetail.RevenueReportDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RevenueReport {
    private int id;

    private short year;

    private byte month;

    private double totalMonthRevenue;

    private List<RevenueReportDetail> revenueReportDetails = new ArrayList<>();

    private LocalDateTime createdAt;
}
