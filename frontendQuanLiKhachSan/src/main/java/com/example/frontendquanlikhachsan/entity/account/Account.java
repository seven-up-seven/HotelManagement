package com.example.frontendquanlikhachsan.entity.account;

import com.example.frontendquanlikhachsan.entity.guest.Guest;
import com.example.frontendquanlikhachsan.entity.staff.Staff;
import com.example.frontendquanlikhachsan.entity.userRole.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Account {
    private int id;

    private String username;

    private String password;

    //khoa ngoai toi vai tro cua tai khoan
    private int userRoleId;

    private UserRole userRole;

    //khoa ngoai toi Guest
    private Guest guest;

    //khoa ngoai toi Staff
    private Staff staff;
}
