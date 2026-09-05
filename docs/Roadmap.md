# Roadmap

## Merge-restore transaction dedup for non-SMS transactions

`BackupManager`'s merge-restore mode dedupes transactions by `smsCode`,
matching the existing M-Pesa import dedup. A transaction with no
`smsCode` (manual entries, and anything imported without one in the
future) has no dedup key, so merging the same backup twice duplicates
it -- documented, deliberate scope for now, not a bug.

**If this becomes a problem:** fall back to a content hash (amount +
type + categoryId + transactionDate, maybe merchant) for transactions
without an `smsCode`, checked the same way `importMpesaTransaction`
checks the unique index today.

## Report export

The Reports share icon was removed since nothing backed it.

**Needs:** an actual export implementation (CSV at minimum, matching the
data already computed in `ReportsViewModel`) written to a file and
shared via `Intent.ACTION_SEND`, plus the share icon back in
`ReportsScreen`.

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
