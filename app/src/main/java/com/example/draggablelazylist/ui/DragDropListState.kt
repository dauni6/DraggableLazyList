package com.example.draggablelazylist.ui

import android.util.Log
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LookaheadScope
import com.example.draggablelazylist.getVisibleItemInfoFor
import com.example.draggablelazylist.offsetEnd
import kotlinx.coroutines.Job

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit,
): DragDropListState {
    return remember { DragDropListState(lazyListState = lazyListState, onMove = onMove) }
}

class DragDropListState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    private var draggedDistance by mutableFloatStateOf(0f)

    // used to obtain initial offsets on drag start
    // (드래그 시작 시점의 정보를 유지, 따라서 onDragStart에서만 1번 호출, 주로 elementDisplacement(변위) 계산의 기준점으로 사용)
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    private val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { Pair(it.offset, it.offsetEnd) }

    // 드래그 중인 아이템의 현재 위치와 원래 위치 사이의 차이
    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem
            ?.let { lazyListState.getVisibleItemInfoFor(absoluteIndex = it) } // 현재 드래그 중인 아이템 정보 가져오기
            ?.let { item ->
                // (initiallyDraggedElement?.offset ?: 0f).toFloat()는 드래그를 시작했을 때의 아이템 초기 위치. 없으면 0f
                // draggedDistance는 내가 드래그한 거리
                (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }

    // 현재 시점의 정보를 나타냄. onDrag()를 통해 계속 바뀜
    private val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let {
            lazyListState.getVisibleItemInfoFor(absoluteIndex = it)
        }

    private var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                // offset.y는 사용자가 터치한 지점의 y좌표
                // item.offset은 아이템의 상단 위치
                // item.offset + item.size은 아이템의 하단 위치
                // 따라서 아래의 코드는 "사용자가 터치한 y좌표가 현재 아이템의 상단과 하단 사이에 있는가?"를 확인하는 것
                Log.d("TEST", "onDragStart | item.offset = ${item.offset}, item.size = ${item.size}")
                offset.y.toInt() in item.offset..(item.offset + item.size)
            }
            ?.also {
                Log.d("TEST", "onDragStart | offset | x = ${offset.x}, y = ${offset.y} / index = ${it.index}")
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
            }
    }

    fun onDragStart(offset: Offset, itemId: Int) {
        currentIndexOfDraggedItem = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull {
                Log.d("TEST", "onDragStart() | ${it.index}, isSame = ${it.key == itemId}")
                it.key == itemId
            }
            ?.index

        currentIndexOfDraggedItem?.let { index ->
            initiallyDraggedElement = lazyListState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == index }
        }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        initialOffsets?.let { (topOffset, bottomOffset) ->
            val startOffset = topOffset + draggedDistance
            val endOffset = bottomOffset + draggedDistance

            currentElement?.let { hovered ->
                lazyListState.layoutInfo.visibleItemsInfo
                    .filterNot { item -> item.offsetEnd < startOffset || item.offset > endOffset || hovered.index == item.index }
                    .firstOrNull { item ->
                        val delta = startOffset - hovered.offset
                        when {
                            delta > 0 -> (endOffset > item.offsetEnd)
                            else -> (startOffset < item.offset)
                        }
                    }
                    ?.also { item ->
                        Log.d("TEST", "onDrag | offset = ${offset.x}, y = ${offset.y} / index = ${item.index}")
                        currentIndexOfDraggedItem?.let { current ->
                            onMove.invoke(current, item.index)
                        }
                        currentIndexOfDraggedItem = item.index
                    }
            }
        }
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance

            return@let when {
                draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}
