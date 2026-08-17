package com.stackpointer.list.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * Screen 04's "container transform from card": the tapped list row and the destination editor
 * screen share one [SharedTransitionScope] plus the NavHost's per-route [AnimatedContentScope].
 * Both live above individual screens (in [DigitalListNavHost]), so they're threaded down via
 * CompositionLocal rather than as constructor params on every screen in between.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedContentScope = compositionLocalOf<AnimatedContentScope?> { null }
