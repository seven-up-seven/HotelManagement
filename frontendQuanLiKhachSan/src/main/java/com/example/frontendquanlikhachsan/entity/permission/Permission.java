package com.example.frontendquanlikhachsan.entity.permission;

import com.example.frontendquanlikhachsan.entity.userRolePermission.UserRolePermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class Permission {
    private int id;

    private String name;

    private List<UserRolePermission> userRolePermissions;
}
