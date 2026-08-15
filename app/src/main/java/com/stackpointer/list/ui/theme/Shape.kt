package com.stackpointer.list.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// The five standard M3 slots map directly to DESIGN_TOKENS.md's shape scale.
val DigitalListShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// "full" and the nav-pill's 32dp corner aren't standard Shapes slots. Per DESIGN_TOKENS.md:
// "fully rounded corners use the full token, not 50% of the component, so shape stays stable
// as a component resizes" — percent-100 achieves that regardless of the composable's size.
val FullShape = RoundedCornerShape(percent = 100)
val NavPillShape = RoundedCornerShape(32.dp)
