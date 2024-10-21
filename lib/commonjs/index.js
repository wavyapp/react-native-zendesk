"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.ZendeskEvent = void 0;
exports.addEventListener = addEventListener;
exports.close = close;
exports.initializeSDK = initializeSDK;
exports.logUserIn = logUserIn;
exports.logUserOut = logUserOut;
exports.open = open;
var _reactNative = require("react-native");
const LINKING_ERROR = `The package '@wavyapp/react-native-zendesk' doesn't seem to be linked. Make sure: \n\n` + _reactNative.Platform.select({
  ios: "- You have run 'pod install'\n",
  default: ''
}) + '- You rebuilt the app after installing the package\n';
const ReactNativeZendesk = _reactNative.NativeModules.ReactNativeZendesk ? _reactNative.NativeModules.ReactNativeZendesk : new Proxy({}, {
  get() {
    throw new Error(LINKING_ERROR);
  }
});
const _nativeEventEmitter = new _reactNative.NativeEventEmitter(ReactNativeZendesk);
let ZendeskEvent = exports.ZendeskEvent = /*#__PURE__*/function (ZendeskEvent) {
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
function initializeSDK(channelKey) {
  return ReactNativeZendesk.initializeSDK(channelKey);
}
function logUserIn(JWT) {
  return ReactNativeZendesk.logUserIn(JWT);
}
function logUserOut() {
  return ReactNativeZendesk.logUserOut();
}
function close() {
  return ReactNativeZendesk.close();
}
function open(metadata = null) {
  return ReactNativeZendesk.open(metadata);
}
function addEventListener(event, callback) {
  return _nativeEventEmitter.addListener(event, callback);
}
//# sourceMappingURL=index.js.map