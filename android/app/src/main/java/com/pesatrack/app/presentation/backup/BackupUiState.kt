package com.pesatrack.app.presentation.backup

import com.pesatrack.app.data.backup.BackupPayload

data class BackupUiState(
    val isWorking: Boolean = false,
    // A file the user picked to restore from, read and parsed, awaiting a
    // merge/replace choice before anything is written.
    val pendingRestore: BackupPayload? = null,
    val message: String? = null
)
