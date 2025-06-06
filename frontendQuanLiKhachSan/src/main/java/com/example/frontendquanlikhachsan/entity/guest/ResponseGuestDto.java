package com.example.frontendquanlikhachsan.entity.guest;

import com.example.frontendquanlikhachsan.entity.enums.Sex;
import lombok.Data;

import java.time.LocalDateTime;
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
    //related invoice
    private List<Integer> invoiceIds;
    private List<LocalDateTime> invoiceCreatedDates;
    //related rental form details
    private List<Integer> rentalFormIds;
    private List<LocalDateTime> rentalFormCreatedDates;
    private List<Integer> rentalFormDetailIds;
    //related booking confirmation forms
    private List<Integer> bookingConfirmationFormIds;
    private List<LocalDateTime> bookingConfirmationFormCreatedDates;
    private List<String> bookingConfirmationFormRoomIds;
    private List<String> bookingConfirmationFormRoomNames;
}
