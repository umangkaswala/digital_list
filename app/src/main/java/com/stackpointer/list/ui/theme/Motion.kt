package com.stackpointer.list.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme

/**
 * DESIGN_TOKENS.md's spring table (spatial fast/default/slow, effects fast/default/slow) is
 * exactly [MotionScheme.expressive]'s own spec set, not a custom scheme — so there's nothing
 * to hand-define here. Set once in [DigitalListTheme] and read at call sites via
 * `MaterialTheme.motionScheme.fastSpatialSpec()` etc. Do not hand-write springs elsewhere.
 *
 * Token → MotionScheme mapping used across screens:
 * - spatial fast    -> fastSpatialSpec()    (FAB menu stagger, check morph)
 * - spatial default -> defaultSpatialSpec() (sheet entry, container transform, nav indicator, list reorder)
 * - spatial slow    -> slowSpatialSpec()    (large layout changes)
 * - effects fast     -> fastEffectsSpec()    (ripple)
 * - effects default  -> defaultEffectsSpec() (chip selection, checkbox fill, strike-through)
 * - effects slow     -> slowEffectsSpec()    (scrim fade)
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val ExpressiveMotionScheme: MotionScheme = MotionScheme.expressive()
