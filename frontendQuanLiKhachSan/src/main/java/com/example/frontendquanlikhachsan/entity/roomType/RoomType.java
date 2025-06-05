package com.example.frontendquanlikhachsan.entity.roomType;

import com.example.frontendquanlikhachsan.entity.revenueReportDetail.RevenueReportDetail;
import com.example.frontendquanlikhachsan.entity.room.Room;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoomType {
    private int id;

    private  String name;

    private double price;

    private List<Room> rooms = new ArrayList<>();

    private List<RevenueReportDetail> revenueReportDetails = new ArrayList<>();
}
