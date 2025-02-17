import ZendeskSDK
import ZendeskSDKMessaging
import UserNotifications

@objc(ReactNativeZendesk)
class ReactNativeZendesk: RCTEventEmitter, UNUserNotificationCenterDelegate {
  var isInit = false
  var hasEventListeners = false
  
  class ZendeskMessagingViewController: UIViewController {
    var onDismiss: (() -> Void)?
    
    override func viewDidDisappear(_ animated: Bool) {
      super.viewDidDisappear(animated)
      onDismiss?()
    }
  }
  
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
  
  override func startObserving() {
    self.hasEventListeners = true
  }
  
  override func stopObserving() {
    self.hasEventListeners = false
  }
  
  override func sendEvent(withName name: String!, body: Any!) {
    if (self.hasEventListeners) {
      super.sendEvent(withName: name, body: body)
    }
  }
  
  override func supportedEvents() -> [String]!
  {
    return [
      "zendeskMessagingAuthenticationFailed",
      "zendeskMessagingConversationAdded",
      "zendeskMessagingConnectionStatusChanged",
      "zendeskMessagingReceivedMessage",
      "zendeskMessagingSendMessageFailed",
      "zendeskMessagingUnreadCountChanged",
      "zendeskMessagingClosed",
      "zendeskMessagingOpened",
    ]
  }
  
