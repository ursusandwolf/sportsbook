package com.example.sportsbook.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerificationRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String mfaToken; // Используется при логине
}
