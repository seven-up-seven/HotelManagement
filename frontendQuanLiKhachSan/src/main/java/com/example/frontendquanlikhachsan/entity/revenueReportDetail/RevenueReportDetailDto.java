package com.example.frontendquanlikhachsan.entity.revenueReportDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDetailDto {
    private Double totalRoomRevenue;

    private Integer revenueReportId;

    private Integer roomTypeId;
}
