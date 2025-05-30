package com.pedro.AndroidAppSuperQuiz.ui.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pedro.AndroidAppSuperQuiz.MainActivity
import com.pedro.AndroidAppSuperQuiz.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Verifique se a mensagem contém uma carga útil de dados.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            remoteMessage.data["status"]?.let { status ->
                sendStatusNotification(status)
            }
        }

        // Verifique se a mensagem contém uma carga útil de notificação.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body)
        }
    }

    private fun sendStatusNotification(status: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "status_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Status do Pedido", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // TODO: Substituir a logica aqui
        val targetProgress = when (status) {
            "em preparo" -> 33
            "saindo para entrega" -> 66
            "entregue" -> 100
            else -> 0
        }

        // TODO: Substituir ícone e layout aqui, se desejar usar outro estilo para notificação de texto simples
        // TODO: Modifique o arquivo custom_notification e defina um novo icone de notificacao
        val remoteViews = RemoteViews(packageName, R.layout.custom_notification)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setCustomContentView(remoteViews)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)

        val notificationId = 1001
        var currentProgress = 0

        val handler = android.os.Handler(mainLooper)
        val updateRunnable = object : Runnable {
            override fun run() {
                if (currentProgress <= targetProgress) {
                    remoteViews.setTextViewText(R.id.status_text, "Pedido: $status")
                    remoteViews.setProgressBar(R.id.progress_bar, 100, currentProgress, false)
                    notificationManager.notify(notificationId, builder.build())
                    currentProgress++
                    handler.postDelayed(this, 50L) // atualiza a cada 50ms (~1.5s até 66)
                }
            }
        }

        handler.post(updateRunnable)

        // Cancela notificação automaticamente se chegar a 100 (opcional)
        if (targetProgress == 100) {
            handler.postDelayed({
                notificationManager.cancel(notificationId)
            }, 3000)
        }
    }



    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Enviar token para o seu servidor de aplicativos para gerenciar subscrições
    }

    private fun sendNotification(messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)


        // TODO: Substituir ícone aqui, se desejar usar outro estilo para notificação de texto simples
        val channelId = "fcm_default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal de notificação no Android 8.0 e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}