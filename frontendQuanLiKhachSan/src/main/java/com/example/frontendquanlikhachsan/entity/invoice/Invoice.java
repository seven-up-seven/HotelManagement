package com.example.frontendquanlikhachsan.entity.invoice;

import com.example.frontendquanlikhachsan.entity.guest.Guest;
import com.example.frontendquanlikhachsan.entity.invoicedetail.InvoiceDetail;
import com.example.frontendquanlikhachsan.entity.staff.Staff;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Invoice {
    private int id;

    private double totalReservationCost;

    private int payingGuestId;

    private Guest payingGuest;

    private int staffId;

    private Staff staff;

    private List<InvoiceDetail> invoiceDetails = new ArrayList<>();

    private LocalDateTime createdAt;
}
