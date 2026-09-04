# Release notes

## PesaTrack v0.3.0

`develop` merged into `main` at this commit.

### Dark mode
Added a dark color scheme in `Theme.kt` built from the design tokens (dark
surfaces, adjusted text colors, the same brand green/expense red/income
green so semantics still hold), plus matching dark values in
`PesaTrackColorScheme` — a darker app bar and adjusted category tint
containers so tinted icons stay legible on dark surfaces. The setting is
persisted in SharedPreferences alongside the onboarding flag, wired to the
existing Settings toggle, and defaults to following the system setting.
Checked every screen in both themes and fixed hardcoded light-only colors
found along the way (donut/trend chart "punch-hole" circles, rank colors
in `ReportsViewModel`).

### Edge-to-edge, per-screen status bar styling
The app now draws edge-to-edge (`enableEdgeToEdge` in `MainActivity`),
with status bar icon color switched per screen — light icons on the dark
green app bars, dark icons on light surfaces, inverted again in dark mode
— via a new `StatusBarStyle.kt`. Fixed content being obscured by the
status bar or gesture nav bar where screens didn't already get it for
free from `Scaffold`/`TopAppBar` (Dashboard and Transactions' custom
headers, `PesaBottomBar`'s nav-bar padding).

### Expanded M-Pesa parser coverage, `SmsParser` interface
Added parsing for airtime purchases, Fuliza/overdraft, Pochi la Biashara,
reversals and refunds, and amounts without decimals or names with
punctuation — plus negative coverage so balance-only and failed-transaction
messages are correctly rejected. The parser is now refactored behind a
`SmsParser` interface with `MpesaSmsParser` as the first implementation,
and `SmsReader` queries by sender pattern so future parsers (e.g. bank SMS,
tracked in `docs/Roadmap.md`) can register without changing the import
flow.

### 37 unit tests
Added ViewModel money-calculation tests against fake in-memory
repositories (no Room): `DashboardViewModel`'s today's spending, month
income, remaining budget, and recent-transactions cap/sort;
`BudgetsViewModel`'s spend-vs-limit and health-tier percent boundaries;
`TransactionsViewModel`'s day grouping across a month boundary. Combined
with the expanded parser suite, the project now has 37 unit tests, all
running on plain JVM with `kotlinx-coroutines-test`.

### Dead-UI sweep
Removed affordances that had nothing behind them (Dashboard notification
bell, Transaction Details overflow menu, Reports export icon, Transactions
search bar and filter icon, several Settings rows), wired the trivial ones
instead (About PesaTrack dialog, Dashboard/Reports month selectors), and
made the Currency Settings row a non-clickable display value instead of a
dead tap target.

### Budgets write path
Added insert/update to `BudgetDao`/`BudgetRepository` and a create/edit
budget form reachable from the previously dead "Add budget" icon, with
category health coloring and the Dashboard's remaining-budget figure
updating from real data.

### Data-integrity fixes
Fixed `Transaction.toEntity()` so inserts set `createdAt` to now and
updates preserve the original `createdAt` instead of overwriting it, wired
the dead Transaction Details Edit button to the edit flow, and fixed edits
wiping the M-Pesa `smsCode` dedup key. Also replaced the Transaction
Details screen's hardcoded "Payment method: Cash" row with the existing
`Source` pill now that it's the single source of truth for
`TransactionSource`.
