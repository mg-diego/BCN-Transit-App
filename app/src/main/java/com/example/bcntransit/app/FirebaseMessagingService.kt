package com.bcntransit.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM_SERVICE", "========================================")
        Log.d("FCM_SERVICE", "📱 MENSAJE RECIBIDO!")
        Log.d("FCM_SERVICE", "From: ${message.from}")
        Log.d("FCM_SERVICE", "Title: ${message.notification?.title}")
        Log.d("FCM_SERVICE", "Body: ${message.notification?.body}")
        Log.d("FCM_SERVICE", "Data: ${message.data}")
        Log.d("FCM_SERVICE", "========================================")

        message.notification?.let {
            showNotification(it.title ?: "BCN Transit", it.body ?: "Nueva notificación")
        }
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bcn_transit_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas BCN Transit",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de alertas de transporte"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Log.d("FCM_SERVICE", "✅ Notificación mostrada en pantalla")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "========================================")
        Log.d("FCM_SERVICE", "🔄 NUEVO TOKEN GENERADO")
        Log.d("FCM_SERVICE", "Token: $token")
        Log.d("FCM_SERVICE", "========================================")
        // Aquí deberías actualizar el token en tu backend
        // TODO: Llamar a tu API para actualizar el token
    }
}