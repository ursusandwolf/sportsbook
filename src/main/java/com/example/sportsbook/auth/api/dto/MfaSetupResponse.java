package com.example.sportsbook.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaSetupResponse {
    private String secret;
    private String qrCodeUri; // Для фронтенда
}
