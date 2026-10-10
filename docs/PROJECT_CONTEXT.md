# Project Context: Sportsbook

## Status
- **Current Phase**: Iteration 26 Complete
- **Last Update**: 2026-10-10

## Critical bug fixes (2026-10-10)
- Connected migrations 016–021 and corrected local XSD resolution for withdrawals and fraud cases.
- Separated MFA challenge tokens from access-token authentication; MFA configuration requires authentication and verification rechecks account status.
- Disabled the legacy direct withdrawal endpoint. Withdrawal requests enforce KYC, amount validation and risk decisions and return a safe DTO.
- Added a separate withdrawal reservation ledger type so self-excluded users can withdraw.
- Restricted Actuator details and metrics to administrators.
- Isolated H2 test databases, made test seeds idempotent and added H2-only TEXT compatibility changes after the production migrations.
- Removed tracked target artifacts from Git; local build output remains on disk.
- Validation: `mvn clean verify` passed 56 tests and built the executable JAR. Real PostgreSQL/Redis verification remains pending.

## Project Goal
Create a modular monolith Sportsbook backend emphasizing Spring Security, financial integrity via an immutable ledger, and multi-layered anti-fraud protection.

## System State
- **Core**: Spring Boot 3.4.0, Java 21, Redis, PostgreSQL.
- **Security**: 
    - JWT-based authentication.
    - TOTP-based 2FA.
    - CORS configured for `localhost:3000`.
    - Traceability via `TraceIdFilter`.
- **Modules**: 
    - Full modular monolith architecture (`audit`, `auth`, `betting`, `bonus`, `common`, `fraud`, `market`, `payment`, `responsiblegaming`, `security`, `settlement`, `sports`, `user`, `wallet`).
- **Observability**:
    - Spring Boot Actuator (health, info, metrics, prometheus enabled).
    - Micrometer metrics implemented for `OutboxProcessor` (processing time, event count).
- **Event-Driven**:
    - Reliable event publication via Outbox pattern with batch processing.

## Roadmap & Pending Items
- [x] Iterations 1-22: Core Domain, Fraud, Case Mgmt, Security.
- [x] Iteration 23: Redis Velocity Counters.
- [x] Iteration 24: Event-driven Outbox pattern.
- [x] Iteration 25: Observability and Hardening.
- [x] Iteration 26: Deeper Metrics and Event Processing.
- [ ] Iteration 27: Production hardening (PII masking).
