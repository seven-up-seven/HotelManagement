package com.example.frontendquanlikhachsan.entity.userRolePermission;

import com.example.frontendquanlikhachsan.entity.userRole.UserRole;
import lombok.Builder;
import lombok.Data;

import java.security.Permission;

@Data
public class ResponseUserRolePermissionDto {
    private Integer userRoleId;

    private UserRole role;

    private Permission permission;
}
