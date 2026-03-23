# BankingApp

A production-style Android banking simulator built in Kotlin with Clean Architecture, MVVM, Jetpack Compose, Retrofit/OkHttp, Hilt, and Room.

## Architecture overview

### Presentation layer
- Contains Compose screens, reusable UI components, navigation, and `ViewModel`s.
- `StateFlow` is used so screens react to state changes from a single immutable UI model.
- Validation and transient UI messages stay in the `ViewModel`, which keeps composables declarative and easy to test.

### Domain layer
- Holds business models, repository contracts, and use cases.
- The domain layer has no Android dependencies, which keeps business rules portable and easier to unit test.

### Data layer
- Combines Retrofit for remote requests and Room for local persistence.
- A repository implementation coordinates network refreshes and database caching so the UI can render cached data first.
- `MockBankingInterceptor` simulates a backend while still exercising a production-ready networking stack.

## Feature summary
- Mock login screen with validation.
- Dashboard that loads and caches bank accounts.
- Transaction history screen with cached and refreshed activity.
- Transfer money screen with required form validation and success/error feedback.
- Reusable Compose components for forms, cards, top bars, loading, and error states.

## Project structure

```text
app/
  src/main/java/com/example/bankingapp/
    data/
    di/
    domain/
    presentation/
```

## Notes
- Replace `MockBankingInterceptor` with a real backend when APIs are available.
- Room is used as the single source of truth for account and transaction lists.
