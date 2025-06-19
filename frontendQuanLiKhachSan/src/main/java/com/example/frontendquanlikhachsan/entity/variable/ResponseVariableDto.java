package com.example.frontendquanlikhachsan.entity.variable;

import lombok.Builder;
import lombok.Data;

@Data
public class ResponseVariableDto {
    private int id;

    private String name;

    private double value;

    private String description;
}
