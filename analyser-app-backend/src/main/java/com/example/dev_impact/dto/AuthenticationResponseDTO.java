package com.example.dev_impact.dto;

public class AuthenticationResponseDTO {

    private String token;

    private String message;

    private String displayName;

    public AuthenticationResponseDTO(String token, String message, String displayName) {
        this.token = token;
        this.message = message;
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public String getDisplayName() {
        return displayName;
    }
}
