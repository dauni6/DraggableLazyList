package com.example.draggablelazylist.ui

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.draggablelazylist.R
import com.example.draggablelazylist.ReorderItem
import com.example.draggablelazylist.detectDragGesturesImmediately
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun DragDropList(
    items: List<ReorderItem>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val dragDropListState = rememberDragDropListState(onMove = onMove)

    LazyColumn(
        modifier = modifier,
        state = dragDropListState.lazyListState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item ->
            val isCurrentItem = index == dragDropListState.currentIndexOfDraggedItem
            val draggingModifier = if (isCurrentItem) {
                Modifier
                    .zIndex(1f)
                    .graphicsLayer {
                        translationY = dragDropListState.elementDisplacement ?: 0f
                    }
            } else {
                Modifier
                    .zIndex(0f)
                    .animateItemPlacement(
                        animationSpec = tween(
                            durationMillis = 300,
                        )
                    )
            }

            Row(
                modifier = draggingModifier
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_hamburger),
                    contentDescription = "hamburger button",
                    modifier = Modifier
                        .pointerInput(item.id) {
                            detectDragGesturesImmediately(
                                onDragStart = { offset ->
                                    dragDropListState.onDragStart(offset, item.id)
                                },
                                onDrag = { change, offset ->
                                    change.consume()
                                    dragDropListState.onDrag(offset)

                                    if (overscrollJob?.isActive == true)
                                        return@detectDragGesturesImmediately

                                    dragDropListState.checkForOverScroll()
                                        .takeIf { it != 0f }
                                        ?.let {
                                            Log.d("TEST", "scroll float = $it")
                                            overscrollJob = scope.launch {
                                                dragDropListState.lazyListState.scrollBy(it)
                                            }
                                        }
                                        ?: run { overscrollJob?.cancel() }
                                },
                                onDragEnd = { dragDropListState.onDragInterrupted() },
                                onDragCancel = { dragDropListState.onDragInterrupted() }
                            )
                        }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier.height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Item ${item.id}",
                        textAlign = TextAlign.Center,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = Color.Black,
            )
        }
    }
}