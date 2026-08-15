package com.stackpointer.list.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
