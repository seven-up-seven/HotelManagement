package com.example.frontendquanlikhachsan.entity.bookingconfirmationform;

import com.example.frontendquanlikhachsan.entity.enums.BookingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
    public class BookingConfirmationFormDto {
        private Integer bookingGuestId;

        private BookingState bookingState;

        private Integer roomId;
        private LocalDateTime bookingDate;
        private Integer rentalDays;
}
