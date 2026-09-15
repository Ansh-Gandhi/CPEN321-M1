package com.example.cpen321application.network

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

private const val TAG = "PixelWebSocket"

@JsonClass(generateAdapter = true)
data class PixelUpdate(
    val x: Int,
    val y: Int,
    val color: String
)

enum class WebSocketStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class PixelWebSocketClient(private val baseUrl: String) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val pixelAdapter = moshi.adapter(PixelUpdate::class.java)

    private val _pixelUpdates = MutableSharedFlow<PixelUpdate>(extraBufferCapacity = 128)
    val pixelUpdates: SharedFlow<PixelUpdate> = _pixelUpdates.asSharedFlow()

    private val _status = MutableStateFlow(WebSocketStatus.DISCONNECTED)
    val status: StateFlow<WebSocketStatus> = _status.asStateFlow()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect() {
        // Close any existing connection first to ensure a clean state
        disconnect()

        _status.value = WebSocketStatus.CONNECTING

        val wsUrl = baseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .removeSuffix("/") + "/ws/pixels"

        Log.d(TAG, "Connecting to WebSocket: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected successfully.")
                _status.value = WebSocketStatus.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val update = pixelAdapter.fromJson(text)
                    if (update != null && update.x in 0..15 && update.y in 0..15) {
                        scope.launch {
                            _pixelUpdates.emit(update)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse pixel payload: $text", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code / $reason")
                _status.value = WebSocketStatus.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code / $reason")
                _status.value = WebSocketStatus.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}", t)
                _status.value = WebSocketStatus.ERROR
            }
        })
    }

    fun disconnect() {
        try {
            webSocket?.cancel() // Instantly terminate the active socket connection
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting socket", e)
        }
        webSocket = null
        _status.value = WebSocketStatus.DISCONNECTED
    }
}
