# Навчальний гід по коду SportsBook

## 1. З чого читати (порядок)

1. `README.md` — можливості й запуск
2. `docs/AI_MAP.md` — карта модулів
3. `WalletService` — як правильно рухати гроші
4. `BetService` + `SettlementService` — повний цикл ставки
5. `JwtAuthenticationFilter` + `AuthController` — MFA challenge
6. `FraudEngine` + одне правило в `fraud/service/rules`
7. `WithdrawalService` — KYC + risk decision

## 2. Ключові уроки

### Гроші ніколи не «мінус у полі»
Усі зміни балансу йдуть через `WalletService.execute`: scale=2, перевірка self-exclusion, idempotency, ledger `WalletEntry`.

### Ідемпотентність — не optional
Ставка вимагає клієнтський `idempotencyKey`. Повтор з тим самим ключем повертає ту саму ставку без другого резерву.

### MFA — два типи токенів
Challenge JWT має claim `mfa` і не створює authenticated principal. Access JWT — після TOTP.

### Антифрод = Strategy
Нове правило = новий `@Component implements FraudRule`. Движок фільтрує за `FraudOperation`.

### Outbox ≠ брокер
Подія зберігається в тій же транзакції, що ставка. Worker поки лише логує і ставить `PROCESSED` — навчальний скелет.

## 3. Вправи

| # | Завдання | Файл |
|---|---|---|
| 1 | Додати `FraudRule` на підозрілий stake | `fraud/service/rules` |
| 2 | Дописати confirm payout з reserved | `WalletService` + withdrawal status |
| 3 | Підключити реальний publisher у Outbox | `OutboxProcessor` |
| 4 | Testcontainers PostgreSQL замість H2 | `src/test` |

## 4. Типові помилки новачка

- Правити баланс напряму в контролері
- Повертати JPA `User`/`Withdrawal` з паролем у JSON
- Генерувати idempotency key на сервері через `UUID.randomUUID()` для клієнтських ретраїв
- Використовувати MFA-токен як Bearer для `/api/wallets/me`
