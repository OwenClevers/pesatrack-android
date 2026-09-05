package com.pesatrack.app.data.backup

import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.YearMonth

class BackupFormatException(message: String) : Exception(message)

/**
 * Serializes/deserializes a [BackupPayload] to/from JSON using the platform's
 * built-in org.json classes -- no serialization library dependency needed.
 */
object BackupSerializer {

    const val CURRENT_SCHEMA_VERSION = 1

    fun serialize(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("schemaVersion", payload.schemaVersion)
        root.put("exportedAt", payload.exportedAt.toString())
        root.put("categories", JSONArray(payload.categories.map(::categoryToJson)))
        root.put("transactions", JSONArray(payload.transactions.map(::transactionToJson)))
        root.put("budgets", JSONArray(payload.budgets.map(::budgetToJson)))
        return root.toString(2)
    }

    fun deserialize(json: String): BackupPayload {
        val root = try {
            JSONObject(json)
        } catch (e: JSONException) {
            throw BackupFormatException("Not a valid PesaTrack backup file.")
        }

        val schemaVersion = root.optInt("schemaVersion", -1)
        if (schemaVersion == -1) {
            throw BackupFormatException("Missing schema version -- this doesn't look like a PesaTrack backup file.")
        }
        if (schemaVersion > CURRENT_SCHEMA_VERSION) {
            throw BackupFormatException(
                "This backup was made with a newer version of PesaTrack (schema $schemaVersion) " +
                    "than this app supports (schema $CURRENT_SCHEMA_VERSION). Update the app and try again."
            )
        }

        // Only schema 1 exists today; a future format change adds a branch here
        // (and bumps CURRENT_SCHEMA_VERSION) rather than changing this parsing
        // in place, so older backups stay readable.
        return try {
            BackupPayload(
                schemaVersion = schemaVersion,
                exportedAt = LocalDateTime.parse(root.getString("exportedAt")),
                categories = parseArray(root.getJSONArray("categories"), ::categoryFromJson),
                transactions = parseArray(root.getJSONArray("transactions"), ::transactionFromJson),
                budgets = parseArray(root.getJSONArray("budgets"), ::budgetFromJson)
            )
        } catch (e: BackupFormatException) {
            throw e
        } catch (e: Exception) {
            throw BackupFormatException("This backup file is corrupted or in an unexpected format.")
        }
    }

    private fun <T> parseArray(array: JSONArray, parse: (JSONObject) -> T): List<T> =
        (0 until array.length()).map { index -> parse(array.getJSONObject(index)) }

    private fun categoryToJson(category: Category): JSONObject =
        JSONObject().apply {
            put("id", category.id)
            put("name", category.name)
            put("iconKey", category.iconKey)
            put("colorKey", category.colorKey)
        }

    private fun categoryFromJson(json: JSONObject): Category =
        Category(
            id = json.getLong("id"),
            name = json.getString("name"),
            iconKey = json.getString("iconKey"),
            colorKey = json.getString("colorKey")
        )

    private fun transactionToJson(transaction: Transaction): JSONObject =
        JSONObject().apply {
            put("id", transaction.id)
            put("amount", transaction.amount)
            put("type", transaction.type.name)
            put("categoryId", transaction.categoryId)
            put("merchant", transaction.merchant)
            put("description", transaction.description)
            put("transactionDate", transaction.transactionDate.toString())
            put("source", transaction.source.name)
            put("createdAt", transaction.createdAt?.toString())
            put("smsCode", transaction.smsCode)
        }

    private fun transactionFromJson(json: JSONObject): Transaction =
        Transaction(
            id = json.getLong("id"),
            amount = json.getDouble("amount"),
            type = TransactionType.valueOf(json.getString("type")),
            categoryId = json.getLong("categoryId"),
            merchant = json.optNullableString("merchant"),
            description = json.optNullableString("description"),
            transactionDate = LocalDateTime.parse(json.getString("transactionDate")),
            source = TransactionSource.valueOf(json.getString("source")),
            createdAt = json.optNullableString("createdAt")?.let { LocalDateTime.parse(it) },
            smsCode = json.optNullableString("smsCode")
        )

    private fun budgetToJson(budget: Budget): JSONObject =
        JSONObject().apply {
            put("id", budget.id)
            put("categoryId", budget.categoryId)
            put("limit", budget.limit)
            put("month", budget.month.toString())
        }

    private fun budgetFromJson(json: JSONObject): Budget =
        Budget(
            id = json.getLong("id"),
            categoryId = json.getLong("categoryId"),
            limit = json.getDouble("limit"),
            month = YearMonth.parse(json.getString("month"))
        )

    // put(key, null) removes the key entirely rather than storing a JSON null,
    // so a missing key and an explicit null both mean "absent" on the way back.
    private fun JSONObject.optNullableString(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null
}
