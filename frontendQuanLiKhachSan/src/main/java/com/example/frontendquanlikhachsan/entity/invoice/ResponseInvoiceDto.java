package com.example.frontendquanlikhachsan.entity.invoice;

import com.example.frontendquanlikhachsan.entity.guest.Guest;
import com.example.frontendquanlikhachsan.entity.staff.Staff;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResponseInvoiceDto {
    private int id;
    private double totalReservationCost;
    private String payingGuestName;
    private int payingGuestId;
    private String staffName;
    private int staffId;
    private LocalDateTime createdAt;
    //related invoice details and rental forms
    private List<Integer> invoiceDetailIds;
    private List<Integer> rentalFormIds;
}
