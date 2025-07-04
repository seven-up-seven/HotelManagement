package com.example.frontendquanlikhachsan.entity.rentalForm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRentalFormDto {
    private Integer id;

    private int roomId;
    private String roomName;

    private int staffId;
    private String staffName;

    private LocalDateTime rentalDate;

    private Short numberOfRentalDays;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime isPaidAt;

    private List<Integer> rentalFormDetailIds;
    private List<Integer> rentalExtensionFormIds;
}
