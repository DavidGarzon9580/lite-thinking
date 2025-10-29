package com.litethinking.platform.auth.dto;

public record TokenResponse(
        String token,
        long expiresInMinutes
) {
}
