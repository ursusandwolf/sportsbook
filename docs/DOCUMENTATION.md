# Technical Documentation: Sportsbook

## Architecture
Modular monolith following "package-by-feature" structure.

## Security
Four levels of protection:
1. **Spring Security**: Authentication and Role-based authorization.
2. **Object-level Authorization**: Ownership checks (e.g., wallet belongs to user).
3. **Business Validation**: Domain rules (e.g., market open, odds valid).
4. **Fraud Decision**: Risk engine and policy checks (Iteration 15+).

## Wallet & Ledger
- **Wallet**: Stores `availableBalance` and `reservedBalance`. Uses Hibernate Optimistic Locking (`@Version`).
- **WalletEntry**: Immutable ledger record for every balance change.
- **Transactions**: Atomic updates ensuring `available + reserved` consistency.

## Betting
Single bet placement involves:
1. User status check (ACTIVE).
2. Market status check (OPEN).
3. Odds version validation.
4. Wallet fund reservation (BET_RESERVE entry).
5. Bet creation (ACCEPTED status).

## Settlement
Settlement is the process of resolving a bet outcome. Supported outcomes:
- **WON**: Payout credited to available balance. `reservedBalance` decreased by stake. `WIN_PAYOUT` ledger entry.
- **LOST**: `reservedBalance` decreased by stake. `BET_LOSS` ledger entry (amount 0.00).
- **VOID / CANCELLED**: Stake returned to available balance. `reservedBalance` decreased by stake. `REFUND` ledger entry.

All settlement operations are idempotent using a generated key: `settle-{betId}-{outcome}`.

## Audit
The system maintains an immutable audit log of critical actions:
- Bet placement and settlement.
- Wallet operations.
- Admin configuration changes.
- Fraud and risk decisions (future).

Audit events record the actor, action, subject, details, and timestamp.
