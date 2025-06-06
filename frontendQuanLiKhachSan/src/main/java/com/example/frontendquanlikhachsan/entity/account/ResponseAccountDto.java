package com.example.frontendquanlikhachsan.entity.account;

import com.example.frontendquanlikhachsan.entity.userRole.UserRole;
import lombok.Data;

import java.util.List;

@Data
public class ResponseAccountDto {
    private int id;
    private String username;
    private String password;
    //userRole
    private int userRoleId;
    private String userRoleName;
    private List<Integer> userRolePermissionIds;
    private List<String> userRolePermissionNames;
}
