package com.example.frontendquanlikhachsan.entity.floor;

import com.example.frontendquanlikhachsan.entity.block.Block;
import com.example.frontendquanlikhachsan.entity.room.Room;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Floor {
    private int id;

    private String name;

    private List<Room> rooms = new ArrayList<>();

    private int blockId;

    private Block block;
}
