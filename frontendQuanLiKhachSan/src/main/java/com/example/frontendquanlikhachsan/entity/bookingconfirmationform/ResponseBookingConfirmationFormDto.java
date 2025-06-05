package com.example.frontendquanlikhachsan.entity.bookingconfirmationform;

import com.example.frontendquanlikhachsan.entity.enums.BookingState;
import com.example.frontendquanlikhachsan.entity.guest.Guest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseBookingConfirmationFormDto {
    private int id;
    private Guest bookingGuest;
    private BookingState bookingState;
    private LocalDateTime createdAt;
}
