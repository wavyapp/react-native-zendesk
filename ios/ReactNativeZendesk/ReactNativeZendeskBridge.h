#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(ReactNativeZendesk, RCTEventEmitter)

RCT_EXTERN_METHOD(
                  initializeSDK: (NSString) channelKey
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  logUserIn: (NSString) JWT
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  logUserOut: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  close: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

RCT_EXTERN_METHOD(
                  open: (id) metadata
                  resolver: (RCTPromiseResolveBlock) resolve
                  rejecter: (RCTPromiseRejectBlock) rejecter
                  )

@end
