package com.example.frontendquanlikhachsan.entity.floor;

import com.example.frontendquanlikhachsan.entity.block.Block;
import lombok.Data;

import java.util.List;

@Data
public class ResponseFloorDto {
    private int id;
    private String name;
    private List<Integer> roomIds;
    private List<String> roomNames;
    private String blockName;
    private int blockId;
}
