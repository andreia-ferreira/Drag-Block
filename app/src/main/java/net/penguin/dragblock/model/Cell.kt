package net.penguin.dragblock.model

data class Cell(
    val type: Type,
) {
    enum class Type {
        ACTIVE, EMPTY, OBSTACLE, FILLED
    }
}
