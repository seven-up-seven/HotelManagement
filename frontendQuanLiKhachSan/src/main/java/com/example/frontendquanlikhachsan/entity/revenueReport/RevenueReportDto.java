package com.example.frontendquanlikhachsan.entity.revenueReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDto {
    private Short year;

    private Byte month;

    private Double totalMonthRevenue;
}
