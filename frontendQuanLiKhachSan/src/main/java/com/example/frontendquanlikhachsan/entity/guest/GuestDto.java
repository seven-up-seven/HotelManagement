package com.example.frontendquanlikhachsan.entity.guest;

import com.example.frontendquanlikhachsan.entity.enums.Sex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestDto {
    private String name;

    private Sex sex;

    private Short age;

    private String identificationNumber;

    private String phoneNumber;

    private String email;

    private Integer accountId;
}
