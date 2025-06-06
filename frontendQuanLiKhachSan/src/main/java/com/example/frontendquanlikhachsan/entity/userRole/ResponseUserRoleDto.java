package com.example.frontendquanlikhachsan.entity.userRole;

import lombok.Data;

import java.util.List;

@Data
public class ResponseUserRoleDto {
    private Integer id;

    private String name;

    private List<Integer> accountIds;
    private List<String> accountUsernames;

    private List<Integer> permissionIds;
    private List<String> permissionNames;
}
