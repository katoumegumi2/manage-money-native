package com.taufik.managemoney.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.taufik.managemoney.domain.model.AccountBalance

/**
 * Grid akun 2 kolom yang bisa **drag-reorder** (tekan-tahan lalu geser sebuah kartu ke posisi lain).
 * [onReorder] dipanggil saat dilepas dengan urutan id akun yang baru (untuk dipersist).
 */
@Composable
fun AccountReorderGrid(
    accounts: List<AccountBalance>,
    onClick: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    val bounds = remember { mutableStateMapOf<Int, Rect>() }

    Column(modifier = modifier.fillMaxWidth()) {
        accounts.chunked(2).forEachIndexed { rowIndex, pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEachIndexed { colIndex, acc ->
                    val index = rowIndex * 2 + colIndex
                    val isDragging = draggingIndex == index
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { bounds[index] = it.boundsInRoot() }
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationX = dragOffset.x
                                    translationY = dragOffset.y
                                }
                            }
                            .pointerInput(accounts, index) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = index
                                        targetIndex = index
                                        dragOffset = Offset.Zero
                                        dragStart = bounds[index]?.center ?: Offset.Zero
                                    },
                                    onDrag = { change, delta ->
                                        change.consume()
                                        dragOffset += delta
                                        val cur = dragStart + dragOffset
                                        val over = bounds.entries
                                            .firstOrNull { it.key != index && it.value.contains(cur) }?.key
                                        targetIndex = over ?: index
                                    },
                                    onDragEnd = {
                                        val from = draggingIndex
                                        val to = targetIndex
                                        draggingIndex = null
                                        targetIndex = null
                                        dragOffset = Offset.Zero
                                        if (from != null && to != null && from != to) {
                                            val ids = accounts.map { it.accountId }.toMutableList()
                                            ids.add(to, ids.removeAt(from))
                                            onReorder(ids)
                                        }
                                    },
                                    onDragCancel = {
                                        draggingIndex = null
                                        targetIndex = null
                                        dragOffset = Offset.Zero
                                    },
                                )
                            },
                    ) {
                        AccountChip(
                            account = acc,
                            onClick = { onClick(acc.accountId) },
                            dragging = isDragging,
                            targeted = targetIndex == index && draggingIndex != null && draggingIndex != index,
                        )
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
