package com.pesatrack.app.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.pesatrack.app.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Runs [SmsTransactionImporter] against each newly arrived SMS so a
 * transaction shows up without the user reopening the M-Pesa import screen.
 * The smsCode unique constraint (see TransactionRepository) means a later
 * manual re-scan never double-imports a message this receiver already
 * handled.
 *
 * Registered in the manifest with android:permission="BROADCAST_SMS" so only
 * the system (the only holder of that permission) can trigger it -- a third
 * party app can't spoof a fake SMS_RECEIVED broadcast at us.
 */
class MpesaSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages.first().originatingAddress.orEmpty()
        // A single logical SMS can arrive as multiple concatenated parts.
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }

        val appContext = context.applicationContext
        val importer = AppModule.provideSmsTransactionImporter(appContext)
        val parser = importer.resolveParser(sender) ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                importer.import(parser, body)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
