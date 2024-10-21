import { NativeEventEmitter, NativeModules, Platform, type EmitterSubscription } from 'react-native';

const LINKING_ERROR =
  `The package '@wavyapp/react-native-zendesk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n';

const ReactNativeZendesk = NativeModules.ReactNativeZendesk
  ? NativeModules.ReactNativeZendesk
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

const _nativeEventEmitter = new NativeEventEmitter(ReactNativeZendesk);

type ConversationField = {
  id: string;
  value: number | string;
};

export type Metadata = {
  tags?: string[];
  fields?: ConversationField[];
};

export type ChatOptions = {
  enableAgentAvailability?: Boolean;
  enablePreChatForm?: Boolean;
  enableTranscript?: Boolean;
  enableOfflineForm?: Boolean;
};

export enum ZendeskEvent {
  AUTHENTICATION_FAILED = "zendeskMessagingAuthenticationFailed",
  CLOSED = "zendeskMessagingClosed",
  CONVERSATION_ADDED = "zendeskMessagingConversationAdded",
  CONNECTION_STATUS_CHANGED = "zendeskMessagingConnectionStatusChanged",
  OPENED = "zendeskMessagingOpened",
  RECEIVED_MESSAGE = "zendeskMessagingReceivedMessage",
  SEND_MESSAGE_FAILED = "zendeskMessagingSendMessageFailed",
  UNREAD_COUNT_CHANGED = "zendeskMessagingUnreadCountChanged",
}

export type ZendeskUser = {
  id: string;
  externalId: string;
};

export function initializeSDK(channelKey: String): Promise<Boolean> {
  return ReactNativeZendesk.initializeSDK(channelKey);
}

export function logUserIn(JWT: string): Promise<ZendeskUser> {
  return ReactNativeZendesk.logUserIn(JWT);
}

export function logUserOut(): Promise<boolean> {
  return ReactNativeZendesk.logUserOut();
}

export function close(): Promise<Boolean> {
  return ReactNativeZendesk.close();
}

export function open(
  metadata: Metadata | null = null,
): Promise<Boolean> {
  return ReactNativeZendesk.open(metadata);
}

export function addEventListener(event: ZendeskEvent.AUTHENTICATION_FAILED, callback: (error: string) => void): EmitterSubscription;
export function addEventListener(event: ZendeskEvent.CLOSED, callback: () => void): EmitterSubscription;
export function addEventListener(event: ZendeskEvent.OPENED, callback: () => void): EmitterSubscription;
export function addEventListener(event: ZendeskEvent.CONVERSATION_ADDED, callback: (id: string) => void): EmitterSubscription;
export function addEventListener(event: ZendeskEvent.CONNECTION_STATUS_CHANGED, callback: (status: string) => void): EmitterSubscription;
export function addEventListener(event: ZendeskEvent.RECEIVED_MESSAGE, callback: (data: Record<string, unknown>) => void): EmitterSubscription;
export function addEventListener(event: ZendeskEvent.SEND_MESSAGE_FAILED, callback: (error: string) => void): EmitterSubscription;
export function addEventListener(event: ZendeskEvent.UNREAD_COUNT_CHANGED, callback: (count: number) => void): EmitterSubscription;
export function addEventListener(
  event:
    ZendeskEvent.AUTHENTICATION_FAILED |
    ZendeskEvent.CLOSED |
    ZendeskEvent.CONVERSATION_ADDED |
    ZendeskEvent.CONNECTION_STATUS_CHANGED |
    ZendeskEvent.OPENED |
    ZendeskEvent.RECEIVED_MESSAGE |
    ZendeskEvent.SEND_MESSAGE_FAILED |
    ZendeskEvent.UNREAD_COUNT_CHANGED,
  callback:
    ((data: Record<string, unknown>) => void) |
    ((data: string) => void) |
    ((data: number) => void)
): EmitterSubscription {
  return _nativeEventEmitter.addListener(event, callback)
}