package com.example.frontendquanlikhachsan.entity.room;

import com.example.frontendquanlikhachsan.entity.enums.RoomState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private String name;

    private String note;

    private RoomState roomState;

    private Integer roomTypeId;

    private Integer floorId;
}
