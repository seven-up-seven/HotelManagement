package com.example.frontendquanlikhachsan.entity.block;

import com.example.frontendquanlikhachsan.entity.floor.Floor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Block {
    private int id;

    private String name;

    private Double posX;

    private Double posY;

    private List<Floor> floors = new ArrayList<>();
}
