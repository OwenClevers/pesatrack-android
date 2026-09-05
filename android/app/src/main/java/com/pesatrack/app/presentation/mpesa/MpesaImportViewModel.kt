package com.pesatrack.app.presentation.mpesa

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.core.BudgetAlertPreferences
import com.pesatrack.app.data.budget.BudgetAlertChecker
import com.pesatrack.app.data.sms.MerchantCategorizer
import com.pesatrack.app.data.sms.SmsParser
import com.pesatrack.app.data.sms.SmsReader
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

class MpesaImportViewModel(
    private val smsReader: SmsReader,
    private val parsers: List<SmsParser>,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val merchantCategorizer: MerchantCategorizer,
    private val budgetAlertChecker: BudgetAlertChecker,
    private val context: Context
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

            val categories = categoryRepository.getCategories().first()
            val enabledThresholds = BudgetAlertPreferences.enabledThresholds(context)

            // One query + pass per registered parser, keyed by its own
            // senderPattern -- adding a parser here is the only thing a new
            // sender (e.g. a bank) needs, no change to this loop.
            for (parser in parsers) {
                val messages = smsReader.readMessages(parser.senderPattern)
                _uiState.update { it.copy(foundCount = it.foundCount + messages.size) }

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
                        categoryId = merchantCategorizer.classify(parsed.counterparty, categories),
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
                    if (inserted && transaction.type == TransactionType.EXPENSE) {
                        budgetAlertChecker.check(
                            transaction.categoryId,
                            YearMonth.from(transaction.transactionDate),
                            enabledThresholds
                        )
                    }
                }
            }

            _uiState.update { it.copy(isImporting = false, isComplete = true) }
        }
    }

    class Factory(
        private val smsReader: SmsReader,
        private val parsers: List<SmsParser>,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val merchantCategorizer: MerchantCategorizer,
        private val budgetAlertChecker: BudgetAlertChecker,
        context: Context
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MpesaImportViewModel(
                smsReader,
                parsers,
                transactionRepository,
                categoryRepository,
                merchantCategorizer,
                budgetAlertChecker,
                appContext
            ) as T
    }
}
