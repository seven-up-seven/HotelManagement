package com.example.frontendquanlikhachsan.entity.rentalFormDetail;

import com.example.frontendquanlikhachsan.entity.guest.Guest;
import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;

import lombok.Data;

@Data
public class RentalFormDetail {
    private int id;

    private int rentalFormId;

    private RentalForm rentalForm;

    private int guestId;

    private Guest guest;
}
