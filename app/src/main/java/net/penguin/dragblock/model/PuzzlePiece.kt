package net.penguin.dragblock.model

data class PuzzlePiece(
    val cells: List<Cell>,
    val rows: Int,
    val columns: Int
)
