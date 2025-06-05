package com.example.frontendquanlikhachsan.entity.guest;

import com.example.frontendquanlikhachsan.entity.account.Account;
import com.example.frontendquanlikhachsan.entity.bookingconfirmationform.BookingConfirmationForm;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.invoice.Invoice;
import com.example.frontendquanlikhachsan.entity.rentalFormDetail.RentalFormDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Guest {
    private int id;

    private String name;

    private Sex sex;

    private short  age;

    private String identificationNumber;

    private String phoneNumber;

    private String email;

    //khoa ngoai toi Account
    private Integer accountId;

    private Account account;

    //cac khoa ngoai toi cac form lien quan
    private List<Invoice> invoices = new ArrayList<>();

    private List<RentalFormDetail> rentalFormDetails = new ArrayList<>();

    private List<BookingConfirmationForm> bookingConfirmationForms = new ArrayList<>();
}
