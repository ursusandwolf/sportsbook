# Архітектура та UML (псевдографіка)

## Контекст

Модульний моноліт: один deployable JAR, межі — пакети предметних областей.
Адекватність: для портфоліо/навчання — так; для production scale — Outbox без брокера і H2-only тести — обмеження.

## Патерни

| Патерн | Де | Навіщо |
|---|---|---|
| Modular Monolith / Package-by-feature | `com.example.sportsbook.*` | ізоляція доменів без мережевих меж |
| Layered (api → service → repository) | кожен модуль | тонкі контролери, логіка в сервісах |
| Strategy | `FraudRule` + правила | нові сигнали без правки движка |
| Template Method (фактично) | `WalletService.execute` | єдиний ledger/idempotency/self-exclusion |
| Outbox | `OutboxService` + `OutboxProcessor` | надійна фіксація подій у тій же TX |
| Optimistic Locking | `Wallet.version`, `Bet.version` | конкурентні гроші без песимістичних locks |
| Filter Chain | JWT, DeviceIdentification | cross-cutting security |
| DTO на межі API | `*Response` / `*Request` | не світити JPA Entity / password hash |
| Scheduled Worker | `@Scheduled` Outbox | фонова обробка |

## UML класів (ядро)

```
┌─────────────────────┐       ┌──────────────────────┐
│ RegistrationController│────▶│ UserService          │
└─────────────────────┘       │ +register(req, ip)   │
                              └──────────┬───────────┘
                                         │
                    ┌────────────────────┼────────────────────┐
                    ▼                    ▼                    ▼
           ┌────────────────┐   ┌────────────────┐   ┌────────────────┐
           │ WalletService  │   │ FraudEngine    │   │ UserRepository │
           └───────┬────────┘   │ +run(ctx):List │   └────────────────┘
                   │            └───────┬────────┘
                   │                    │ uses
                   │            ┌───────▼────────┐
                   │            │ «interface»    │
                   │            │ FraudRule      │
                   │            └───────┬────────┘
                   │     ┌──────────────┼────────────────┐
                   │     ▼              ▼                ▼
                   │ RegistrationVelocityRule  ...інші правила
                   ▼
        ┌─────────────────────────┐
        │ Wallet / WalletEntry    │
        │ (@Version на Wallet)    │
        └─────────────────────────┘
```

## UML ставок і розрахунку

```
BetController ──▶ BetService ──▶ SelectionRepository
                     │
                     ├─▶ ResponsibleGamingService
                     ├─▶ WalletService.reserveFunds
                     ├─▶ BetRepository (idempotency_key UNIQUE)
                     └─▶ OutboxService.saveEvent(BET_PLACED)

SettlementController ──▶ SettlementService
                              ├─▶ WalletService.settleWin/Loss/Refund
                              └─▶ AuditService (PiiMasker)
```

## UML виводу

```
WithdrawalController ──▶ WithdrawalService
                              ├─ KYC / status / amount scale
                              ├─ FraudEngine(WITHDRAWAL)
                              ├─ RiskAssessmentService.assess
                              ├─ WithdrawalRepository.save
                              └─ WalletService.reserveWithdrawal
```

## UML security

```
HttpRequest
   │
   ▼
JwtAuthenticationFilter ── reject if claims.mfa
   │
   ▼
DeviceIdentificationFilter
   │
   ▼
SecurityFilterChain matchers
   │  permit: register/login/verify-2fa, public health/events
   │  denyAll: POST /api/wallets/withdraw
   │  ADMIN: /actuator/**
   ▼
@PreAuthorize на контролерах
```

## Потік грошей (sequence)

```
Player          BetService         WalletService        DB
  │ placeBet(key) │                    │                 │
  │──────────────▶│ findByIdempotency  │                 │
  │               │─────────────────────────────────────▶│
  │               │ reserveFunds(key)  │                 │
  │               │───────────────────▶│ UPDATE wallet   │
  │               │                    │ INSERT entry    │
  │               │ save Bet(key)      │                 │
  │               │─────────────────────────────────────▶│
  │               │ outbox BET_PLACED  │                 │
  │◀──────────────│                    │                 │
```
