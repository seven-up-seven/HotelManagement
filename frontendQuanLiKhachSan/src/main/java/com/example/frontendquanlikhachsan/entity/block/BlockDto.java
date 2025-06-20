package com.example.frontendquanlikhachsan.entity.block;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockDto {
    private Integer Id;
    private String name;
    private Double posX;
    private Double posY;
}
