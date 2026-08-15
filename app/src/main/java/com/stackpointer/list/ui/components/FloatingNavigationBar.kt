package com.stackpointer.list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stackpointer.list.ui.theme.DigitalListTheme
import com.stackpointer.list.ui.theme.FullShape
import com.stackpointer.list.ui.theme.NavPillShape

enum class NavDestination(val icon: ImageVector, val label: String) {
    HOME(Icons.AutoMirrored.Outlined.StickyNote2, "Home"),
    TASKS(Icons.Filled.TaskAlt, "Tasks"),
    COLLECTIONS(Icons.Filled.Style, "Collections"),
}

/** The three-destination floating nav bar (SCREENS.md's shared element). Only the active
 * destination shows a label, inside a [FullShape] pill. */
@Composable
fun FloatingNavigationBar(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = NavPillShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavDestination.entries.forEach { destination ->
                NavItem(
                    destination = destination,
                    isSelected = destination == selected,
                    onClick = { onSelect(destination) },
                )
            }
        }
    }
}

@Composable
private fun NavItem(destination: NavDestination, isSelected: Boolean, onClick: () -> Unit) {
    val itemSemantics: Modifier.() -> Modifier = {
        semantics {
            role = Role.Tab
            selected = isSelected
            contentDescription = destination.label
        }
    }

    if (isSelected) {
        Surface(
            onClick = onClick,
            shape = FullShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.height(40.dp).itemSemantics(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    } else {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            modifier = Modifier.size(48.dp).itemSemantics(),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FloatingNavigationBarPreview() {
    DigitalListTheme {
        FloatingNavigationBar(selected = NavDestination.HOME, onSelect = {})
    }
}
