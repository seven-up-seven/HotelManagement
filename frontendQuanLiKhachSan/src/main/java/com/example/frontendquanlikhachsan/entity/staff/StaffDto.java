package com.example.frontendquanlikhachsan.entity.staff;

import com.example.frontendquanlikhachsan.entity.enums.Sex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {
    private String fullName;

    private Integer age;

    private String identificationNumber;

    private String address;

    private Sex sex;

    private Float salaryMultiplier;

    private Integer positionId;

    private Integer accountId;
}
