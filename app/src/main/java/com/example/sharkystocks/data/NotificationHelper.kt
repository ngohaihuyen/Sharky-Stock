package com.example.sharkystocks.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sharkystocks.R

class NotificationHelper(private val context: Context) {
    private val channelId = "sharky_price_alerts"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sharky Stock Price Alerts"
            val descriptionText = "Thông báo cảnh báo giá cổ phiếu"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendPriceAlertNotification(symbol: String, currentPrice: Double, targetPrice: Double, isAbove: Boolean) {
        val direction = if (isAbove) "vượt mốc" else "giảm xuống mốc"
        val message = "Cổ phiếu $symbol vừa $direction target $${String.format("%.2f", targetPrice)} (Giá hiện tại: $${String.format("%.2f", currentPrice)})"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // hoặc icon sharky của bạn
            .setContentTitle("🚨 Sharky Alert: $symbol")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(symbol.hashCode(), builder.build())
    }
}
