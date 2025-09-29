package net.penguin.dragblock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

val MaterialTheme.sizes: AppSizes
    @Composable
    @ReadOnlyComposable
    get() = LocalSizes.current