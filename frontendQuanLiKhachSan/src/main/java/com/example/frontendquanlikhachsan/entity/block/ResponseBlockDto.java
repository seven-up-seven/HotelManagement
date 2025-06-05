package com.example.frontendquanlikhachsan.entity.block;

import lombok.Data;

import java.util.List;

@Data
public class ResponseBlockDto {
    private int id;
    private String name;
    private List<Integer> floorIds;
}
