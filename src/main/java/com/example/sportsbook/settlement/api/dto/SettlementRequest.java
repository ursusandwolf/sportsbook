package com.example.sportsbook.settlement.api.dto;

import com.example.sportsbook.betting.model.BetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRequest {
    
    @NotNull
    private BetStatus status;
}
