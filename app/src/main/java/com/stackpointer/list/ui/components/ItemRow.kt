package com.stackpointer.list.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stackpointer.list.ui.theme.DigitalListTheme

/**
 * The item card shared across every list screen (SCREENS.md's "Item card" element). Callers
 * pass already-formatted text — date/recurrence/overdue wording is a screen-level concern, not
 * this component's, per CLAUDE.md's "composables take data and lambdas" rule.
 */
@Composable
fun ItemRow(
    title: String,
    metadata: String?,
    isCompleted: Boolean,
    isStarred: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier,
    recurrenceText: String? = null,
    checklistProgress: String? = null,
    isOverdue: Boolean = false,
    onToggleStar: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompleteCheckbox(isCompleted = isCompleted, onToggle = onToggleComplete)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                )
                if (metadata != null || checklistProgress != null) {
                    Column(modifier = Modifier.padding(top = 3.dp)) {
                        metadata?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        checklistProgress?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (recurrenceText != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = recurrenceText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (isStarred) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Starred",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )
            } else if (onToggleStar != null) {
                // Reserve no extra space for an absent star — starring from the row itself
                // isn't part of the design (screen 24's detail top bar owns that toggle).
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompleteCheckbox(isCompleted: Boolean, onToggle: () -> Unit) {
    // Circle -> squircle on completion, per DESIGN_TOKENS.md's shape-morph note. A full
    // shape-morph API call is still marked experimental across Material3 alpha releases, so
    // this animates the corner radius directly on the spatial-fast spring instead, which
    // reads the same and needs no experimental shape-morphing surface.
    val cornerRadius by animateDpAsState(
        targetValue = if (isCompleted) 6.dp else 12.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "checkboxCornerRadius",
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(50))
            .clickable(
                onClickLabel = if (isCompleted) "Mark as not completed" else "Mark as completed",
                role = Role.Checkbox,
                onClick = onToggle,
            )
            .semantics { contentDescription = if (isCompleted) "Completed" else "Not completed" },
        contentAlignment = Alignment.Center,
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant, shape = RoundedCornerShape(cornerRadius)),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemRowPreview() {
    DigitalListTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            ItemRow(
                title = "Call mum",
                metadata = "1:30 PM · Personal",
                recurrenceText = "Every day",
                isCompleted = false,
                isStarred = true,
                onClick = {},
                onToggleComplete = {},
            )
            ItemRow(
                title = "Send the lease addendum",
                metadata = "Was due Monday · Work",
                isCompleted = false,
                isStarred = false,
                isOverdue = true,
                onClick = {},
                onToggleComplete = {},
            )
            ItemRow(
                title = "Pay the electricity bill",
                metadata = "Completed 8:04 AM",
                isCompleted = true,
                isStarred = false,
                onClick = {},
                onToggleComplete = {},
            )
        }
    }
}
