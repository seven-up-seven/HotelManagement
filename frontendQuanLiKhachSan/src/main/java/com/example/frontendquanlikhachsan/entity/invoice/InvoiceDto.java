package com.example.frontendquanlikhachsan.entity.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private Double totalReservationCost;

    private Integer payingGuestId;

    private Integer staffId;
}
