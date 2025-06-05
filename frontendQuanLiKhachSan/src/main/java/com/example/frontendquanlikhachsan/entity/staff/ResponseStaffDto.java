package com.example.frontendquanlikhachsan.entity.staff;

import com.example.frontendquanlikhachsan.entity.account.Account;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.position.Position;
import lombok.Data;

import java.util.List;

@Data
public class ResponseStaffDto {
    private Integer id;

    private String fullName;

    private Integer age;

    private String identificationNumber;

    private String address;

    private Sex sex;

    private Float salaryMultiplier;

    private Position position;

    private Account account;

    private List<Integer> invoiceIds;

    private List<Integer> rentalExtensionFormIds;

    private List<Integer> rentalFormIds;
}
