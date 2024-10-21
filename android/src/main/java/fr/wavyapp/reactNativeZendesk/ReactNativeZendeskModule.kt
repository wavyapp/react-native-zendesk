package fr.wavyapp.reactNativeZendesk

import android.app.Activity
import android.app.Application
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import zendesk.android.Zendesk
import zendesk.android.ZendeskResult
import zendesk.android.events.ZendeskEvent
import zendesk.android.events.ZendeskEventListener
import zendesk.messaging.android.DefaultMessagingFactory

class ReactNativeZendeskModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private var isInit = false
  private val zendeskCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var listeners = 0

  override fun initialize() {
    super.initialize()

    reactApplicationContext.currentActivity?.application?.registerActivityLifecycleCallbacks(object: Application.ActivityLifecycleCallbacks {
      override fun onActivityPaused(activity: Activity) {
      }

      override fun onActivityStarted(activity: Activity) {
        if (activity.localClassName == "zendesk.messaging.android.internal.conversationscreen.ConversationActivity") {
          sendEvent("zendeskMessagingOpened")
        }
      }

      override fun onActivityDestroyed(activity: Activity) {
        // noop
      }

      override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
        // noop
      }

      override fun onActivityStopped(activity: Activity) {
        if (activity.localClassName == "zendesk.messaging.android.internal.conversationscreen.ConversationActivity") {
          sendEvent("zendeskMessagingClosed")
        }
      }

      override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        // noop
      }

      override fun onActivityResumed(activity: Activity) {
        // noop
      }
    })
  }

  private fun addZendeskEventsObservers() {
    val eventListener = ZendeskEventListener {
      event -> when (event) {
        is ZendeskEvent.AuthenticationFailed -> sendEvent("zendeskMessagingAuthenticationFailed")
        is ZendeskEvent.ConversationAdded -> sendEvent("zendeskMessagingConversationAdded")
        is ZendeskEvent.ConnectionStatusChanged -> sendEvent("zendeskMessagingConnectionStatusChanged")
        is ZendeskEvent.FieldValidationFailed -> {}
        is ZendeskEvent.SendMessageFailed -> sendEvent("zendeskMessagingSendMessageFailed")
        is ZendeskEvent.UnreadMessageCountChanged -> sendEvent("zendeskMessagingUnreadCountChanged", event.currentUnreadCount)
      }
    }
    Zendesk.instance.addEventListener(eventListener)
  }

  override fun getName(): String {
    return "ReactNativeZendesk"
  }

  @Suppress("unused", "UNUSED_PARAMETER")
  @ReactMethod
  fun addListener(eventName: String) {
    listeners++
  }

  @Suppress("unused")
  @ReactMethod
  fun removeListeners(count: Int) {
    if (listeners > 1) {
      listeners -= count
    } else {
      listeners = 0
    }
  }

  @Suppress("unused")
  @ReactMethod
  fun initializeSDK(
    channelKey: String,
    promise: Promise
  ) {
    zendeskCoroutineScope.launch {
      val init = Zendesk.initialize(
        context = reactApplicationContext,
        channelKey = channelKey,
        messagingFactory = DefaultMessagingFactory(),
      )

      when (init) {
        is ZendeskResult.Success -> {
          isInit = true
          addZendeskEventsObservers()
          promise.resolve(true)
        }
        is ZendeskResult.Failure -> promise.reject("FAILED_TO_INIT_MESSAGING_SDK", init.error)
      }
    }
  }

  @Suppress("unused")
  @ReactMethod
  fun logUserIn(JWT: String, promise: Promise) {
    if (!isInit) {
      promise.reject("LOG_USER_IN_MUST_INIT_SDK", "Call the initialize method first", null)
    }

    zendeskCoroutineScope.launch {
      when (val loginResult = Zendesk.instance.loginUser(JWT)) {
        is ZendeskResult.Success -> promise.resolve(true)
        is ZendeskResult.Failure -> promise.reject("LOGIN_USER_IN_FAILURE", loginResult.error)
      }
    }
  }

  @Suppress("unused")
  @ReactMethod
  fun logUserOut(promise: Promise) {
    if (!isInit) {
      promise.reject("LOG_USER_OUT_MUST_INIT_SDK", "Call the initialize method first", null)
    }

    zendeskCoroutineScope.launch {
      when (val logoutResult = Zendesk.instance.logoutUser()) {
        is ZendeskResult.Success -> promise.resolve(true)
        is ZendeskResult.Failure -> promise.reject("LOGIN_USER_OUT_FAILURE", logoutResult.error)
      }
    }
  }

  @Suppress("unused")
  @ReactMethod
  fun close(promise: Promise) {
    // Just resolve the promise without doing anything since when the conversation activity is started the JS is not executed, so this methods can't be called anyway
    promise.resolve(true)
  }

  @Suppress("unused")
  @ReactMethod
  fun open(
    metadata: ReadableMap?,
    promise: Promise
  ) {
    if (!isInit) {
      promise.reject("OPEN_MUST_INIT_SDK", "Call the initialize method first", null)
    }

    metadata?.let { notNullMetadata ->
      val tags = notNullMetadata.getArray("tags")?.toArrayList()?.filterIsInstance<String>()
      tags?.let { Zendesk.instance.messaging.setConversationTags(it) }

      val conversationFields = notNullMetadata.getArray("fields")?.toArrayList()

      conversationFields?.filterIsInstance<Map<String,*>>()?.let { notNullConversationFields ->
        val conversationFieldsFormatIsValid = notNullConversationFields.all {
          it.contains("id") && it.contains("value") && it["id"] is String && it["value"] != null
        }
        if (!conversationFieldsFormatIsValid) {
          return promise.reject("OPEN_MUST_MALFORMED_CONVERSATION_FIELDS", "`fields` parameter should be of the form [{ id: string, value: String | number | boolean}]")
        }

        Zendesk.instance.messaging.setConversationFields(notNullConversationFields.associate { Pair(it["id"] as String, it["value"] as Any) })
      }
    }

    Zendesk.instance.messaging.showMessaging(reactApplicationContext, FLAG_ACTIVITY_NEW_TASK)

    promise.resolve(true)
  }

  private fun sendEvent(name: String, body: Any? = null) {
    if (listeners > 0) {
      reactApplicationContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(name, body)
    }
  }
}
