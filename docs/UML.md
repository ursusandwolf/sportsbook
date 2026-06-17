# UML Diagrams (Pseudographic)

### Settlement Pattern
```text
[Trader/Admin] -> [SettlementController] -> [SettlementService]
                                                   |
                                                   +-> [BetRepository] (Update status/settledAt)
                                                   |
                                                   +-> [WalletService] (Update balances/ledger)
                                                   |
                                                   +-> [AuditService] (Log event)
```

### Betting Pattern
```text
[Player] -> [BetController] -> [BetService]
                                    |
                                    +-> [SportsService] (Market/Odds Check)
                                    |
                                    +-> [WalletService] (Reserve Funds)
                                    |
                                    +-> [BetRepository] (Save Bet)
```

### Wallet Operation Pattern (Immutable Ledger)
```text
[Service] -> [WalletService]
                  |
                  v
       [Transaction Boundary]
          |   1. Load Wallet (Optimistic Lock)
          |   2. Validate Balance
          |   3. Update available/reserved
          |   4. Create WalletEntry (Ledger)
          |   5. Save Wallet (Version++)
       [/Transaction Boundary]
```

### MFA Flow Pattern
```text
[Player] -> [Login (Credentials)] -> [202 Accepted + mfaToken]
[Player] -> [Verify (mfaToken + TOTP Code)] -> [200 OK + JWT]
```

### Modular Monolith structure
```text
com.example.sportsbook
├── auth (Registration, MFA)
├── security (FilterChain, JWT, Principal)
├── user (User, Roles, Status)
├── wallet (Balance, Ledger, Atomic updates)
├── sports (Sports, Events, Line management)
├── market (Markets, Selections, Odds)
├── betting (Bet Slip, Placement)
├── settlement (Payout, Void, Idempotency)
├── audit (Immutable Event Log)
└── common (Health, Global Exceptions)
```
