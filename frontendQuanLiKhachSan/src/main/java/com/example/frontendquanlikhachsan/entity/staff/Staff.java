package com.example.frontendquanlikhachsan.entity.staff;

import com.example.frontendquanlikhachsan.entity.account.Account;
import com.example.frontendquanlikhachsan.entity.enums.Sex;
import com.example.frontendquanlikhachsan.entity.invoice.Invoice;
import com.example.frontendquanlikhachsan.entity.position.Position;
import com.example.frontendquanlikhachsan.entity.rentalExtensionForm.RentalExtensionForm;
import com.example.frontendquanlikhachsan.entity.rentalForm.RentalForm;
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

    private String email;

    private List<Invoice> invoices;

    private List<RentalForm> rentalForms;

    private List<RentalExtensionForm> rentalExtensionForms;
}
