package com.example.frontendquanlikhachsan.entity.rentalForm;

import com.example.frontendquanlikhachsan.entity.invoicedetail.InvoiceDetail;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.RentalExtensionForm;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.RentalFormDetail;
import com.example.frontendquanlikhachsan.entity.room.Room;
import com.example.frontendquanlikhachsan.entity.staff.Staff;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RentalForm {
    private int id;

    private int roomId;

    private Room room;

    private int staffId;

    private Staff staff;

    private LocalDateTime rentalDate;

    private LocalDateTime isPaidAt;

    private short numberOfRentalDays;

    private String note;

    private InvoiceDetail invoiceDetail;

    private List<RentalFormDetail> rentalFormDetails = new ArrayList<>();

    private List<RentalExtensionForm> rentalExtensionForms = new ArrayList<>();

    private LocalDateTime createdAt;
}
