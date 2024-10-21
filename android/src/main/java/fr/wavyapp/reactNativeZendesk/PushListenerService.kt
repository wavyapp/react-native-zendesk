package fr.wavyapp.reactNativeZendesk

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import zendesk.messaging.android.push.PushNotifications
import zendesk.messaging.android.push.PushResponsibility.MESSAGING_SHOULD_DISPLAY
import zendesk.messaging.android.push.PushResponsibility.MESSAGING_SHOULD_NOT_DISPLAY
import zendesk.messaging.android.push.PushResponsibility.NOT_FROM_MESSAGING

class PushListenerService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    PushNotifications.updatePushNotificationToken(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    val shouldDisplay = PushNotifications.shouldBeDisplayed(remoteMessage.data)

    when (shouldDisplay) {
      MESSAGING_SHOULD_DISPLAY -> {
        PushNotifications.setNotificationSmallIconId(R.mipmap.ic_launcher)
        PushNotifications.displayNotification(context = this, messageData = remoteMessage.data)
      }
      MESSAGING_SHOULD_NOT_DISPLAY -> {}
      NOT_FROM_MESSAGING -> {

      }
    }
  }
}
