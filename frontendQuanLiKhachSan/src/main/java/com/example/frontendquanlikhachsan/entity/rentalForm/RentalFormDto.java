package com.example.frontendquanlikhachsan.entity.rentalForm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalFormDto {
    private Integer roomId;

    private Integer staffId;

    private LocalDateTime rentalDate;

    private Short numberOfRentalDays;

    private LocalDateTime isPaidAt;

    private String note;
}
