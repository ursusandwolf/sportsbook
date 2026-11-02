# AI Map — SportsBook

Карта для ШІ/розробника: куди дивитися, що можна чіпати, що потребує рішення людини.

## Швидкий старт

| Питання | Відповідь |
|---|---|
| Точка входу | `SportsbookApplication` |
| Стек | Java 21, Spring Boot 3.4, JPA, Liquibase, Redis, JWT, TOTP |
| Архітектура | Модульний моноліт, package-by-feature |
| Гроші | тільки через `WalletService` + `WalletEntry` + idempotency key |
| Auth | `SecurityConfig` → `JwtAuthenticationFilter` → `SecurityUser` |
| Антифрод | `FraudEngine` + `FraudRule` + `RiskAssessmentService` |
| Поточний спринт | Iteration 26 done; Iteration 27 (PII) частково: `PiiMasker` |

## Модулі (owner → змінювати тут)

| Модуль | Owner-пакети | Не ламати |
|---|---|---|
| auth | `auth.api`, `auth.service` | MFA challenge ≠ access JWT |
| security | `security.config`, `security.jwt` | `denyAll` на `/api/wallets/withdraw` |
| user | `user.service.UserService` | реєстрація викликає `FraudEngine(REGISTRATION)` |
| wallet | `wallet.service.WalletService` | scale=2, optimistic lock, ledger |
| betting | `betting.service.BetService` | клієнтський `idempotencyKey` обовʼязковий |
| settlement | `settlement.service.SettlementService` | settle лише з `ACCEPTED` |
| payment | `payment.service.WithdrawalService` | KYC + risk decision |
| fraud | `fraud.service.*`, `fraud.service.rules.*` | правила = Strategy |
| common | Outbox, exceptions, `PiiMasker` | Outbox поки лише mark PROCESSED |

## Фінансовий інваріант

```
available + reserved (+ bonus окремо) змінюються ТІЛЬКИ в WalletService.execute
кожна зміна → WalletEntry з унікальним idempotencyKey (якщо передано)
```

## Безпекові інваріанти

1. JWT з claim `mfa` ніколи не ставить SecurityContext.
2. Публічні auth: тільки register/login/verify-2fa.
3. Прямий withdraw endpoint заборонений.
4. Actuator details — ADMIN.
5. Audit details проходять через `PiiMasker`.

## Де додавати фічу

| Фіча | Кроки |
|---|---|
| Нове fraud-правило | клас `implements FraudRule` + `@Component` |
| Нова wallet-операція | метод у `WalletService` через `execute`/`reserve` |
| Нова таблиця | Liquibase `migrations/0xx-...xml` + include у master |
| Новий API | `api` DTO + controller + service; не віддавати Entity |

## Заборонено без рішення людини

- Вибір брокера для Outbox (Kafka/Rabbit/HTTP)
- Повний lifecycle виводу (PSP / manual payout)
- Зміна risk-порогів (30/50/70/85)
- Мікросервісний розпил
- Видалення ролей / зміна JWT TTL у проді

## Тести

- Профіль `test`, H2 + Liquibase `db.changelog-test.xml`
- Redis у тестах: `RedisVelocityService` при помилці повертає 0
- Критичні регресії: `SecurityRegressionTests`, `WithdrawalIntegrationTests`

## Команди

```powershell
$env:JAVA_HOME="C:\Users\Admin\.jdks\corretto-21.0.12.1"
$mvn="C:\Users\Admin\.m2\wrapper\dists\apache-maven-3.9.11\03d7e36a140982eea48e22c1dcac01d8862b2550b2939e09a0809bbc5182a5bc\bin\mvn.cmd"
& $mvn test
docker compose up -d
```
