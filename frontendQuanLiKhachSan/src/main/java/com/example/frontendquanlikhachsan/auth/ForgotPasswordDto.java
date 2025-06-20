package com.example.frontendquanlikhachsan.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordDto {

    private String username;

    private String email;
}
