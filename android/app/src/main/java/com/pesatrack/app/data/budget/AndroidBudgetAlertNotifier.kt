package com.pesatrack.app.data.budget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pesatrack.app.MainActivity
import com.pesatrack.app.R
import com.pesatrack.app.core.Constants
import com.pesatrack.app.domain.model.BudgetThreshold
import com.pesatrack.app.domain.model.Category

class AndroidBudgetAlertNotifier(private val context: Context) : BudgetAlertNotifier {

    override fun notify(category: Category, threshold: BudgetThreshold, percent: Int): Boolean {
        ensureChannel()

        // POST_NOTIFICATIONS (API 33+) may have been denied -- checkSelfPermission
        // for it returns GRANTED on older APIs where the permission doesn't apply,
        // so this one check covers every API level without branching on SDK_INT.
        // Silently skipping (not throwing, not nagging) is what keeps the app fully
        // functional when the user denies it.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val title = when (threshold) {
            BudgetThreshold.WARNING -> "${category.name} budget at $percent%"
            BudgetThreshold.EXCEEDED -> "${category.name} budget exceeded"
        }
        val text = when (threshold) {
            BudgetThreshold.WARNING -> "You've used $percent% of your ${category.name} budget this month."
            BudgetThreshold.EXCEEDED -> "You've spent $percent% of your ${category.name} budget this month."
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(Constants.EXTRA_NAVIGATE_TO, Constants.NAVIGATE_TARGET_BUDGETS)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            notificationId(category.id, threshold),
            contentIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId(category.id, threshold), notification)
        return true
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Budget alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts when a category's spending crosses a budget threshold."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Distinct per category+threshold so a warning and an exceeded alert for
    // the same category don't overwrite each other in the notification shade.
    private fun notificationId(categoryId: Long, threshold: BudgetThreshold): Int =
        (categoryId * 10 + threshold.ordinal).toInt()

    companion object {
        const val CHANNEL_ID = "budget_alerts"
    }
}
