package com.stackpointer.list.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stackpointer.list.ui.theme.DigitalListTheme
import com.stackpointer.list.ui.theme.TitleMediumEmphasized

/** A bucket header — "Today Thu 14 Aug · 3", "Past 1", etc. [isError] reads it in the error
 * role for "Past"/"Overdue" buckets, per DESIGN_TOKENS.md — never colour alone, so the label
 * text itself should already say "Past"/"Overdue" when [isError] is true. */
@Composable
fun SectionHeader(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        Text(text = label, style = TitleMediumEmphasized, color = color)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    DigitalListTheme {
        androidx.compose.foundation.layout.Column {
            SectionHeader(label = "Today Thu 14 Aug", count = 3)
            SectionHeader(label = "Past", count = 1, isError = true)
        }
    }
}
