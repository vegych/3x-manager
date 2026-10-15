package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.billing.BillingManager
import com.example.data.db.ServerEntity

object BillingNotificationManager {
    const val CHANNEL_ID = "billing_reminders"
    const val CHANNEL_NAME = "Напоминания об оплате серверов"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления за 7, 3 и 1 день до окончания срока оплаты или баланса сервера"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkAndNotify(
        context: Context,
        servers: List<ServerEntity>,
        enabled: Boolean,
        thresholdDays: Set<Int>
    ) {
        if (!enabled || servers.isEmpty()) return
        createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        servers.forEach { server ->
            val status = BillingManager.calculateStatus(server)
            val daysLeft = status.daysLeft ?: return@forEach

            // Trigger notification strictly if daysLeft matches one of thresholdDays configured by user (e.g. 7, 3, 1)
            if (thresholdDays.contains(daysLeft)) {
                val notifId = (server.id * 1000 + daysLeft).toInt()

                val title = if (daysLeft == 1) {
                    "⚠️ Завтра оплата: ${server.name}"
                } else {
                    "⏳ Напоминание об оплате: ${server.name}"
                }

                val text = if (daysLeft == 1) {
                    "Остался всего 1 день до окончания оплаты сервера «${server.name}»!"
                } else {
                    "До даты оплаты сервера «${server.name}» осталось $daysLeft дн."
                }

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    notifId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                try {
                    notificationManager.notify(notifId, notif)
                } catch (e: SecurityException) {
                    // Permission not granted
                }
            }
        }
    }
}
