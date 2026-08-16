package com.stackpointer.list.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stackpointer.list.Features
import com.stackpointer.list.ui.theme.DigitalListTheme

/**
 * The docked capture bar on every list screen (SCREENS.md's shared "Capture bar" element).
 * [placeholder] depends on the view it's shown in — "Add a note or task" on Home, "Add to
 * today" in Today, etc. — that's the caller's call, not this component's.
 */
@Composable
fun CaptureBar(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMicClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // DESIGN_TOKENS.md's shape-morph rule: "Pressing a button squares its corners one step and
    // springs back on release" — extraLarge (28dp) steps down to large (16dp) while pressed.
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "captureBarCornerRadius",
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { if (Features.voiceCapture) onMicClick?.invoke() },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(imageVector = Icons.Filled.Mic, contentDescription = "Voice capture")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CaptureBarPreview() {
    DigitalListTheme {
        CaptureBar(placeholder = "Add a note or task", onClick = {})
    }
}
