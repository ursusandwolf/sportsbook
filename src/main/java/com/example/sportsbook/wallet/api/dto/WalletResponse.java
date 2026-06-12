package com.example.sportsbook.wallet.api.dto;

import java.math.BigDecimal;

public record WalletResponse(
    BigDecimal availableBalance,
    BigDecimal reservedBalance,
    String currency
) {}
