# Roadmap

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
