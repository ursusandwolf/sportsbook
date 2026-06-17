package com.example.sportsbook.fraud.service;

import com.example.sportsbook.fraud.model.Device;
import com.example.sportsbook.fraud.model.UserDeviceLink;
import com.example.sportsbook.fraud.repository.DeviceRepository;
import com.example.sportsbook.fraud.repository.UserDeviceLinkRepository;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserDeviceLinkRepository userDeviceLinkRepository;
    private final UserRepository userRepository;

    public String extractFingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String ipAddress = request.getRemoteAddr();

        String rawData = (userAgent != null ? userAgent : "") +
                         (acceptLanguage != null ? acceptLanguage : "") +
                         (ipAddress != null ? ipAddress : "");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }

    @Transactional
    public void registerDeviceForUser(HttpServletRequest request, Long userId) {
        String fingerprint = extractFingerprint(request);
        
        Device device = deviceRepository.findByFingerprintHash(fingerprint)
                .orElseGet(() -> deviceRepository.save(Device.builder()
                        .fingerprintHash(fingerprint)
                        .userAgent(request.getHeader("User-Agent"))
                        .ipAddress(request.getRemoteAddr())
                        .language(request.getHeader("Accept-Language"))
                        .lastSeenAt(OffsetDateTime.now())
                        .build()));

        boolean linkExists = userDeviceLinkRepository.findByUserIdAndDeviceId(userId, device.getId()).isPresent();
        if (!linkExists) {
            User user = userRepository.getReferenceById(userId);
            userDeviceLinkRepository.save(UserDeviceLink.builder()
                    .user(user)
                    .device(device)
                    .linkedAt(OffsetDateTime.now())
                    .build());
        }
    }
}
