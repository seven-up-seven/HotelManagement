package com.example.frontendquanlikhachsan.entity.revenueReport;

import lombok.Data;

import java.util.List;

@Data
public class ResponseRevenueReportDto {
    private Integer id;

    private Short year;

    private Byte month;

    private Double totalMonthRevenue;

    private List<Integer> revenueReportDetailIds;
}
