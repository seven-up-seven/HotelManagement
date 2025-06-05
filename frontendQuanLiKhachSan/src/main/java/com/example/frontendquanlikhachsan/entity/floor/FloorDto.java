package com.example.frontendquanlikhachsan.entity.floor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FloorDto {
    private String name;

    private Integer blockId;
}
