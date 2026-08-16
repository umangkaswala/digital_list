package com.stackpointer.list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stackpointer.list.ui.theme.DigitalListTheme

/** One of Home's six saved-view tiles (screen 11): "Today 1/5", "Starred 2", etc. */
@Composable
fun ViewTile(
    icon: ImageVector,
    label: String,
    count: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        // A minimum, not a fixed height — screen 11 specs 76dp, but at large font scales the
        // label+count row needs more room than that to avoid clipping wrapped text.
        modifier = modifier.heightIn(min = 76.dp),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
                Text(text = count, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ViewTilePreview() {
    DigitalListTheme {
        ViewTile(
            icon = Icons.Filled.Today,
            label = "Today",
            count = "1/5",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = {},
        )
    }
}
