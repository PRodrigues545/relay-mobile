package ipca.api.relay.data.local

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

/**
 * Wrapper around socket.io that's safe to be re-entered as screens come and go.
 *
 * Important invariant: every `on(event, ...)` call MUST be preceded by `off(event)`.
 * The underlying socket.io client appends listeners — it doesn't replace them — so
 * naively calling `on(...)` from a screen's init each time it opens leaves dangling
 * handlers behind. Those keep firing forever, holding references to old ViewModels
 * (a leak) and producing duplicated message arrivals.
 */
object SocketManager {
    private var socket: Socket? = null

    private const val EVENT_NEW_MESSAGE = "new_message"
    private const val EVENT_MEETING_POINT = "meeting-point-update"

    fun connect(serverUrl: String, token: String) {
        if (socket?.connected() == true) return

        val options = IO.Options.builder()
            .setAuth(mapOf("token" to token))
            .setPath("/api/messaging/socket.io/")
            .build()

        socket = IO.socket(serverUrl, options)
        socket?.connect()
    }

    fun disconnect() {
        // Remove our listeners explicitly before disconnecting so they don't
        // survive a reconnect cycle on the same Socket instance.
        socket?.off(EVENT_NEW_MESSAGE)
        socket?.off(EVENT_MEETING_POINT)
        socket?.disconnect()
        socket = null
    }

    fun joinConversation(conversationId: String) {
        socket?.emit("join_conversation", JSONObject().put("conversationId", conversationId))
    }

    /**
     * Symmetric counterpart to [joinConversation] — call from MessagesViewModel.onCleared()
     * so the server can release any per-conversation state for this client. Calling
     * `off` here too means a fresh screen always starts with exactly one listener.
     */
    fun leaveConversation(conversationId: String) {
        socket?.emit("leave_conversation", JSONObject().put("conversationId", conversationId))
        socket?.off(EVENT_NEW_MESSAGE)
    }

    fun sendMessage(conversationId: String, body: String) {
        socket?.emit("send_message", JSONObject()
            .put("conversationId", conversationId)
            .put("body", body)
        )
    }

    fun onNewMessage(callback: (JSONObject) -> Unit) {
        // Replace, don't append. Two MessagesScreens never live concurrently,
        // so a single active handler is the right model.
        socket?.off(EVENT_NEW_MESSAGE)
        socket?.on(EVENT_NEW_MESSAGE) { args ->
            (args.firstOrNull() as? JSONObject)?.let { callback(it) }
        }
    }

    fun offNewMessage() {
        socket?.off(EVENT_NEW_MESSAGE)
    }

    fun isConnected() = socket?.connected() == true

    fun emitMeetingPoint(latitude: Double, longitude: Double) {
        socket?.emit(EVENT_MEETING_POINT, latitude, longitude)
    }

    /**
     * Replace the meeting-point listener. Before this change, opening the
     * MeetingPointScreen N times would register N listeners, all of which
     * would fire on every server push, each holding a reference to a
     * different (mostly dead) Composable scope.
     */
    fun onMeetingPointUpdated(callback: (Double, Double) -> Unit) {
        socket?.off(EVENT_MEETING_POINT)
        socket?.on(EVENT_MEETING_POINT) { args ->
            val latitude = args.getOrNull(0) as? Double ?: return@on
            val longitude = args.getOrNull(1) as? Double ?: return@on
            callback(latitude, longitude)
        }
    }

    /**
     * Detach the meeting-point listener. Call this from a DisposableEffect's
     * onDispose in MeetingPointScreen so the handler doesn't outlive the screen.
     */
    fun offMeetingPointUpdated() {
        socket?.off(EVENT_MEETING_POINT)
    }
}