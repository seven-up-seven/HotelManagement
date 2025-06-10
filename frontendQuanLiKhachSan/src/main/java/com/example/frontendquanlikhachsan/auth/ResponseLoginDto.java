package com.example.frontendquanlikhachsan.auth;

public record ResponseLoginDto (
    String accessToken,
    String refreshToken,
    String message
) {}
