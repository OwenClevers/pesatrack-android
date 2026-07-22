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
        value?.let(LocalDateTime::parse)

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