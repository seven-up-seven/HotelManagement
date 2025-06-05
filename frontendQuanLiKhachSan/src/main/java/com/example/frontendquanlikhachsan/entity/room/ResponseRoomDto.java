package com.example.frontendquanlikhachsan.entity.room;

import lombok.Data;

import java.util.List;

@Data
public class ResponseRoomDto {
    private Integer id;

    private String name;

    private String note;

    private RoomState roomState;

    private RoomType roomType;

    private Floor floor;

    private List<Integer> bookingConfirmationFormIds;

    private List<Integer> rentalFormIds;
}
