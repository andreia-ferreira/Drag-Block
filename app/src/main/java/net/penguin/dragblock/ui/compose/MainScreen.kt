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
import net.penguin.dragblock.model.PuzzlePiece
import net.penguin.dragblock.ui.theme.DragBlockTheme
import net.penguin.dragblock.ui.theme.sizes
import kotlin.math.roundToInt
import kotlin.math.sqrt

sealed class PuzzlePieceAction {
    data class OnDrag(val offset: Offset) : PuzzlePieceAction()
    data object OnDragEnd : PuzzlePieceAction()
    data class OnGlobalPositionChanged(val index: Int, val center: Offset) : PuzzlePieceAction()
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val puzzlePiece by viewModel.puzzlePiece.collectAsStateWithLifecycle()
    val grid by viewModel.gridState.collectAsStateWithLifecycle()

    var puzzlePieceOffset by remember { mutableStateOf(Offset.Zero) }
    val puzzlePiecesCenter = remember {
        mutableStateListOf<Offset>().apply {
            repeat(puzzlePiece.cells.size) { add(Offset.Zero) }
        }
    }
    val cellsCenter = remember {
        mutableStateListOf<Offset>().apply {
            repeat(grid.cells.size) { add(Offset.Zero) }
        }
    }
    val hoveredCellIndexes by remember {
        derivedStateOf { mutableStateListOf<Int>() }
    }

    Surface(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            PuzzlePieces(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .zIndex(1f),
                puzzlePiece = puzzlePiece,
                currentPieceOffset = puzzlePieceOffset,
                onAction = {
                    when (it) {
                        PuzzlePieceAction.OnDragEnd -> {
                            if (hoveredCellIndexes.isNotEmpty()) {
                                viewModel.onBlockPlaced(hoveredCellIndexes)
                                puzzlePieceOffset = Offset.Zero
                            } else {
                                puzzlePieceOffset = Offset.Zero
                            }
                        }

                        is PuzzlePieceAction.OnDrag -> {
                            puzzlePieceOffset += it.offset
                        }

                        is PuzzlePieceAction.OnGlobalPositionChanged -> {
                            puzzlePiecesCenter[it.index] = it.center
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
                    cellsCenter[index] = rect.center
                }
            )
        }
    }

    LaunchedEffect(puzzlePieceOffset, cellsCenter, puzzlePiece) {
        if (hoveredCellIndexes.isNotEmpty() || puzzlePieceOffset == Offset.Zero) {
            println("cleared list")
            hoveredCellIndexes.clear()
        }
        val currentHoveredIndexes = mutableListOf<Int>()
        puzzlePiecesCenter.forEach { center ->
            val distances = cellsCenter.map { calculateDistance(center, it) }
            val hoveredIndex = distances.indexOf(distances.min().takeIf { it <= 200 })

            if (!currentHoveredIndexes.contains(hoveredIndex) && hoveredIndex != -1) {
                currentHoveredIndexes.add(hoveredIndex)
            }
        }

        if (currentHoveredIndexes.size == puzzlePiece.cells.size) {
            hoveredCellIndexes.addAll(currentHoveredIndexes)
            println("currentHoveredIndexes $currentHoveredIndexes")
        }
    }
}

@Composable
private fun PuzzlePieces(
    modifier: Modifier = Modifier,
    puzzlePiece: PuzzlePiece,
    currentPieceOffset: Offset,
    onAction: (PuzzlePieceAction) -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column {
                repeat(puzzlePiece.rows) { row ->
                    Row {
                        repeat(puzzlePiece.columns) { column ->
                            val index = row * puzzlePiece.columns + column
                            Cell(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            currentPieceOffset.x.roundToInt(),
                                            currentPieceOffset.y.roundToInt()
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                onAction(PuzzlePieceAction.OnDrag(dragAmount))
                                            },
                                            onDragEnd = {
                                                onAction(PuzzlePieceAction.OnDragEnd)
                                            }
                                        )
                                    }
                                    .onGloballyPositioned {
                                        onAction(
                                            PuzzlePieceAction.OnGlobalPositionChanged(
                                                index = index,
                                                center = it.boundsInRoot().center
                                            )
                                        )
                                    },
                                type = puzzlePiece.cells[index].type,
                                isHighlighted = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculateDistance(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

@Composable
private fun Grid(
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
                            modifier = Modifier
                                .onGloballyPositioned {
                                    onCellPositioned(index, it.boundsInRoot())
                                },
                            type = gridState.cells[index].type,
                            isHighlighted = hoveredCellIndexes.contains(index),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(
    modifier: Modifier = Modifier,
    type: Cell.Type,
    isHighlighted: Boolean,
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