  func userNotificationCenter(
    _ center: UNUserNotificationCenter,
    willPresent notification: UNNotification,
    withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
  ) {
    let userInfo = notification.request.content.userInfo
    let shouldBeDisplayed = PushNotifications.shouldBeDisplayed(userInfo)
    
    switch shouldBeDisplayed {
      case .messagingShouldDisplay:
        if
          let message = userInfo["message"] as? Dictionary<AnyHashable, AnyHashable>,
          let agentName = message["name"] as? String,
          let content = message["text"] as? String
        {
          sendEvent(withName: "zendeskMessagingReceivedMessage", body: ["message": content, "agentName": agentName])
        }
        // This push belongs to Messaging and the SDK is able to display it to the end user
        if #available(iOS 14.0, *) {
          completionHandler([.banner, .sound, .badge])
        } else {
          completionHandler([.alert, .sound, .badge])
        }
      case .messagingShouldNotDisplay:
        // This push belongs to Messaging but it should not be displayed to the end user
        completionHandler([])
      case .notFromMessaging:
        // This push does not belong to Messaging
        // If not, just call the `completionHandler`
        completionHandler([.alert, .sound, .badge])
      @unknown default: break
    }
  }
  
  func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
    let shouldBeDisplayed = PushNotifications.shouldBeDisplayed(response.notification.request.content.userInfo)
    
    switch shouldBeDisplayed {
      case .messagingShouldDisplay:
        PushNotifications.handleTap(response.notification.request.content.userInfo) { viewController in
          DispatchQueue.main.async {
            guard let presentedVc = RCTPresentedViewController() else {
              return
            }
            guard let notNilViewController = viewController else {
              return
            }
            presentedVc.present(self.wrapZendeskVC(notNilViewController), animated: true) {
              self.sendEvent(withName: "zendeskMessagingOpened", body: nil)
            }
          }
        }
      default: break
    }
    completionHandler()
  }
  
  func wrapZendeskVC(_ vc: UIViewController) -> UINavigationController {
    let messagingVCWrapper = ZendeskMessagingViewController()
    
    messagingVCWrapper.addChild(vc);
    messagingVCWrapper.view.addSubview(vc.view)
    vc.didMove(toParent: messagingVCWrapper)
    messagingVCWrapper.onDismiss = {
      self.sendEvent(withName: "zendeskMessagingClosed", body: nil)
    }
    
    return UINavigationController(rootViewController: messagingVCWrapper)
  }
  
  func addZendeskEventsObservers() {
    Zendesk.instance?.addEventObserver(self) { event in
      switch event {
        case .unreadMessageCountChanged(let unreadCount):
          self.sendEvent(withName: "zendeskMessagingUnreadCountChanged", body: unreadCount)
        case .authenticationFailed(let error as NSError):
          self.sendEvent(withName: "zendeskMessagingAuthenticationFailed", body: error.localizedDescription)
        case .conversationAdded(conversationId: let conversationId):
          self.sendEvent(withName: "zendeskMessagingConversationAdded", body: conversationId)
        case .connectionStatusChanged(connectionStatus: let connectionStatus):
          self.sendEvent(withName: "zendeskMessagingConnectionStatusChanged", body: connectionStatus)
        case .sendMessageFailed(let error as NSError):
          self.sendEvent(withName: "zendeskMessagingSendMessageFailed", body: error.localizedDescription)
        @unknown default:
          break
      }
    }
  }
  
  @objc
  func initializeSDK(
    _ channelKey: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    Zendesk.initialize(withChannelKey: channelKey,messagingFactory: DefaultMessagingFactory()) { result in
      if case let .failure(error) = result {
        reject("FAILED_TO_INIT_MESSAGING_SDK", error.localizedDescription, nil)
      } else {
        self.isInit = true
        self.addZendeskEventsObservers()
        resolve(true)
      }
    }
  }
  
  @objc
  func logUserIn(
    _ JWT: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    guard isInit else {
      return reject("LOG_USER_IN_MUST_INIT_SDK", "Call the initialize method first", nil)
    }
    Zendesk.instance?.loginUser(with: JWT) { result in
      switch result {
        case .success(let user):
          resolve([
            "id": user.id,
            "externalId": user.externalId,
          ]);
        case .failure(let error):
          reject("LOGIN_USER_IN_FAILURE", error.localizedDescription, nil)
      }
    }
  }
  
  @objc
  func logUserOut(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    guard isInit else {
      return reject("LOG_USER_OUT_MUST_INIT_SDK", "Call the initialize method first", nil)
    }
    Zendesk.instance?.logoutUser { result in
      switch result {
        case .success():
          resolve(true);
        case .failure(let error):
          reject("LOGIN_USER_OUT_FAILURE", error.localizedDescription, nil)
      }
    }
  }
  
  @objc
  func close(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    DispatchQueue.main.async {
      guard let presentedVc = RCTPresentedViewController() else {
        return reject("CLOSE_CANNOT_RETRIEVE_VC", "Could not retrieve the presented view controller", nil)
      }
      presentedVc.dismiss(animated: true) {
        self.sendEvent(withName: "zendeskMessagingClosed", body: nil)
        resolve(true)
      }
    }
  }
  
  @objc
  func open(
    _ metadata: [String:Any]?,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    guard isInit else {
      return reject("OPEN_MUST_INIT_SDK", "Call the initialize method first", nil)
    }
    
    if let notNilMetadata = metadata {
      if let tags = notNilMetadata["tags"] as? [String] {
        Zendesk.instance?.messaging?.setConversationTags(tags)
      }
      if let conversationFields = notNilMetadata["fields"] as? [[String: AnyHashable]] {
        let conversationFieldsFormatIsValid = conversationFields.allSatisfy { fieldData in fieldData["id"] is String && fieldData["value"] != nil }
        
        guard conversationFieldsFormatIsValid else {
          return reject("OPEN_MUST_MALFORMED_CONVERSATION_FIELDS", "`fields` parameter should be of the form [{ id: string, value: String | number | boolean}]", nil)
        }
        let conversationFieldsDict = Dictionary(
          conversationFields.map { fieldData in (fieldData["id"] as! String, fieldData["value"]!)},
          uniquingKeysWith: { (_, last) in last }
        )
        Zendesk.instance?.messaging?.setConversationFields(conversationFieldsDict)
      }
    }
    DispatchQueue.main.async {
      guard let presentedVc = RCTPresentedViewController() else {
        return reject("OPEN_CANNOT_RETRIEVE_VC", "Could not retrieve the presented view controller", nil)
      }
      guard let messagingViewController = Zendesk.instance?.messaging?.messagingViewController() else {
        return reject("OPEN_CANNOT_MESSAGING_VC", "Could not retrieve zendesk messaging view controller", nil)
      }
      
      presentedVc.present(self.wrapZendeskVC(messagingViewController), animated: true) {
        self.sendEvent(withName: "zendeskMessagingOpened", body: nil)
        resolve(true)
      }
    }
  }
}
