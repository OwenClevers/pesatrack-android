package com.pesatrack.app.data.database.converters

import androidx.room.TypeConverter
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? =
        value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        // Falls back to now() rather than null/throwing: the entity fields using this
        // are non-nullable, so a null here would just relocate the crash to a Room NPE.
        value?.let { runCatching { LocalDateTime.parse(it) }.getOrDefault(LocalDateTime.now()) }

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