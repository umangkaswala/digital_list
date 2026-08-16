package com.stackpointer.list.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.Features
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

/** Screen 06 — full-screen search, reached from the docked search bar on Home. */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    SearchContent(
        uiState = uiState,
        onBack = onBack,
        onOpenItem = onOpenItem,
        onQueryChange = viewModel::updateQuery,
        onScopeSelect = viewModel::selectScope,
        onSubmit = viewModel::commitQuery,
        onClearQuery = viewModel::clearQuery,
        onSelectRecent = viewModel::selectRecentQuery,
        onRemoveRecent = viewModel::removeRecentQuery,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    uiState: SearchUiState,
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onQueryChange: (String) -> Unit,
    onScopeSelect: (SearchScope) -> Unit,
    onSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    onSelectRecent: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = uiState.query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search notes and tasks") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            onSubmit()
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear query")
                        }
                    }
                    // Features.voiceCapture is off — the mic is shown for design fidelity only.
                    IconButton(onClick = { if (Features.voiceCapture) Unit }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice search")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                ScopeChip("Everything", uiState.scope == SearchScope.EVERYTHING) { onScopeSelect(SearchScope.EVERYTHING) }
                ScopeChip("Notes", uiState.scope == SearchScope.NOTES) { onScopeSelect(SearchScope.NOTES) }
                ScopeChip("Tasks", uiState.scope == SearchScope.TASKS) { onScopeSelect(SearchScope.TASKS) }
                ScopeChip("Archive", uiState.scope == SearchScope.ARCHIVE) { onScopeSelect(SearchScope.ARCHIVE) }
            }

            if (uiState.query.isBlank()) {
                RecentQueries(
                    recentQueries = uiState.recentQueries,
                    onSelectRecent = onSelectRecent,
                    onRemoveRecent = onRemoveRecent,
                )
            } else if (uiState.results.isEmpty()) {
                EmptyState(
                    headline = "No results",
                    supportingText = "Try a different word, or check another scope above.",
                    actionLabel = "Clear search",
                    onAction = onClearQuery,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = "${uiState.results.size} RESULTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.results, key = { it.id }) { item ->
                        SearchResultRow(
                            item = item,
                            query = uiState.query,
                            onClick = { onOpenItem(item) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun RecentQueries(
    recentQueries: List<String>,
    onSelectRecent: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
) {
    if (recentQueries.isEmpty()) return
    Column {
        Text(
            text = "RECENT",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        recentQueries.forEach { recentQuery ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = recentQuery,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f).clickable { onSelectRecent(recentQuery) },
                )
                IconButton(onClick = { onRemoveRecent(recentQuery) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove \"$recentQuery\"", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(item: Item, query: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon: ImageVector = when {
        item.isArchived -> Icons.Filled.Archive
        item.isNote -> Icons.AutoMirrored.Outlined.StickyNote2
        else -> Icons.Filled.TaskAlt
    }
    val now = Instant.now()
    val supportingRaw = item.body?.takeIf { item.isNote } ?: ItemFormatting.metadata(item, now) ?: ""
    val metaPrefix = when {
        item.isArchived -> "Archived · "
        item.isNote -> "Note · "
        else -> ""
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column {
                Text(text = highlightMatch(item.title, query), style = MaterialTheme.typography.titleMedium)
                if (supportingRaw.isNotBlank()) {
                    Text(
                        text = highlightMatch("$metaPrefix$supportingRaw", query),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun highlightMatch(text: String, query: String) = buildAnnotatedString {
    if (query.isBlank()) {
        append(text)
        return@buildAnnotatedString
    }
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer
    var start = 0
    while (start < text.length) {
        val matchIndex = text.indexOf(query, start, ignoreCase = true)
        if (matchIndex == -1) {
            append(text.substring(start))
            break
        }
        append(text.substring(start, matchIndex))
        withStyle(SpanStyle(background = highlightColor, fontWeight = FontWeight.Bold)) {
            append(text.substring(matchIndex, matchIndex + query.length))
        }
        start = matchIndex + query.length
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    DigitalListTheme {
        SearchContent(
            uiState = SearchUiState(
                query = "lease",
                results = listOf(
                    Item.draft().copy(title = "Send the lease addendum"),
                    Item.draft().copy(title = "Flat move — admin", body = "…countersigned lease goes to the agent"),
                ),
                recentQueries = listOf("standup", "tile samples"),
            ),
            onBack = {},
            onOpenItem = {},
            onQueryChange = {},
            onScopeSelect = {},
            onSubmit = {},
            onClearQuery = {},
            onSelectRecent = {},
            onRemoveRecent = {},
        )
    }
}
