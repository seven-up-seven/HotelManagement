package com.example.frontendquanlikhachsan.entity.rentalForm;

import com.example.frontendquanlikhachsan.entity.staff.Staff;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class ResponseRentalFormDto {
    private Integer id;

    private Room room;

    private Staff staff;

    private LocalDateTime rentalDate;

    private Short numberOfRentalDays;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime isPaidAt;

    private List<Integer> rentalFormDetailIds;
    private List<Integer> rentalExtensionFormIds;
}
