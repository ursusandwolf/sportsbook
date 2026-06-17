# Changelog

## [Iteration 26] - 2026-06-18
### Added
- Observability: Added Micrometer metrics (`Counter`, `Timer`) to `OutboxProcessor` to track event throughput and processing latency.
### Changed
- Event Processing: Refactored `OutboxProcessor` to use batch processing (size 100) via `Pageable` instead of `findAll()` to improve performance and scalability.

## [Iteration 25] - 2026-06-18
### Added
- Observability: Actuator endpoints (prometheus, metrics, health, info) enabled.
- Hardening: CORS policy restricted to `http://localhost:3000`.
- Traceability: `TraceIdFilter` for cross-module request tracking via MDC.
### Fixed
- Integration tests: Fixed context loading, test data initialization, and incorrect assertion expectations.

## [Iteration 24] - 2026-06-18
### Added
- Outbox pattern for reliable event processing (`outbox_events` table).
- `OutboxProcessor` scheduled task.

## [Iteration 23] - 2026-06-18
### Added
- Redis velocity counters for fraud prevention.


## [Iteration 22] - 2026-06-18
### Added
- Created `SecurityIntegrationTests` to verify unauthenticated access security.
- Configured H2 database for testing with Hibernate schema auto-generation.

## [Iteration 21] - 2026-06-18
### Added
- Implemented `CustomAuthenticationEntryPoint` for 401 Unauthorized handling.
- Implemented `CustomAccessDeniedHandler` for 403 Forbidden handling.
- Registered custom handlers in `SecurityConfig`.
- Ensured consistent `ErrorResponse` structure with `traceId` for security-related exceptions.

## [Iteration 20] - 2026-06-18
### Added
- Created `FraudCase` entity and `FraudCaseStatus` enum.
- Created `FraudCaseRepository` and `FraudCaseService`.
- Created `FraudCaseController` with `@PreAuthorize("hasRole('ROLE_RISK_ANALYST')")`.
- Added Liquibase migration for `fraud_cases` table.
- Implemented audit logging for fraud case creation and status updates.

## [Iteration 19] - 2026-06-18
### Added
- Created `Withdrawal` entity and `WithdrawalStatus` enum.
- Created `WithdrawalRepository`.
- Created `WithdrawalService` for withdrawal request orchestration (KYC, balance, fraud).
- Created `WithdrawalController` endpoint.
- Added Liquibase migration for `withdrawals` table.

## [Iteration 18] - 2026-06-18
### Added
- `BonusEligibilityRule` to check for fraud indicators (linked accounts) during bonus claim.
- Integrated `FraudEngine` into `BonusService` to trigger checks before bonus issuance.

## [Iteration 17] - 2026-06-18
### Added
- Fraud linkage engine for connecting user accounts.
- `FraudLink` entity and `FraudLinkType` enum.
- `FraudLinkageService` for processing and persisting linkages based on shared signals.
- Liquibase migration `018-create-fraud-links-table.xml`.
- Unit tests for `FraudLinkageService`.

## [Iteration 16] - 2026-06-18
### Added
- Risk scoring model based on weighted fraud signals.
- `RiskAssessmentService` for calculating scores and determining actions.
- `RiskAssessment` entity and `RiskDecision` policy.
- Liquibase migration `017-create-risk-tables.xml`.
- Unit tests for `RiskAssessmentService`.

## [Iteration 15] - 2026-06-17
### Added
- FraudRule engine infrastructure (FraudRule interface, FraudEngine orchestrator).
- FraudContext snapshot for rule evaluation.
- FraudSignal entity for storing detected signals.
- Liquibase migration `016-create-fraud-tables.xml`.
- Unit tests for FraudEngine.

## [Iteration 14] - 2026-06-17
### Added
- `payment` module for handling payment instrument fingerprinting.
- `PaymentInstrument` and `WithdrawalDestination` models.
- Fingerprint generation service using SHA-256.
- Liquibase migration `015-create-payment-tables.xml`.
- `PaymentIntegrationTests` verifying registration of instruments.

