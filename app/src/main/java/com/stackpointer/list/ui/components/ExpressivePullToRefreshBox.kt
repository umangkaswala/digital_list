package com.stackpointer.list.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen 10's pull-to-refresh — "the expressive loading indicator (a 40 dp morphing shape) sits
 * centred under the bar during pull-to-refresh", i.e. [PullToRefreshDefaults.LoadingIndicator]
 * rather than the classic spinner. Every list in this app is a live Room [kotlinx.coroutines.flow.Flow]
 * with no network fetch behind it, so there's nothing to actually await — the gesture just
 * confirms "this is already current" with a brief indicator rather than triggering a real reload.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressivePullToRefreshBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                delay(400)
                isRefreshing = false
            }
        },
        state = state,
        modifier = modifier,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
        content = content,
    )
}
