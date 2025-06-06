package com.example.frontendquanlikhachsan.entity.rentalExtensionForm;

import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;
import com.example.frontendquanlikhachsan.entity.staff.Staff;

import lombok.Data;

@Data
public class ResponseRentalExtensionFormDto {

    private int id;

    private int rentalFormId;

    private String rentalFormRoomName;

    private Short numberOfRentalDays;

    private int staffId;
    private String staffName;
}
