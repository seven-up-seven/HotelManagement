package com.example.frontendquanlikhachsan.entity.position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionDto {
    private String name;

    private Double baseSalary;
}
