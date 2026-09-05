# Roadmap

## User profile

Dashboard's "Hello, Owen" (`DashboardScreen.kt`) is a hardcoded string
literal — there's no user model behind it, and Settings has no way to
change the displayed name.

**Needs:** a `Profile` concept (at minimum a display name; the design
spec also shows an email and avatar) persisted the same way as
`OnboardingPreferences`/`ThemePreferences` (SharedPreferences, since
there's no backend or account system), a Settings row to edit it, and
`DashboardScreen` reading it instead of the literal.

## Transaction search and filter

Removed from the Transactions screen in the dead-UI sweep since neither
had anything behind them.

**Needs:** search state and filter criteria (category, type, date range)
added to `TransactionsUiState`/`TransactionsViewModel`, filtering
`transactionRepository.getTransactions()` client-side or via a new DAO
query, plus the search field and a filter sheet back in
`TransactionsScreen`.

## Report export

The Reports share icon was removed since nothing backed it.

**Needs:** an actual export implementation (CSV at minimum, matching the
data already computed in `ReportsViewModel`) written to a file and
shared via `Intent.ACTION_SEND`, plus the share icon back in
`ReportsScreen`.

## Backup and restore

Settings row removed in the dead-UI sweep.

**Needs:** an export/import path for the Room database (or a JSON dump
of `TransactionRepository`/`CategoryRepository`/`BudgetRepository`) to
a user-chosen location via the Storage Access Framework, since there's
no backend to sync against.

## Security lock

Settings row removed in the dead-UI sweep.

**Needs:** a PIN and/or biometric gate (`BiometricPrompt`) in front of
`AppNavigation`'s start destination, a way to set/change the PIN stored
in `EncryptedSharedPreferences`, and a Settings row to enable/disable it.

## Budget alert notifications

Onboarding's third page used to promise this ("get alerts when you are
nearing your limits"); the copy was corrected to describe what
`BudgetsScreen` actually does instead of building this. No notification
code exists anywhere in the app.

**Needs:** a notification channel, a `POST_NOTIFICATIONS` permission
request (API 33+), and a trigger — most likely a `WorkManager` periodic
check comparing each `BudgetRow`'s `percent` against a threshold, since
budget spend is only recomputed reactively today (when the app is open
and a transaction changes), not observed in the background.

## Multi-currency

Settings' Currency row is a non-clickable display value ("Kenyan
Shilling (KSh)" is hardcoded) — a deliberate choice while only KSh is
supported, matching `core/Formatters.kt`'s `formatKsh`.

**Needs:** a `Currency` concept (code, symbol, formatting rule) with a
selected-currency preference (same SharedPreferences pattern as dark
mode), `formatKsh` generalized into a currency-aware formatter used
everywhere it's called today, and the Currency row wired to a picker.

## Bank SMS import

Extend transaction import to bank confirmation SMS, alongside the existing
M-Pesa import.

**Status:** Blocked on real sample messages. Bank SMS formats vary a lot
between institutions (and sometimes between message types from the same
bank), so implementation needs actual anonymised sample messages per bank
to write and test parsers against — not assumed formats.

**Already in place for this:**
- `TransactionSource.BANK_SYNC` — the domain enum value this would tag
  imported transactions with.
- The `SmsParser` interface (`android/app/src/main/java/com/pesatrack/app/data/sms/SmsParser.kt`)
  — each bank gets its own implementation with its own `senderPattern`,
  registered in `AppModule.provideSmsParsers()`. `SmsReader` already
  queries by sender pattern, so no changes are needed there or in the
  import flow to add one.
