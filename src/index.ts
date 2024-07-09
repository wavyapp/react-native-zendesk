import { /*NativeEventEmitter, */NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-zendesk' doesn't seem to be linked. Make sure: \n\n` +
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

export type InitOptions = {
  zendeskUrl: string;
  appId: string;
  clientId: string;
  chatAppId?: string;
  chatAccountKey?: string;
};

export type UserIdentityTraits = {
  name?: string;
  email?: string;
};

export type ChatVisitorInfo = UserIdentityTraits & {
  phone?: string;
  tags?: string[];
};

export type ChatOptions = {
  enableAgentAvailability?: Boolean;
  enablePreChatForm?: Boolean;
  enableTranscript?: Boolean;
  enableOfflineForm?: Boolean;
};

export function initialize(opts: InitOptions): Promise<Boolean> {
  return ReactNativeZendesk.initialize(opts.zendeskUrl, opts.appId, opts.clientId, opts.chatAppId || null, opts.chatAccountKey || null);
}

export function identifyUser(traits: UserIdentityTraits): Promise<Boolean> {
  return ReactNativeZendesk.identifyUser(traits);
}

export function openChat(
  visitorInfo: ChatVisitorInfo | null = null,
  chatOpts: ChatOptions | null = null,
): Promise<Boolean> {
  return ReactNativeZendesk.openChat(visitorInfo, chatOpts);
}