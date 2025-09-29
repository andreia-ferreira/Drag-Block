package net.penguin.dragblock.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSizes(
    val blockSize: Dp = 60.dp
)

val LocalSizes = compositionLocalOf { AppSizes() }