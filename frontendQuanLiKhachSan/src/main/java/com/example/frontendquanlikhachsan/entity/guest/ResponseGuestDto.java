package com.example.frontendquanlikhachsan.entity.guest;

import com.example.frontendquanlikhachsan.entity.enums.Sex;
import lombok.Data;

import java.util.List;

@Data
public class ResponseGuestDto {
    private int id;
    private String name;
    private Sex sex;
    private short age;
    private String identificationNumber;
    private String phoneNumber;
    private String email;
    private List<Integer> invoiceIds;
    private List<Integer> rentalFormDetailIds;
    private List<Integer> bookingConfirmationFormIds;
}
