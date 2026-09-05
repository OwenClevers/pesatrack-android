# Release notes

## PesaTrack v0.4.0

`develop` merged into `main` at this commit. A completeness audit against
`docs/pesatrack-design-spec.html` covering dead UI, spec gaps, and
half-wired features (data layer present but not fully connected to the
UI, the same shape as last release's missing Budgets write path) turned
up four issues, all fixed here.

### Category icon and color customization
`Category.colorKey` was persisted through the whole Room stack (seed
data, entity, mapper) but never actually read — `CategoryVisuals.visual()`
derived both icon and color from a single hardcoded switch on `iconKey`,
and `addCategory()` hardcoded every new category to `iconKey = "other"`.
Any category a user created was permanently stuck looking like "Other."
`visual()` now reads `colorKey` independently of `iconKey`, and the
add/rename sheet gets icon and color pickers so a custom category can
look like anything a seeded one can. Seeded categories are unaffected,
since their `colorKey` already equals their `iconKey` — exactly the
pairing the old hardcoded switch used.

### Budget delete path
`BudgetDao`/`BudgetRepository` only ever had `upsert` — once a category
was budgeted there was no way to remove it, only re-limit it. Added
`deleteById`/`deleteBudget`, and a Delete button with a confirmation
dialog in `BudgetSheet`, matching how Categories already handles delete.

### Corrected onboarding copy
Onboarding's third page promised budget alerts ("get alerts when you are
nearing your limits"), but no notification system exists anywhere in the
app. Reworded it to describe what budgets actually do — the percent and
health-color progress each `BudgetCard` already shows.

### Removed unreachable DAO methods
Deleted `TransactionDao.getLatestTransaction()` and `.deleteAll()`,
neither of which was ever called from `TransactionRepository` or
anywhere else — leftovers from before smsCode-based dedup and an
unshipped "reset app data" feature, respectively.

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
