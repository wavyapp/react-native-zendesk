package fr.wavyapp.reactNativeZendesk

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager

class ReactNativeZendeskPackage : ReactPackage {
  private var firebaseMessagingToken: String? = null

  override fun createViewManagers(
    context: ReactApplicationContext
  ): MutableList<ViewManager<View, ReactShadowNode<*>>> = mutableListOf()

  override fun createNativeModules(
    context: ReactApplicationContext
  ): MutableList<NativeModule> = listOf(ReactNativeZendeskModule(context, firebaseMessagingToken)).toMutableList()

  @Suppress("unused")
  fun setFirebaseMessagingToken(token: String?) {
    if (token != null) {
      firebaseMessagingToken = token
    }
  }
}