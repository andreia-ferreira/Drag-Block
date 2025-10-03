package net.penguin.dragblock.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.penguin.dragblock.MainViewModel
import net.penguin.dragblock.model.Cell
import net.penguin.dragblock.model.Grid
import net.penguin.dragblock.ui.theme.DragBlockTheme
import net.penguin.dragblock.ui.theme.sizes
import kotlin.math.roundToInt

sealed class BlockAction {
    data class OnDrag(val offset: Offset) : BlockAction()
    data object OnDragEnd : BlockAction()
    data class OnGloballyPositioned(val bounds: Rect) : BlockAction()
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val grid by viewModel.gridState.collectAsStateWithLifecycle()
    var blockOffset by remember { mutableStateOf(Offset.Zero) }
    var blockBounds by remember { mutableStateOf(Rect.Zero) }
    val cellBounds = remember {
        mutableStateListOf<Rect>().apply {
            repeat(grid.rowSize * grid.rowSize) { add(Rect.Zero) }
        }
    }
    val hoveredCellIndexes by remember {
        derivedStateOf { mutableStateListOf<Int>() }
    }

    Surface(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Block(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .zIndex(1f),
                currentOffset = blockOffset,
                onAction = {
                    when (it) {
                        BlockAction.OnDragEnd -> {
                            if (hoveredCellIndexes.isNotEmpty()) {
                                viewModel.onBlockPlaced(hoveredCellIndexes)
                                blockOffset = Offset.Zero
                            } else {
                                blockOffset = Offset.Zero
                            }
                        }
                        is BlockAction.OnDrag -> {
                            blockOffset += it.offset
                        }
                        is BlockAction.OnGloballyPositioned -> {
                            blockBounds = it.bounds
                        }
                    }
                }
            )

            Grid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                gridState = grid,
                hoveredCellIndexes = hoveredCellIndexes,
                onCellPositioned = { index, rect ->
                    cellBounds[index] = rect
                }
            )
        }
    }

    LaunchedEffect(blockOffset, cellBounds) {
        val draggedCenter = blockBounds.center
        cellBounds.forEachIndexed { index, rect ->
            if (rect.contains(draggedCenter)) {
                println("Currently over cell $index")
                // Here you could update state to highlight the cell
                if (!hoveredCellIndexes.contains(index)) {
                    hoveredCellIndexes.add(index)
                }
            } else if (hoveredCellIndexes.contains(index)) {
                hoveredCellIndexes.remove(index)
            }
        }
    }
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
                .onGloballyPositioned {
                    onAction(BlockAction.OnGloballyPositioned(it.boundsInRoot()))
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onAction(BlockAction.OnDrag(dragAmount))
                        },
                        onDragEnd = {
                            onAction(BlockAction.OnDragEnd)
                        }
                    )
                }
        )
    }
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    gridState: Grid,
    hoveredCellIndexes: List<Int>,
    onCellPositioned: (Int, Rect) -> Unit
) {
    Box(modifier) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            repeat(gridState.rowSize) { row ->
                Row {
                    repeat(gridState.rowSize) { column ->
                        val index = row * gridState.rowSize + column
                        Cell(
                            type = gridState.cells[index].type,
                            isHighlighted = hoveredCellIndexes.contains(index),
                            onGloballyPositioned = {
                                onCellPositioned(index, it.boundsInRoot())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Cell(
    modifier: Modifier = Modifier,
    type: Cell.Type,
    isHighlighted: Boolean,
    onGloballyPositioned: (LayoutCoordinates) -> Unit
) {
    val boxModifier = modifier
        .size(MaterialTheme.sizes.blockSize)
        .then(
            when (type) {
                Cell.Type.ACTIVE -> Modifier
                    .background(
                        if (isHighlighted) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
                    .onGloballyPositioned {
                        onGloballyPositioned(it)
                    }

                Cell.Type.EMPTY -> Modifier
                Cell.Type.OBSTACLE -> Modifier.background(Color.Gray)
                Cell.Type.FILLED -> Modifier
                    .background(MaterialTheme.colorScheme.secondary)
            }
        )

    Box(boxModifier)
}

@Preview
@Composable
private fun MainScreenPreview() {
    DragBlockTheme {
        MainScreen()
    }
}