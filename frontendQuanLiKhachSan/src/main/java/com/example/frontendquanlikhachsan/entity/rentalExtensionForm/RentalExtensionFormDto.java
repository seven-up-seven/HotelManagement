package com.example.frontendquanlikhachsan.entity.rentalExtensionForm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalExtensionFormDto {
    private Short numberOfRentalDays;

    private Integer rentalFormId;

    private Integer staffId;
}