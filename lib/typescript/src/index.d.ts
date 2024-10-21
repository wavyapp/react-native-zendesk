import { type EmitterSubscription } from 'react-native';
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
export declare enum ZendeskEvent {
    AUTHENTICATION_FAILED = "zendeskMessagingAuthenticationFailed",
    CLOSED = "zendeskMessagingClosed",
    CONVERSATION_ADDED = "zendeskMessagingConversationAdded",
    CONNECTION_STATUS_CHANGED = "zendeskMessagingConnectionStatusChanged",
    OPENED = "zendeskMessagingOpened",
    RECEIVED_MESSAGE = "zendeskMessagingReceivedMessage",
    SEND_MESSAGE_FAILED = "zendeskMessagingSendMessageFailed",
    UNREAD_COUNT_CHANGED = "zendeskMessagingUnreadCountChanged"
}
export type ZendeskUser = {
    id: string;
    externalId: string;
};
export declare function initializeSDK(channelKey: String): Promise<Boolean>;
export declare function logUserIn(JWT: string): Promise<ZendeskUser>;
export declare function logUserOut(): Promise<boolean>;
export declare function close(): Promise<Boolean>;
export declare function open(metadata?: Metadata | null): Promise<Boolean>;
export declare function addEventListener(event: ZendeskEvent.AUTHENTICATION_FAILED, callback: (error: string) => void): EmitterSubscription;
export declare function addEventListener(event: ZendeskEvent.CLOSED, callback: () => void): EmitterSubscription;
export declare function addEventListener(event: ZendeskEvent.OPENED, callback: () => void): EmitterSubscription;
export declare function addEventListener(event: ZendeskEvent.CONVERSATION_ADDED, callback: (id: string) => void): EmitterSubscription;
export declare function addEventListener(event: ZendeskEvent.CONNECTION_STATUS_CHANGED, callback: (status: string) => void): EmitterSubscription;
export declare function addEventListener(event: ZendeskEvent.RECEIVED_MESSAGE, callback: (data: Record<string, unknown>) => void): EmitterSubscription;
export declare function addEventListener(event: ZendeskEvent.SEND_MESSAGE_FAILED, callback: (error: string) => void): EmitterSubscription;
export declare function addEventListener(event: ZendeskEvent.UNREAD_COUNT_CHANGED, callback: (count: number) => void): EmitterSubscription;
export {};
//# sourceMappingURL=index.d.ts.map