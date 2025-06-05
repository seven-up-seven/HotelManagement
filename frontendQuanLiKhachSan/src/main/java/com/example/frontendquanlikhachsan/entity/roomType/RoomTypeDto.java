package com.example.frontendquanlikhachsan.entity.roomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeDto {
    private String name;

    private Double price;
}
