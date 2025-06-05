package com.example.frontendquanlikhachsan.entity.account;

import com.example.frontendquanlikhachsan.entity.userRole.UserRole;
import lombok.Data;

@Data
public class ResponseAccountDto {
    private int id;
    private String username;
    private String password;
    private UserRole userRole;
}
