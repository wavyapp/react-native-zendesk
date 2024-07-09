package fr.wavyapp.reactNativeZendesk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import zendesk.chat.Chat
import zendesk.chat.PushData
import zendesk.classic.messaging.MessagingActivity

class PushListenerService : FirebaseMessagingService() {
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
    val zendeskChatPushNotificationProvider = Chat.INSTANCE.providers()?.pushNotificationsProvider()

    manager?.let {
      val builder =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
          .setSmallIcon(R.mipmap.ic_launcher)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
          NOTIFICATION_CHANNEL_ID,
          NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )

        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 100, 200)

        builder.setChannelId(NOTIFICATION_CHANNEL_ID)

        val contentIntent = PendingIntent.getActivity(
          this,
          0,
          Intent(this, MessagingActivity::class.java),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(contentIntent)
        manager.createNotificationChannel(channel)
      }

      val pushData = zendeskChatPushNotificationProvider?.processPushNotification(remoteMessage.data)

      when (pushData?.type) {
        PushData.Type.END -> {
          builder.setContentTitle("Chat ended")
          builder.setContentText("The chat has ended!")
        }

        PushData.Type.MESSAGE -> {
          builder.setContentTitle("Chat message")
          builder.setContentText("New chat message!")
        }

        else -> return
      }
      manager.notify(NOTIFICATION_ID, builder.build())

      // IMPORTANT! forward the notification data to the SDK
      //ZopimChatApi.onMessageReceived(pushData)
    } ?: {
      Log.d(LOG_TAG, "Notification manager not found")
    }
  }

  companion object {
    private const val LOG_TAG = "PushListenerService"

    private val NOTIFICATION_ID = System.currentTimeMillis().toInt()

    private const val NOTIFICATION_CHANNEL_ID = "ChatSampleAppChannelId"
    private const val NOTIFICATION_CHANNEL_NAME = "ChatSampleAppChannelId"
  }
}
