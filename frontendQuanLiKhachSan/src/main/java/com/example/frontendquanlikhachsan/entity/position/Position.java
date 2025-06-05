package com.example.frontendquanlikhachsan.entity.position;

import com.example.frontendquanlikhachsan.entity.staff.Staff;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class Position {
    private int id;

    private String name;

    private double baseSalary;

    private List<Staff> staffs;
}
