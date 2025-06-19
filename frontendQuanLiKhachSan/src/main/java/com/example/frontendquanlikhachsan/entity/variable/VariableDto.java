package com.example.frontendquanlikhachsan.entity.variable;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VariableDto {
    private String name;

    private double value;

    private String description;
}
