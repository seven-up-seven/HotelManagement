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
    private Guest payingGuest;
    private Staff staff;
    private LocalDateTime createdAt;
    private List<Integer> invoiceDetailIds;
}
