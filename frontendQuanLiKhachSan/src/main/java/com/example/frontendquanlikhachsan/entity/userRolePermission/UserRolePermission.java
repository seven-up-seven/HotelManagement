package com.example.frontendquanlikhachsan.entity.userRolePermission;

import com.example.frontendquanlikhachsan.entity.userRole.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.security.Permission;

@Data
public class UserRolePermission {
    private int userRoleId;

    private int permissionId;

    private UserRole userRole;

    private Permission permission;
}
