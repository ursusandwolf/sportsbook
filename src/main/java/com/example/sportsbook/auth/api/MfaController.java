package com.example.sportsbook.auth.api;

import com.example.sportsbook.auth.api.dto.MfaSetupResponse;
import com.example.sportsbook.auth.service.MfaService;
import com.example.sportsbook.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;

    @PostMapping("/setup")
    public MfaSetupResponse setup(@AuthenticationPrincipal SecurityUser securityUser) {
        String secret = mfaService.generateNewSecret();
        MfaSetupResponse response = new MfaSetupResponse();
        response.setSecret(secret);
        // Формат для Google Authenticator: otpauth://totp/Sportsbook:email?secret=...&issuer=Sportsbook
        String uri = String.format("otpauth://totp/Sportsbook:%s?secret=%s&issuer=Sportsbook", 
                securityUser.getUsername(), secret);
        response.setQrCodeUri(uri);
        return response;
    }

    @PostMapping("/enable")
    public ResponseEntity<Void> enable(@AuthenticationPrincipal SecurityUser securityUser, 
                                       @RequestBody Map<String, String> request) {
        String secret = request.get("secret");
        String code = request.get("code");

        if (mfaService.verifyCode(secret, code)) {
            mfaService.enableMfa(securityUser.getUser().getId(), secret);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
