package com.example.frontendquanlikhachsan.entity.rentalFormDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalFormDetailDto {
    private Integer rentalFormId;

    private Integer guestId;
}
