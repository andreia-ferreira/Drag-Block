package net.penguin.dragblock.ui.compose

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import net.penguin.dragblock.ui.theme.DragBlockTheme
import net.penguin.dragblock.ui.theme.sizes
import kotlin.math.roundToInt


@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var blockOffset by remember { mutableStateOf(Offset.Zero) }

    Surface(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Block(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                currentOffset = blockOffset,
                onAction = {
                    when (it) {
                        BlockAction.OnCancel -> {
                            blockOffset = Offset.Zero
                        }
                        is BlockAction.OnDrag -> {
                            blockOffset += it.offset
                        }
                    }
                }
            )

            Grid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                size = 2
            )
        }
    }
}

sealed class BlockAction {
    data class OnDrag(val offset: Offset): BlockAction()
    data object OnCancel: BlockAction()
}

@Composable
fun Block(
    modifier: Modifier = Modifier,
    currentOffset: Offset,
    onAction: (BlockAction) -> Unit
) {
    Box(modifier) {
        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffset.x.roundToInt(), currentOffset.y.roundToInt()) }
                .background(color = MaterialTheme.colorScheme.secondary)
                .size(MaterialTheme.sizes.blockSize)
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onAction(BlockAction.OnDrag(dragAmount))
                        },
                        onDragEnd = {
                            onAction(BlockAction.OnCancel)
                        }
                    )
                }
        )
    }
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    size: Int
) {
    Box(modifier) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            repeat(size) {
                Row {
                    repeat(size) {
                        Cell()
                    }
                }
            }
        }
    }
}

@Composable
fun Cell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
            )
            .size(MaterialTheme.sizes.blockSize)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
    )
}

@Preview
@Composable
private fun MainScreenPreview() {
    DragBlockTheme {
        MainScreen()
    }
}