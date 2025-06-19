package com.example.frontendquanlikhachsan.entity.bookingconfirmationform;

import com.example.frontendquanlikhachsan.entity.enums.BookingState;
import com.example.frontendquanlikhachsan.entity.guest.Guest;
import com.example.frontendquanlikhachsan.entity.room.Room;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingConfirmationForm {
    private int id;

    private int bookingGuestId;

    private Guest bookingGuest;

    private BookingState bookingState;

    private int roomId;

    private Room room;

    private LocalDateTime createdAt;
    private LocalDateTime bookingDate;
    private int rentalDays;
}
