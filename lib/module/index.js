import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
const LINKING_ERROR = `The package '@wavyapp/react-native-zendesk' doesn't seem to be linked. Make sure: \n\n` + Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n';
const ReactNativeZendesk = NativeModules.ReactNativeZendesk ? NativeModules.ReactNativeZendesk : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
const _nativeEventEmitter = new NativeEventEmitter(ReactNativeZendesk);
export let ZendeskEvent = /*#__PURE__*/function (ZendeskEvent) {
  ZendeskEvent["AUTHENTICATION_FAILED"] = "zendeskMessagingAuthenticationFailed";
  ZendeskEvent["CLOSED"] = "zendeskMessagingClosed";
  ZendeskEvent["CONVERSATION_ADDED"] = "zendeskMessagingConversationAdded";
  ZendeskEvent["CONNECTION_STATUS_CHANGED"] = "zendeskMessagingConnectionStatusChanged";
  ZendeskEvent["OPENED"] = "zendeskMessagingOpened";
  ZendeskEvent["RECEIVED_MESSAGE"] = "zendeskMessagingReceivedMessage";
  ZendeskEvent["SEND_MESSAGE_FAILED"] = "zendeskMessagingSendMessageFailed";
  ZendeskEvent["UNREAD_COUNT_CHANGED"] = "zendeskMessagingUnreadCountChanged";
  return ZendeskEvent;
}({});
export function initializeSDK(channelKey) {
  return ReactNativeZendesk.initializeSDK(channelKey);
}
export function logUserIn(JWT) {
  return ReactNativeZendesk.logUserIn(JWT);
}
export function logUserOut() {
  return ReactNativeZendesk.logUserOut();
}
export function close() {
  return ReactNativeZendesk.close();
}
export function open(metadata = null) {
  return ReactNativeZendesk.open(metadata);
}
export function addEventListener(event, callback) {
  return _nativeEventEmitter.addListener(event, callback);
}
//# sourceMappingURL=index.js.map