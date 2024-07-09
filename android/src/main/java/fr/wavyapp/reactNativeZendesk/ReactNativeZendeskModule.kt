package fr.wavyapp.reactNativeZendesk

import android.content.Intent
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.zendesk.service.ErrorResponse
import com.zendesk.service.ZendeskCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import zendesk.chat.Chat
import zendesk.chat.ChatConfiguration
import zendesk.chat.ChatEngine
import zendesk.chat.VisitorInfo
import zendesk.classic.messaging.MessagingActivity
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.Support

class ReactNativeZendeskModule(reactContext: ReactApplicationContext, private val firebaseMessagingToken: String?) : ReactContextBaseJavaModule(reactContext) {
  private var isInit = false
  private val zendeskCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  override fun getName(): String {
    return "ReactNativeZendesk"
  }

  @Suppress("unused")
  @ReactMethod
  fun initialize(
    zendeskUrl: String,
    appId: String,
    clientId: String,
    chatAppId: String?,
    chatAccountKey: String?,
    promise: Promise
  ) {
    zendeskCoroutineScope.launch {
      Zendesk.INSTANCE.init(reactApplicationContext, zendeskUrl, appId, clientId)
      Support.INSTANCE.init(Zendesk.INSTANCE)

      if (chatAppId != null && chatAccountKey != null) {
        Chat.INSTANCE.init(reactApplicationContext, chatAccountKey, chatAppId)
        firebaseMessagingToken?.let {
          Chat.INSTANCE.providers()?.
            pushNotificationsProvider()?.
            registerPushToken(firebaseMessagingToken)
        }
      }
      isInit = true
      promise.resolve(true)
    }
  }

  @Suppress("unused")
  @ReactMethod
  fun identifyUser(identityTraits: ReadableMap, promise: Promise) {
    if (!isInit) {
      promise.reject("OPEN_CHAT_MUST_INIT_SDK", "Call the initChat method first", null)
    }
    val identity = AnonymousIdentity.Builder()

    identityTraits.getString("name")?.let { identity.withNameIdentifier(it) }
    identityTraits.getString("email")?.let { identity.withEmailIdentifier(it) }

    Zendesk.INSTANCE.setIdentity(identity.build())
    promise.resolve(true)
  }

  @Suppress("unused")
  @ReactMethod
  fun openChat(
    userInfos: ReadableMap?,
    chatOpts: ReadableMap?,
    promise: Promise
  ) {
    if (!isInit) {
      promise.reject("OPEN_CHAT_MUST_INIT_SDK", "Call the initialize method first", null)
    }

    userInfos?.let {chatUserInfos ->
      val visitorInfo = VisitorInfo.builder()
      chatUserInfos.getString("name")?.let { visitorInfo.withName(it) }
      chatUserInfos.getString("email")?.let { visitorInfo.withEmail(it) }
      chatUserInfos.getString("phone")?.let { visitorInfo.withPhoneNumber(it) }

      val chatProfileProvider = Chat.INSTANCE.providers()?.profileProvider()

      chatProfileProvider?.setVisitorInfo(visitorInfo.build(), object : ZendeskCallback<Void>() {
        override fun onError(error: ErrorResponse?) {
          promise.reject("OPEN_CHAT_FAILED_TO_SET_VISITOR_INFO", error.toString())
        }
        override fun onSuccess(result: Void?) {
          // do nothing
        }
      })

      chatUserInfos.getArray("tags")?.let {
        if (it.size() > 0) {
          chatProfileProvider?.addVisitorTags(
            it.toArrayList().filterIsInstance<String>().toMutableList(),
            object : ZendeskCallback<Void>() {
              override fun onError(error: ErrorResponse?) {
                promise.reject("OPEN_CHAT_FAILED_TO_SET_VISITOR_TAGS", error.toString())
              }
              override fun onSuccess(result: Void?) {
                // do nothing
              }
            }
          )
        }
      }
    }
    val chatConfig = ChatConfiguration.builder()

    chatOpts?.let {
      if (chatOpts.hasKey("enableAgentAvailability")) {
        chatConfig.withAgentAvailabilityEnabled(
          chatOpts.getBoolean("enableAgentAvailability")
        )
      }
      if (chatOpts.hasKey("enablePreChatForm")) {
        chatConfig.withPreChatFormEnabled(
          chatOpts.getBoolean("enablePreChatForm")
        )
      }
      if (chatOpts.hasKey("enableTranscript")) {
        chatConfig.withTranscriptEnabled(
          chatOpts.getBoolean("enableTranscript")
        )
      }
      if (chatOpts.hasKey("enableOfflineForm")) {
        chatConfig.withOfflineFormEnabled(
          chatOpts.getBoolean("enableOfflineForm")
        )
      }
    }

    val intent = MessagingActivity.builder().withEngines(ChatEngine.engine()).intent(
      reactApplicationContext,
      chatConfig.build()
    )

    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    reactApplicationContext.startActivity(intent)
    promise.resolve(true)
  }
}