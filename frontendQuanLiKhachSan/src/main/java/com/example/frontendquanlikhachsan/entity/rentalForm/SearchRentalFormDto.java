package com.example.frontendquanlikhachsan.entity.rentalForm;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

@Data
@Builder
public class SearchRentalFormDto {
    @NumberFormat
    private Integer roomId;
    private String roomName;
    @NumberFormat
    private Integer rentalFormId;

    @Override
    public String toString() {
        return "Mã phòng =" + roomId +
                ", Tên phòng = " + roomName + '\'' +
                " Mã phiếu thuê = " + rentalFormId;
    }
}
