package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SortOrder { DUE_DATE, RECENTLY_EDITED, TITLE_A_TO_Z, MANUAL }

private val LABELS = mapOf(
    SortOrder.DUE_DATE to "Due date",
    SortOrder.RECENTLY_EDITED to "Recently edited",
    SortOrder.TITLE_A_TO_Z to "Title, A to Z",
    SortOrder.MANUAL to "Manual order",
)

/** Screen 26 (sort-by half). Selecting an option applies and closes immediately — there's no
 * separate confirm step in the design, unlike the alert-type sheet next to it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBySheet(selected: SortOrder, onSelect: (SortOrder) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            LABELS.forEach { (order, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected == order, onClick = { onSelect(order) })
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == order, onClick = { onSelect(order) })
                    Text(text = label, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
