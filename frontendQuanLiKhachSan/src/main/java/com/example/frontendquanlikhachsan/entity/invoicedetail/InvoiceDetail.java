package com.example.frontendquanlikhachsan.entity.invoicedetail;

import com.example.frontendquanlikhachsan.entity.invoice.Invoice;
import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetail {
    private int id;

    private int numberOfRentalDays;

    private int invoiceId;

    private Invoice invoice;

    private double reservationCost;

    private int rentalFormId;

    private RentalForm rentalForm;
}
