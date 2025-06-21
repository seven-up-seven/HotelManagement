package com.example.frontendquanlikhachsan.entity.block;

import lombok.Data;

import java.util.List;

@Data
public class ResponseBlockDto {
    private int id;
    private String name;
    private Double posX;
    private Double posY;
    private List<Integer> floorIds;
    private List<String> floorNames;
}
