package com.pesatrack.app.core

import android.os.SystemClock

// Tracks when the app was last backgrounded so the process-lifecycle observer
// in AppNavigation can decide, on the next foreground, whether enough of the
// configured timeout has elapsed to re-show the lock screen. Deliberately a
// plain in-memory singleton, not persisted -- a killed process always re-locks
// via SplashScreen instead, so surviving process death isn't needed here.
object AppLockState {

    private var backgroundedAtElapsedRealtime: Long? = null

    fun markBackgrounded() {
        backgroundedAtElapsedRealtime = SystemClock.elapsedRealtime()
    }

    fun shouldRelock(timeout: LockTimeout): Boolean {
        val backgroundedAt = backgroundedAtElapsedRealtime ?: return false
        val elapsed = SystemClock.elapsedRealtime() - backgroundedAt
        return elapsed >= timeout.millis
    }
}
