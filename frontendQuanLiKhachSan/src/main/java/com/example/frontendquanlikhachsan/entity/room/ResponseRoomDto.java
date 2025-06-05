package com.example.frontendquanlikhachsan.entity.room;

import com.example.frontendquanlikhachsan.entity.enums.RoomState;
import com.example.frontendquanlikhachsan.entity.floor.Floor;
import com.example.frontendquanlikhachsan.entity.roomType.RoomType;
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
