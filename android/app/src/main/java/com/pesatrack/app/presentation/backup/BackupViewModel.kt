package com.pesatrack.app.presentation.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.data.backup.BackupManager
import com.pesatrack.app.data.backup.BackupSerializer
import com.pesatrack.app.data.backup.RestoreMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BackupViewModel(
    private val context: Context,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun export(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, message = null) }
            val result = runCatching {
                val payload = backupManager.createBackup()
                val json = BackupSerializer.serialize(payload)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                } ?: error("Could not open the selected file for writing.")
            }
            _uiState.update {
                it.copy(
                    isWorking = false,
                    message = result.fold(
                        onSuccess = { "Backup saved." },
                        onFailure = { e -> "Backup failed: ${e.message}" }
                    )
                )
            }
        }
    }

    fun loadBackupFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, message = null) }
            val result = runCatching {
                val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.readBytes().decodeToString()
                } ?: error("Could not open the selected file for reading.")
                BackupSerializer.deserialize(json)
            }
            result.fold(
                onSuccess = { payload ->
                    _uiState.update { it.copy(isWorking = false, pendingRestore = payload) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isWorking = false, message = "Couldn't read that file: ${e.message}") }
                }
            )
        }
    }

    fun confirmRestore(mode: RestoreMode) {
        val payload = _uiState.value.pendingRestore ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, pendingRestore = null) }
            val result = runCatching { backupManager.restore(payload, mode) }
            _uiState.update {
                it.copy(
                    isWorking = false,
                    message = result.fold(
                        onSuccess = { r ->
                            "Restored ${r.importedTransactions} transactions, " +
                                "${r.importedCategories} categories, and ${r.importedBudgets} budgets." +
                                if (r.skippedTransactions > 0) {
                                    " Skipped ${r.skippedTransactions} duplicate transactions."
                                } else {
                                    ""
                                }
                        },
                        onFailure = { e -> "Restore failed: ${e.message}" }
                    )
                )
            }
        }
    }

    fun dismissPendingRestore() {
        _uiState.update { it.copy(pendingRestore = null) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    class Factory(
        context: Context,
        private val backupManager: BackupManager
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BackupViewModel(appContext, backupManager) as T
    }
}
