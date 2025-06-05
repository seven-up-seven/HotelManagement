package com.example.frontendquanlikhachsan.entity.rentalFormDetail;

import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;
import lombok.Data;

@Data
public class ResponseRentalFormDetailDto {
    private Integer id;

    private RentalForm rentalForm;

    private Guest guest;
}
