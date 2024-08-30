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
import zendesk.chat.ChatConfiguration
import zendesk.chat.ChatEngine
import zendesk.chat.PushData
import zendesk.classic.messaging.MessagingActivity

class PushListenerService() : FirebaseMessagingService() {

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

        val chatConfig = ChatConfiguration.builder()
        chatConfig.withPreChatFormEnabled(false)

        val intent = MessagingActivity.builder().withEngines(ChatEngine.engine()).intent(
          applicationContext,
          chatConfig.build()
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val contentIntent = PendingIntent.getActivity(
          this,
          0,
          intent,
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
          builder.setContentTitle(pushData.author)
          builder.setContentText(pushData.message)
          EventsEmitter.instance.dispatchEvent("chatReceivedMessage", pushData)
        }

        else -> return
      }
      manager.notify(NOTIFICATION_ID, builder.build())
    } ?: {
      Log.d(LOG_TAG, "Notification manager not found")
    }
  }

  companion object {
    private const val LOG_TAG = "PushListenerService"

    private val NOTIFICATION_ID = System.currentTimeMillis().toInt()

    private const val NOTIFICATION_CHANNEL_ID = "ZendeskChat"
    private const val NOTIFICATION_CHANNEL_NAME = "Zendesk chat notifications"
  }
}
