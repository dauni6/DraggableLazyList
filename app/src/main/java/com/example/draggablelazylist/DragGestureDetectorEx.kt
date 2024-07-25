package com.example.draggablelazylist

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.util.fastForEach
import kotlin.coroutines.cancellation.CancellationException

suspend fun PointerInputScope.detectDragGesturesImmediately(
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
    awaitEachGesture {
        try {
            val down = awaitFirstDown(requireUnconsumed = false)
            onDragStart.invoke(down.position)

            if (
                drag(down.id) {
                    onDrag(it, it.positionChange())
                    it.consume()
                }
            ) {
                // consume up if we quit drag gracefully with the up
                currentEvent.changes.fastForEach {
                    if (it.changedToUp()) it.consume()
                }
                onDragEnd()
            } else {
                onDragCancel()
            }
        } catch (c: CancellationException) {
            onDragCancel()
            throw c
        }
    }
}