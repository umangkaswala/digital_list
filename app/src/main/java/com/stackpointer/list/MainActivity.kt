package com.stackpointer.list

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.stackpointer.list.domain.model.ThemeMode
import com.stackpointer.list.domain.repository.SettingsRepository
import com.stackpointer.list.ui.navigation.DigitalListNavHost
import com.stackpointer.list.ui.navigation.ItemDeepLink
import com.stackpointer.list.ui.theme.DigitalListTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    // singleTop means a tapped notification while the app is already running arrives via
    // onNewIntent, not a fresh onCreate — held as Compose state so the NavHost can react to it.
    private var pendingDeepLink by mutableStateOf<ItemDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = extractDeepLink(intent)
        setContent {
            val themeMode by settingsRepository.settings.map { it.themeMode }
                .collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            DigitalListTheme(darkTheme = darkTheme) {
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
