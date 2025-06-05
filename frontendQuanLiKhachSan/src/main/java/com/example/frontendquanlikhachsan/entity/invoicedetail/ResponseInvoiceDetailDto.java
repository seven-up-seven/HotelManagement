package com.example.frontendquanlikhachsan.entity.invoicedetail;

import com.example.frontendquanlikhachsan.entity.invoice.Invoice;
import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;
import lombok.Data;

@Data
public class ResponseInvoiceDetailDto {
    private int id;
    private int numberOfRentalDays;
    private Invoice invoice;
    private double reservationCost;
    private RentalForm rentalForm;
}
