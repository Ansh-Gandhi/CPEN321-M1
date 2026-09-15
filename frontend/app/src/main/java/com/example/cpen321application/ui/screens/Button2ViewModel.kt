package com.example.cpen321application.ui.screens

import android.graphics.Color as AndroidColor
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpen321application.network.PixelWebSocketClient
import com.example.cpen321application.network.WebSocketStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "Button2ViewModel"
const val GRID_SIZE = 16

data class PixelArtState(
    val grid: List<List<Color>> = List(GRID_SIZE) { List(GRID_SIZE) { Color.Transparent } },
    val status: WebSocketStatus = WebSocketStatus.DISCONNECTED,
    val pixelCount: Int = 0
)

class Button2ViewModel(private val webSocketClient: PixelWebSocketClient) : ViewModel() {
    private val _state = MutableStateFlow(PixelArtState())
    val state: StateFlow<PixelArtState> = _state.asStateFlow()

    init {
        // Observe WebSocket status
        viewModelScope.launch {
            webSocketClient.status.collect { status ->
                _state.value = _state.value.copy(status = status)
            }
        }

        // Observe incoming pixel stream
        viewModelScope.launch {
            webSocketClient.pixelUpdates.collect { update ->
                val parsedColor = parseHexColor(update.color)
                if (parsedColor != null) {
                    val currentGrid = _state.value.grid.map { it.toMutableList() }.toMutableList()
                    currentGrid[update.y][update.x] = parsedColor

                    _state.value = _state.value.copy(
                        grid = currentGrid.map { it.toList() },
                        pixelCount = _state.value.pixelCount + 1
                    )
                }
            }
        }
    }

    /**
     * Called when opening the Button 2 screen.
     * Clears canvas and opens a fresh WebSocket connection.
     */
    fun onScreenOpened() {
        webSocketClient.disconnect()
        clearCanvas()
        webSocketClient.connect()
    }

    /**
     * Called when leaving the Button 2 screen.
     * Instantly closes WebSocket connection to prevent background updates or ghost pixels.
     */
    fun onScreenClosed() {
        webSocketClient.disconnect()
        clearCanvas()
    }

    fun retryConnection() {
        webSocketClient.connect()
    }

    fun clearCanvas() {
        _state.value = _state.value.copy(
            grid = List(GRID_SIZE) { List(GRID_SIZE) { Color.Transparent } },
            pixelCount = 0
        )
    }

    private fun parseHexColor(hex: String): Color? {
        return try {
            val formatted = if (hex.startsWith("#")) hex else "#$hex"
            val colorInt = AndroidColor.parseColor(formatted)
            Color(colorInt)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse hex color: $hex", e)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}
