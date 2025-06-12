package com.example.frontendquanlikhachsan.entity.rentalForm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRentalFormDto {
    private Integer roomId;
    private String roomName;
    private Integer rentalFormId;

    @Override
    public String toString() {
        return "Mã phòng =" + roomId +
                ", Tên phòng = " + roomName + '\'' +
                " Mã phiếu thuê = " + rentalFormId;
    }
}
