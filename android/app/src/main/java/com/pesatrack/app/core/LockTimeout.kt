package com.pesatrack.app.core

// Options for how long the app can sit backgrounded before the security lock
// re-engages. IMMEDIATE (the default) re-locks on every single background/
// foreground cycle, matching the "defaulting to immediately" requirement.
enum class LockTimeout(val millis: Long, val label: String) {
    IMMEDIATE(0L, "Immediately"),
    ONE_MINUTE(60_000L, "After 1 minute"),
    FIVE_MINUTES(5 * 60_000L, "After 5 minutes"),
    FIFTEEN_MINUTES(15 * 60_000L, "After 15 minutes"),
    THIRTY_MINUTES(30 * 60_000L, "After 30 minutes");

    companion object {
        fun fromMillis(millis: Long): LockTimeout =
            entries.firstOrNull { it.millis == millis } ?: IMMEDIATE
    }
}
