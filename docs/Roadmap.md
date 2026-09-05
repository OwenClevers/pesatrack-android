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