## [Iteration 13] - 2026-06-17
### Added
- Device Identification module for fraud detection infrastructure.
- `Device` and `UserDeviceLink` entities and repositories.
- `DeviceService` to extract device fingerprint (User-Agent, Accept-Language, IP).
- `DeviceIdentificationFilter` to automatically capture device signals on authenticated requests.
- Liquibase migration adding `devices` and `user_device_links` tables.
- `DeviceIntegrationTests` to verify device signal collection.

### Changed
- `SecurityConfig` updated to include `DeviceIdentificationFilter` after `JwtAuthenticationFilter`.


## [Iteration 10] - 2026-06-17
### Added
- `settlement` module for bet outcome management.
- `SettlementService` with support for WON, LOST, VOID, and CANCELLED statuses.
- `SettlementController` for admin/trader settlement operations.
- `audit` module for immutable event logging.
- `AuditService` and `AuditEvent` entity with persistence.
- Liquibase migrations for `settled_at` column in `bets` and `audit_events` table.
- New integration tests in `SettlementIntegrationTests`.

### Changed
- `WalletService` updated with idempotent `settleWin`, `settleLoss`, and `settleRefund` methods.
- `WalletService.execute` modified to allow zero amounts (required for `BET_LOSS`).
- `SecurityUser` updated with `getId()` for easier access to user identity in audit logs.
- `Bet` entity updated with `settledAt` field.

## [Iteration 9] - 2026-06-17
### Added
- `betting` module for single bet placement.
- `Bet` entity with status tracking (ACCEPTED, PENDING, etc.).
- `BetService` coordinating bet placement with wallet reservation.
- `BetController` for player-facing betting API.
- Liquibase migration for `bets` table with financial constraints.
- `BettingIntegrationTests` verifying placement, validation, and fund reservation.

### Changed
- `WalletService` added `reserveFunds` method with optimistic locking.
- `GlobalExceptionHandler` updated to map `IllegalStateException` to HTTP 422.

## [Iteration 8] - 2026-06-17
### Added
- `sports` module: `Sport`, `SportEvent` entities and management APIs.
- `market` module: `Market`, `Selection` entities and odds management.
- Liquibase migrations for sports, events, markets, and selections.
- Integration tests for sports and market management.

## [Iteration 7] - 2026-06-17
### Added
- `wallet` module: `Wallet` and `WalletEntry` (ledger).
- Immutable ledger pattern for financial operations.
- Optimistic locking on `Wallet` to prevent race conditions.
- Idempotency support for wallet operations.
- Integration tests for deposits and concurrent reservations.

## [Iteration 6.5] - 2026-06-17
### Added
- MFA integration: TOTP (Google Authenticator) support.
- Two-step login flow: credentials -> 202 Accepted + mfaToken -> verify TOTP -> final JWT.
- `MfaService` for secret generation and verification.
- `MfaIntegrationTests`.

## [Iteration 6] - 2026-06-17
### Added
- JWT authentication: Stateless login with `Bearer` tokens.
- `JwtUtils`, `JwtAuthenticationFilter`, and login endpoint.
- `SportsbookApplicationTests` verified context loading.

## [Iteration 5] - 2026-06-17
### Added
- `@PreAuthorize` on controller methods.
- `SecurityUtils` for accessing current user principal.
- `/api/users/me` and `/api/admin/users` endpoints.

## [Iteration 4] - 2026-06-17
### Added
- `RegistrationController` and `RegistrationRequest` DTO.
- `UserService` for handling user creation and validation.
- Liquibase migration 001 for users and roles.

## [Iteration 3] - 2026-06-17
### Added
- `BCryptPasswordEncoder` for secure password storage.
- User Status checks in `SecurityUser`.

## [Iteration 2] - 2026-06-17
### Added
- JPA-backed `UserDetailsService`.
- `User`, `Role` entities.

## [Iteration 1] - 2026-06-17
### Added
- Basic `SecurityFilterChain` with HTTP Basic.

## [Iteration 0] - 2026-06-17
### Added
- Initial project structure.
- Health check endpoint.
