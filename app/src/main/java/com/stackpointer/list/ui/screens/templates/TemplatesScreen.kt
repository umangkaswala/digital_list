package com.stackpointer.list.ui.screens.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.Template
import com.stackpointer.list.domain.model.TriggerType
import com.stackpointer.list.ui.screens.capture.CaptureMode
import com.stackpointer.list.ui.screens.capture.CaptureSheet
import com.stackpointer.list.ui.screens.capture.CaptureViewModel
import com.stackpointer.list.ui.theme.DigitalListTheme

/** Screen 28 — "Try these out", reached from Home's templates teaser. */
@Composable
fun TemplatesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TemplatesViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    TemplatesContent(
        uiState = uiState,
        onBack = onBack,
        onCardTap = { template ->
            val draft = template.draft.toItem()
            val mode = when {
                draft.subItems.isNotEmpty() -> CaptureMode.CHECKLIST
                draft.triggerType == TriggerType.TIME -> CaptureMode.TIME
                else -> CaptureMode.NONE
            }
            captureViewModel.openWithDraft(draft, mode)
        },
        onApply = viewModel::applyTemplate,
        modifier = modifier,
    )
    CaptureSheet(viewModel = captureViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatesContent(
    uiState: TemplatesUiState,
    onBack: () -> Unit,
    onCardTap: (Template) -> Unit,
    onApply: (Template) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Try these out") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            item {
                Text(
                    text = "Tap one to open it in the capture sheet, already filled in.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            items(uiState.templates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onTap = { onCardTap(template) },
                    onApply = { onApply(template) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .animateItem(placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(template: Template, onTap: () -> Unit, onApply: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onTap,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = template.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledIconButton(onClick = onApply) {
                Icon(Icons.Filled.Add, contentDescription = "Add \"${template.title}\"")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TemplatesScreenPreview() {
    DigitalListTheme {
        TemplatesContent(
            uiState = TemplatesUiState(
                isLoading = false,
                templates = listOf(
                    Template(
                        id = "1",
                        title = "Walk with family",
                        description = "Every week on Sat",
                        iconKey = "lightbulb",
                        draft = com.stackpointer.list.domain.model.TemplateDraft(title = "Walk with family"),
                    ),
                ),
            ),
            onBack = {},
            onCardTap = {},
            onApply = {},
        )
    }
}
