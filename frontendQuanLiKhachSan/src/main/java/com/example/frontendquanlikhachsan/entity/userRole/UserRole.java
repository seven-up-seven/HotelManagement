package com.example.frontendquanlikhachsan.entity.userRole;

import com.example.frontendquanlikhachsan.entity.account.Account;
import com.example.frontendquanlikhachsan.entity.userRolePermission.UserRolePermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserRole {
    private int id;

    private String name;

    private List<Account> accounts = new ArrayList<>();

    private List<UserRolePermission> userRolePermissions = new ArrayList<>();
}
