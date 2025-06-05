package com.example.frontendquanlikhachsan.entity.roomType;

import lombok.Data;

import java.util.List;

@Data
public class ResponseRoomTypeDto {
    private Integer id;

    private String name;

    private Double price;

    private List<Integer> roomIds;

    private List<Integer> revenueReportDetailIds;
}
