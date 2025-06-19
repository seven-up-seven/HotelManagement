package com.example.frontendquanlikhachsan.entity.userRole;

import com.example.frontendquanlikhachsan.entity.userRolePermission.UserRolePermissionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDto {
    private String name;

    private List<UserRolePermissionDto> listPermissions;
}
