import ChatSDK
import ChatProvidersSDK
import MessagingSDK
import ZendeskCoreSDK
import UserNotifications

@objc(ReactNativeZendesk)
class ReactNativeZendesk: RCTEventEmitter, UNUserNotificationCenterDelegate {
  var isInit = false
  
  override init() {
    super.init()
    let notificationCenter = UNUserNotificationCenter.current()
    notificationCenter.requestAuthorization(options: [.alert, .sound, .badge]) { allowed, _ in
      guard allowed else { return }
      notificationCenter.delegate = self
      DispatchQueue.main.async {
        UIApplication.shared.registerForRemoteNotifications()
      }
    }
  }
  
  override func supportedEvents() -> [String]!
  {
    return [
      "zendeskChatReceivedMessage",
    ]
  }
  
  func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
    let userInfo = notification.request.content.userInfo
    let application = UIApplication.shared
    Chat.didReceiveRemoteNotification(userInfo, in: application)
    
    if
      let notificationData = userInfo["data"] as? NSDictionary,
      let notificationType = notificationData["type"] as? String,
      notificationType == "zd.chat.msg",
      let aps = userInfo["aps"] as? NSDictionary,
      let alert = aps["alert"] as? NSDictionary,
      let agentName = alert["title"] as? String,
      let message = alert["body"] as? String
    {
      sendEvent(withName: "zendeskChatReceivedMessage", body: ["message": message, "agentName": agentName])
    }
    completionHandler([.alert, .sound, .badge])
  }
  
  func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) {
    Swift.print(response.actionIdentifier)
  }
  
  @objc
  func initialize(
    _ zendeskUrl: String,
    appId: String,
    clientId: String,
    chatAppId: String?,
    chatAccountKey: String?,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    Zendesk.initialize(appId: appId, clientId: clientId, zendeskUrl: zendeskUrl)
    
    if let chatAppIdNotNil = chatAppId, let chatAccountKeyNotNil = chatAccountKey {
      Chat.initialize(accountKey: chatAccountKeyNotNil, appId: chatAppIdNotNil)
    }
    
    isInit = true
    resolve(true)
  }
  
  @objc
  func identifyUser(
    _ identityTraits: [String:Any],
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    guard isInit else {
      return reject("IDENTIFY_USER_MUST_INIT_SDK", "Call the initialize method first", nil)
    }
    
    let identity = Identity.createAnonymous(
      name: identityTraits["name"] as? String,
      email: identityTraits["email"] as? String
    )
    
    Zendesk.instance?.setIdentity(identity)
    resolve(true)
  }
  
  @objc
  func openChat(
    _ userInfos: [String:Any]?,
    chatOpts: [String:Any]?,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    guard isInit else {
      return reject("OPEN_CHAT_MUST_INIT_SDK", "Call the initialize method first", nil)
    }
    let messagingConfig = MessagingConfiguration()
    let chatConfig = ChatConfiguration()
    
    if let notNilChatOpts = chatOpts {
      if let enableAgentAvailability = notNilChatOpts["enableAgentAvailability"] as? Bool {
        chatConfig.isAgentAvailabilityEnabled = enableAgentAvailability
      }
      if let enablePreChatForm = notNilChatOpts["enablePreChatForm"] as? Bool {
        chatConfig.isPreChatFormEnabled = enablePreChatForm
      }
      if let enableTranscript = notNilChatOpts["enableTranscript"] as? Bool {
        chatConfig.isChatTranscriptPromptEnabled = enableTranscript
      }
      if let enableOfflineForm = notNilChatOpts["enableOfflineForm"] as? Bool {
        chatConfig.isOfflineFormEnabled = enableOfflineForm
      }
    }
    
    if let notNilUserInfos = userInfos {
      let visitorInfo = VisitorInfo(
        name: notNilUserInfos["name"] as? String ?? "",
        email: notNilUserInfos["email"] as? String ?? "",
        phoneNumber: notNilUserInfos["phone"] as? String ?? ""
      )
      Chat.profileProvider?.setVisitorInfo(visitorInfo) { result in
        switch result {
          case .failure(let error):
            reject("OPEN_CHAT_FAILED_TO_SET_VISITOR_INFO", error.localizedDescription, error)
          default:
            break
        }
      }
      if let tags = notNilUserInfos["tags"] as? [String] {
        Chat.profileProvider?.addTags(tags)
      }
    }
    DispatchQueue.main.async {
      do {
        guard let presentedVc = RCTPresentedViewController() else {
          return reject("OPEN_CHAT_CANNOT_RETRIEVE_VC", "Could not retrieve the presented view controller", nil)
        }
        let chatEngine = try ChatEngine.engine()
        let navigationController = UINavigationController()
        let chatViewController = try Messaging.instance.buildUI(engines: [chatEngine], configs: [chatConfig, messagingConfig])
        navigationController.pushViewController(chatViewController, animated: true)
        presentedVc.present(navigationController, animated: true) {
          resolve(true)
        }
      } catch ChatError.chatIsNotInitialized {
        reject("OPEN_CHAT_MUST_INIT_CHAT", "Call the initialize method with appropriate parameters to initialize the chat first", nil)
      } catch {
        reject("OPEN_CHAT_UNKNOWN_ERROR", "Unknown error while trying to open the chat", error)
      }
    }
  }
}
