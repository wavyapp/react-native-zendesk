#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(ReactNativeZendesk, RCTEventEmitter)

RCT_EXTERN_METHOD(
                  initialize: (NSString) zendeskUrl
                  appId: (NSString) appId
                  clientId: (NSString) clientId
                  chatAppId: (NSString) chatAppId
                  chatAccountKey: (NSString) chatAccountKey
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  identifyUser: (id) identityTraits
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  openChat: (id) userInfos
                  chatOpts: (id) chatOpts
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

@end
