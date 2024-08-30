package fr.wavyapp.reactNativeZendesk

typealias Callback = (data: Any) -> Unit

class EventsEmitter {
  private val subscribers: MutableMap<String, MutableList<Callback>> = emptyMap<String, MutableList<Callback>>().toMutableMap()

  @Suppress("UNUSED")
  fun subscribe(event: String, callback: Callback): () -> Unit {
    subscribers[event] = subscribers.getOrDefault(event, emptyList<Callback>().toMutableList())
    subscribers[event]?.add(callback)

    return fun() {
      subscribers[event]?.remove(callback)
    }
  }

  @Suppress("unused")
  fun dispatchEvent(event: String, data: Any) {
    subscribers[event]?.forEach {
      it(data)
    }
  }

  companion object {
    val instance: EventsEmitter = EventsEmitter()
  }
}