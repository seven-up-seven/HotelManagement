package com.example.frontendquanlikhachsan.entity.variable;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class Variable {
    private int id;

    private String name;

    private double value;

    private String description;
}
