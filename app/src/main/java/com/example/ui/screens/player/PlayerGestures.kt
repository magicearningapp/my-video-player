package com.example.ui.screens.player

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun PlayerGestureDetector(
    isLocked: Boolean,
    onSingleTap: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit,
    onDoubleTapCenter: () -> Unit,
    onAdjustVolume: (delta: Float) -> Unit,
    onAdjustBrightness: (delta: Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var width by remember { mutableFloatStateOf(1f) }
    var height by remember { mutableFloatStateOf(1f) }
    var dragStartX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                width = coordinates.size.width.toFloat().coerceAtLeast(1f)
                height = coordinates.size.height.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(isLocked) {
                detectTapGestures(
                    onTap = {
                        onSingleTap()
                    },
                    onDoubleTap = { offset ->
                        if (!isLocked) {
                            val xRatio = offset.x / width
                            when {
                                xRatio < 0.35f -> onDoubleTapLeft()
                                xRatio > 0.65f -> onDoubleTapRight()
                                else -> onDoubleTapCenter()
                            }
                        }
                    }
                )
            }
            .pointerInput(isLocked) {
                if (!isLocked) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragStartX = offset.x
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Inverted delta: dragging up increases, dragging down decreases
                            val delta = -dragAmount.y / (height * 0.75f)
                            if (dragStartX < width * 0.5f) {
                                onAdjustBrightness(delta)
                            } else {
                                onAdjustVolume(delta)
                            }
                        }
                    )
                }
            }
    ) {
        content()
    }
}
