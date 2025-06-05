package com.example.frontendquanlikhachsan.entity.staff;

import lombok.Data;

import java.util.List;
@Data
public class Staff {
    private int id;

    private String fullName;

    private int age;

    private String identificationNumber;

    private String address;

    private Sex sex;

    private float salaryMultiplier;

    private int positionId;

    private Position position;

    private Integer accountId;

    private Account account;

    private List<Invoice> invoices;

    private List<RentalForm> rentalForms;

    private List<RentalExtensionForm> rentalExtensionForms;
}
