package com.example.frontendquanlikhachsan.entity.permission;

import java.util.List;

public class ResponsePermissionDto {
    private int id;
    private String name;
    private List<Integer> userRoleIds; // userRolePermission contains a composite key
    private List<String> userRoleNames;
}
