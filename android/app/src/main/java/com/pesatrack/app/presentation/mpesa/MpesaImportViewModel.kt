package com.pesatrack.app.presentation.mpesa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.data.sms.MpesaSmsParser
import com.pesatrack.app.data.sms.SmsReader
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MpesaImportViewModel(
    private val smsReader: SmsReader,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val parser = MpesaSmsParser()

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

            val categories = categoryRepository.getCategories().first()
            val messages = smsReader.readMpesaMessages()
            _uiState.update { it.copy(foundCount = messages.size) }

            messages.forEach { sms ->
                val parsed = parser.parse(sms.body)
                if (parsed == null) {
                    _uiState.update { it.copy(failedCount = it.failedCount + 1) }
                    return@forEach
                }

                val transaction = Transaction(
                    id = 0,
                    amount = parsed.amount,
                    type = parsed.type,
                    categoryId = matchCategory(parsed.counterparty, categories),
                    merchant = parsed.counterparty,
                    description = null,
                    transactionDate = parsed.timestamp,
                    source = TransactionSource.MPESA_SMS
                )

                val inserted = transactionRepository.importMpesaTransaction(transaction, parsed.transactionCode)
                _uiState.update {
                    if (inserted) it.copy(importedCount = it.importedCount + 1)
                    else it.copy(duplicateCount = it.duplicateCount + 1)
                }
            }

            _uiState.update { it.copy(isImporting = false, isComplete = true) }
        }
    }

    class Factory(
        private val smsReader: SmsReader,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MpesaImportViewModel(smsReader, transactionRepository, categoryRepository) as T
    }
}

// Simple keyword match against the counterparty text, keyed by the seeded categories'
// iconKey. Falls back to "other" for anything unrecognised (most person-to-person
// transfers, since they carry a name rather than a business/merchant keyword).
private val categoryKeywords: Map<String, List<String>> = mapOf(
    "food" to listOf("RESTAURANT", "CAFE", "HOTEL", "EATERY", "KFC", "JAVA", "PIZZA", "CHICKEN", "BAKERY"),
    "fuel" to listOf("PETROL", "FUEL", "SHELL", "TOTAL ENERGIES", "OILIBYA", "RUBIS", "GAS STATION"),
    "shopping" to listOf("SUPERMARKET", "MART", "SHOP", "STORE", "NAIVAS", "CARREFOUR", "QUICKMART", "TUSKYS"),
    "utilities" to listOf("KPLC", "ELECTRICITY", "WATER", "UTILITY", "UTILITIES"),
    "entertainment" to listOf("CINEMA", "MOVIE", "NETFLIX", "SHOWMAX", "ENTERTAINMENT"),
    "transport" to listOf("UBER", "BOLT", "TAXI", "MATATU", "TRANSPORT", "BUS"),
    "medical" to listOf("HOSPITAL", "CLINIC", "PHARMACY", "CHEMIST", "MEDICAL"),
    "education" to listOf("SCHOOL", "UNIVERSITY", "COLLEGE", "TUITION", "EDUCATION")
)

private fun matchCategory(counterparty: String, categories: List<Category>): Long {
    val text = counterparty.uppercase()
    val iconKey = categoryKeywords.entries
        .firstOrNull { (_, keywords) -> keywords.any { text.contains(it) } }
        ?.key
        ?: "other"

    return categories.firstOrNull { it.iconKey == iconKey }?.id
        ?: categories.firstOrNull { it.iconKey == "other" }?.id
        ?: categories.firstOrNull()?.id
        ?: 0L
}
