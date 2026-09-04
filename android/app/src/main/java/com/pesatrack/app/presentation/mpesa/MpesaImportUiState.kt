package com.pesatrack.app.presentation.mpesa

data class MpesaImportUiState(
    val permissionGranted: Boolean = false,
    val permissionDenied: Boolean = false,
    val isImporting: Boolean = false,
    val isComplete: Boolean = false,
    val foundCount: Int = 0,
    val importedCount: Int = 0,
    val duplicateCount: Int = 0,
    val failedCount: Int = 0
) {
    val processedCount: Int
        get() = importedCount + duplicateCount + failedCount

    val progressPercent: Int
        get() = if (foundCount == 0) 0 else (processedCount * 100 / foundCount)
}
