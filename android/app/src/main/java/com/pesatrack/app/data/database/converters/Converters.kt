package com.pesatrack.app.data.database.converters

import android.util.Log
import androidx.room.TypeConverter
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime

private const val TAG = "Converters"

class Converters {

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? =
        value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        // Falls back to MIN rather than null/now(): the entity fields using this are
        // non-nullable, so null would just relocate the crash to a Room NPE, and now()
        // would make corrupt data silently inflate today's totals. MIN makes it visibly
        // wrong (sorts first, shows an implausible date) instead of silently plausible.
        value?.let {
            runCatching { LocalDateTime.parse(it) }
                .getOrElse { error ->
                    Log.e(TAG, "Failed to parse stored LocalDateTime: $it", error)
                    LocalDateTime.MIN
                }
        }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String =
        type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)

    @TypeConverter
    fun fromTransactionSource(source: TransactionSource): String =
        source.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource =
        TransactionSource.valueOf(value)
}