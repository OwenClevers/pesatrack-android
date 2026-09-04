package com.pesatrack.app.data.sms

import android.content.Context
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String
)

/**
 * Reads SMS messages from a given sender out of the device's SMS inbox
 * content provider. Requires the caller to already hold READ_SMS -- this
 * class does not request it.
 *
 * Queries by sender rather than owning any sender-specific logic itself, so
 * registering a new SmsParser (each of which declares its own
 * [SmsParser.senderPattern]) doesn't require any change here.
 */
class SmsReader(private val context: Context) {

    suspend fun readMessages(senderPattern: String): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY
        )

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            "${Telephony.Sms.ADDRESS} LIKE ?",
            arrayOf("%$senderPattern%"),
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)

            while (cursor.moveToNext()) {
                messages += SmsMessage(
                    id = cursor.getLong(idIndex),
                    sender = cursor.getString(addressIndex).orEmpty(),
                    body = cursor.getString(bodyIndex).orEmpty()
                )
            }
        }

        messages
    }
}
