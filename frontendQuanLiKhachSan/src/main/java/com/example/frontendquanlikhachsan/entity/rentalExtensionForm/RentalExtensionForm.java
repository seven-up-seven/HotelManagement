package com.example.frontendquanlikhachsan.entity.rentalExtensionForm;

import com.example.frontendquanlikhachsan.entity.staff.Staff;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RentalExtensionForm {
    private int id;

    private int rentalFormId;

    private RentalForm rentalForm;

    private short numberOfRentalDays;

    private LocalDateTime createdAt;

    private int staffId;

    private Staff staff;
}
