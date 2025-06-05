package com.example.frontendquanlikhachsan.entity.rentalExtensionForm;

import com.example.frontendquanlikhachsan.entity.staff.Staff;

import lombok.Data;

@Data
public class ResponseRentalExtensionFormDto {

    private RentalForm rentalForm;

    private Short numberOfRentalDays;

    private Staff staff;
}
