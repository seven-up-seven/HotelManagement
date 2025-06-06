package com.example.frontendquanlikhachsan.entity.bookingconfirmationform;

import com.example.frontendquanlikhachsan.entity.enums.BookingState;
import com.example.frontendquanlikhachsan.entity.guest.Guest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseBookingConfirmationFormDto {
    private int id;
    private BookingState bookingState;
    private LocalDateTime createdAt;
    //guest
    private String guestName;
    private String guestEmail;
    private String guestPhoneNumber;
    private int guestId;
    private String guestIdentificationNumber;
    //room
    private int roomId;
    private String roomName;
    private String roomTypeName;
}
