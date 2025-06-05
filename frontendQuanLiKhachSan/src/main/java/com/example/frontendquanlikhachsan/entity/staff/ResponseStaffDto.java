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

    public ResponseStaffDto(int i, String nguyễnVănA, int i1, String number, String s, Sex sex, float v, Position position, Object o, List<Integer> list, List<Integer> list1, List<Integer> list2) {
        this.id = i;
        this.fullName = nguyễnVănA;
        this.age = i1;
        this.identificationNumber = number;
        this.address = s;
        this.position = position;
    }
}
