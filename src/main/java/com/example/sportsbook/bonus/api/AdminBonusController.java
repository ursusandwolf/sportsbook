package com.example.sportsbook.bonus.api;

import com.example.sportsbook.bonus.model.BonusCampaign;
import com.example.sportsbook.bonus.repository.BonusCampaignRepository;
import com.example.sportsbook.bonus.service.BonusService;
import com.example.sportsbook.user.model.User;
import com.example.sportsbook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/admin/bonuses")
@RequiredArgsConstructor
public class AdminBonusController {

    private final BonusService bonusService;
    private final BonusCampaignRepository bonusCampaignRepository;
    private final UserRepository userRepository;

    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN')")
    public BonusCampaign createCampaign(@RequestParam String name, @RequestParam String type, @RequestParam BigDecimal amount) {
        return bonusCampaignRepository.save(BonusCampaign.builder()
                .name(name)
                .type(type)
                .startAt(OffsetDateTime.now())
                .build());
    }

    @PostMapping("/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public void issueBonus(@RequestParam Long userId, @RequestParam Long campaignId, @RequestParam BigDecimal amount, @RequestParam BigDecimal wagering) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        bonusService.issueBonus(user, campaignId, amount, wagering);
    }
}
