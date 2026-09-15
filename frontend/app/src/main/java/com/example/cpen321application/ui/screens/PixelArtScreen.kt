package com.example.cpen321application.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cpen321application.network.WebSocketStatus

/**
 * Button 2 Screen: Live Pixel Art Canvas (16x16 Grid)
 */
@Composable
fun PixelArtScreen(
    state: PixelArtState,
    onScreenOpened: () -> Unit,
    onScreenClosed: () -> Unit,
    onRetryConnection: () -> Unit,
    onClearCanvas: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Connect on entering screen, disconnect instantly on leaving screen
    DisposableEffect(Unit) {
        onScreenOpened()
        onDispose {
            onScreenClosed()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Live Pixel Stream",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "16x16 Grid Canvas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = state.status)
            }
        }

        // 16x16 Pixel Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(8.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val cellSize = canvasWidth / GRID_SIZE

                // Draw background pixels
                for (y in 0 until GRID_SIZE) {
                    for (x in 0 until GRID_SIZE) {
                        val color = state.grid.getOrNull(y)?.getOrNull(x) ?: Color.Transparent
                        if (color != Color.Transparent) {
                            drawRect(
                                color = color,
                                topLeft = Offset(x * cellSize, y * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }

                // Draw subtle grid lines
                val gridColor = Color.White.copy(alpha = 0.15f)
                for (i in 0..GRID_SIZE) {
                    val pos = i * cellSize
                    // Vertical grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(pos, 0f),
                        end = Offset(pos, canvasHeight),
                        strokeWidth = 1f
                    )
                    // Horizontal grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, pos),
                        end = Offset(canvasWidth, pos),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Pixel Stats & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pixels Received: ${state.pixelCount}",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClearCanvas) {
                    Text("Clear")
                }

                if (state.status == WebSocketStatus.DISCONNECTED || state.status == WebSocketStatus.ERROR) {
                    Button(onClick = onRetryConnection) {
                        Text("Reconnect")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: WebSocketStatus) {
    val (statusText, statusColor) = when (status) {
        WebSocketStatus.CONNECTED -> "Live Streaming" to Color(0xFF4CAF50)
        WebSocketStatus.CONNECTING -> "Connecting..." to Color(0xFFFF9800)
        WebSocketStatus.DISCONNECTED -> "Disconnected" to Color(0xFF9E9E9E)
        WebSocketStatus.ERROR -> "Error" to Color(0xFFF44336)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(statusColor, shape = CircleShape)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor
        )
    }
}
