package net.penguin.dragblock

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.penguin.dragblock.model.Cell
import net.penguin.dragblock.model.Grid
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _gridState: MutableStateFlow<Grid> = MutableStateFlow(TEST_GRID)
    val gridState: StateFlow<Grid> = _gridState

    fun onBlockPlaced(indexes: List<Int>) {
        _gridState.value = _gridState.value.copy(
            cells = _gridState.value.cells.mapIndexed { index, cell ->
                if (index in indexes) {
                    cell.copy(Cell.Type.FILLED)
                } else cell
            }
        )
    }

    companion object {
        val TEST_GRID = Grid(
            rowSize = 2,
            cells = listOf(
                Cell(Cell.Type.ACTIVE),
                Cell(Cell.Type.ACTIVE),
                Cell(Cell.Type.ACTIVE),
                Cell(Cell.Type.ACTIVE),
            )
        )
    }
}