package com.stackpointer.list.ui.screens.capture

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.stackpointer.list.Features
import com.stackpointer.list.domain.model.TriggerType

/**
 * The capture sheet — screens 17-23, one modal bottom sheet with five mode panels. Callers
 * hold the [CaptureViewModel] at screen level (via `hiltViewModel()`) and call
 * [CaptureViewModel.openFor] from the capture bar's onClick; this composable just renders
 * whatever [CaptureUiState.isOpen] says.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(viewModel: CaptureViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    if (!uiState.isOpen) return

    val context = LocalContext.current
    // Requested here rather than at launch, per DATA_MODEL.md — "at the first reminder
    // creation, not at launch". Fires whenever a TIME-triggered draft is confirmed; harmless
    // to call again once granted or permanently denied, since the system just no-ops the launch.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* AlarmReceiver checks the permission again before posting, regardless of the result. */ }

    ModalBottomSheet(
        onDismissRequest = viewModel::dismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        TextField(
            value = uiState.draft.title,
            onValueChange = viewModel::updateTitle,
            placeholder = { Text("Title") },
            textStyle = MaterialTheme.typography.titleLarge,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        )
        TextField(
            value = uiState.draft.body.orEmpty(),
            onValueChange = viewModel::updateBody,
            placeholder = { Text("Add a time, place or list below") },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // TODO(expressive): swap for the connected icon-button group component once it's
            // stable in this Material3 version — approximated here with individually toggled
            // icon buttons using the same selected/unselected colours.
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ModeToggle(
                    icon = Icons.Filled.Schedule,
                    label = "Time",
                    selected = uiState.mode == CaptureMode.TIME,
                    onClick = { viewModel.selectMode(CaptureMode.TIME) },
                )
                ModeToggle(
                    icon = Icons.Filled.LocationOn,
                    label = "Place",
                    selected = uiState.mode == CaptureMode.PLACE,
                    onClick = { if (Features.placeReminders) viewModel.selectMode(CaptureMode.PLACE) },
                )
                ModeToggle(
                    icon = Icons.Filled.Checklist,
                    label = "Checklist",
                    selected = uiState.mode == CaptureMode.CHECKLIST,
                    onClick = { viewModel.selectMode(CaptureMode.CHECKLIST) },
                )
                ModeToggle(
                    icon = Icons.Filled.Image,
                    label = "Image",
                    selected = false,
                    onClick = { /* Features.imageAttachments is off — no-op */ },
                )
                ModeToggle(
                    icon = Icons.AutoMirrored.Filled.Label,
                    label = "Label",
                    selected = uiState.mode == CaptureMode.LABEL,
                    onClick = { viewModel.selectMode(CaptureMode.LABEL) },
                )
            }

            FloatingActionButton(
                onClick = {
                    if (uiState.draft.triggerType == TriggerType.TIME &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    viewModel.confirm()
                },
                containerColor = if (uiState.canConfirm) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "Save")
            }
        }

        when (uiState.mode) {
            CaptureMode.TIME -> TimeModeContent(uiState = uiState, viewModel = viewModel)
            CaptureMode.CHECKLIST -> ChecklistModeContent(uiState = uiState, viewModel = viewModel)
            CaptureMode.LABEL -> CollectionModeContent(uiState = uiState, viewModel = viewModel)
            CaptureMode.PLACE, CaptureMode.NONE -> Unit
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 16.dp))
    }

    if (uiState.earlyAlertMenuOpen) EarlyAlertMenu(uiState = uiState, viewModel = viewModel)
    if (uiState.repeatPickerOpen) RepeatPickerSheet(uiState = uiState, viewModel = viewModel)
    if (uiState.alertTypeSheetOpen) AlertTypeSheet(uiState = uiState, viewModel = viewModel)
}

@Composable
private fun ModeToggle(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = label)
    }
}

// No @Preview here — every method on this composable is a direct ViewModel call (openFor,
// selectMode, confirm, ...) rather than plain lambdas, so a meaningful preview would need to
// duplicate most of CaptureViewModel's logic. Verified on-device instead (see the
// android-build-verify skill) — the interactive, stateful nature of this sheet makes a real
// run more informative than a static preview anyway.
