package com.pesatrack.app.presentation.mpesa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.data.sms.SmsParser
import com.pesatrack.app.data.sms.SmsReader
import com.pesatrack.app.data.sms.SmsTransactionImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MpesaImportViewModel(
    private val smsReader: SmsReader,
    private val parsers: List<SmsParser>,
    private val importer: SmsTransactionImporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(MpesaImportUiState())
    val uiState: StateFlow<MpesaImportUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted, permissionDenied = !granted) }
        if (granted) startImport()
    }

    private fun startImport() {
        if (_uiState.value.isImporting || _uiState.value.isComplete) return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }

            // One query + pass per registered parser, keyed by its own
            // senderPattern -- adding a parser here is the only thing a new
            // sender (e.g. a bank) needs, no change to this loop.
            for (parser in parsers) {
                val messages = smsReader.readMessages(parser.senderPattern)
                _uiState.update { it.copy(foundCount = it.foundCount + messages.size) }

                messages.forEach { sms ->
                    when (importer.import(parser, sms.body)) {
                        is SmsTransactionImporter.Result.Imported ->
                            _uiState.update { it.copy(importedCount = it.importedCount + 1) }
                        SmsTransactionImporter.Result.Duplicate ->
                            _uiState.update { it.copy(duplicateCount = it.duplicateCount + 1) }
                        SmsTransactionImporter.Result.FailedToParse ->
                            _uiState.update { it.copy(failedCount = it.failedCount + 1) }
                    }
                }
            }

            _uiState.update { it.copy(isImporting = false, isComplete = true) }
        }
    }

    class Factory(
        private val smsReader: SmsReader,
        private val parsers: List<SmsParser>,
        private val importer: SmsTransactionImporter
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MpesaImportViewModel(smsReader, parsers, importer) as T
    }
}
