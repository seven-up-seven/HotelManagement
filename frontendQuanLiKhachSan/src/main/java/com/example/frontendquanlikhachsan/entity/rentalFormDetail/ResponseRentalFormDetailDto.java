package com.example.frontendquanlikhachsan.entity.rentalFormDetail;

import com.example.frontendquanlikhachsan.entity.guest.Guest;
import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;
import lombok.Data;

@Data
public class ResponseRentalFormDetailDto {
    private Integer id;

    private int rentalFormId;

    private int guestId;

    private String guestName;
    private String guestPhoneNumber;
    private String guestEmail;
    private String guestIdentificationNumber;
}
