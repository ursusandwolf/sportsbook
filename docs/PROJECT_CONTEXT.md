# Project Context: Sportsbook

## Status
- **Current Phase**: Iteration 25 Complete
- **Last Update**: 2026-06-18

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

## Roadmap & Pending Items
- [x] Iterations 1-22: Core Domain, Fraud, Case Mgmt, Security.
- [x] Iteration 23: Redis Velocity Counters.
- [x] Iteration 24: Event-driven Outbox pattern.
- [x] Iteration 25: Observability and Hardening.
- [ ] Iteration 26: Production hardening (PII masking, deeper metrics, event-driven integration completion).
