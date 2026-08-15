package com.stackpointer.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.stackpointer.list.ui.navigation.DigitalListNavHost
import com.stackpointer.list.ui.navigation.ItemDeepLink
import com.stackpointer.list.ui.theme.DigitalListTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // singleTop means a tapped notification while the app is already running arrives via
    // onNewIntent, not a fresh onCreate — held as Compose state so the NavHost can react to it.
    private var pendingDeepLink by mutableStateOf<ItemDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = extractDeepLink(intent)
        setContent {
            DigitalListTheme {
                DigitalListNavHost(
                    deepLink = pendingDeepLink,
                    onDeepLinkConsumed = { pendingDeepLink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink = extractDeepLink(intent)
    }

    private fun extractDeepLink(intent: Intent?): ItemDeepLink? {
        val itemId = intent?.getStringExtra(EXTRA_OPEN_ITEM_ID) ?: return null
        val isNote = intent.getBooleanExtra(EXTRA_OPEN_ITEM_IS_NOTE, false)
        return ItemDeepLink(itemId, isNote)
    }

    companion object {
        const val EXTRA_OPEN_ITEM_ID = "openItemId"
        const val EXTRA_OPEN_ITEM_IS_NOTE = "openItemIsNote"
    }
}
