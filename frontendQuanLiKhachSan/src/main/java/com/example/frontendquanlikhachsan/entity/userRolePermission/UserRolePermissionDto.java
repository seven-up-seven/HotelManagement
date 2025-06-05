package com.example.frontendquanlikhachsan.entity.userRolePermission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRolePermissionDto {
    private Integer userRoleId;

    private Integer permissionId;
}
