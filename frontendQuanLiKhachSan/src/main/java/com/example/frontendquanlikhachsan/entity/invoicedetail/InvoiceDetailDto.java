package com.example.frontendquanlikhachsan.entity.invoicedetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDetailDto {
    private Integer numberOfRentalDays;

    private Integer invoiceId;

    private Double reservationCost;

    private Integer rentalFormId;
}
