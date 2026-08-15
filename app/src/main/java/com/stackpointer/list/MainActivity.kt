package com.stackpointer.list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stackpointer.list.ui.navigation.DigitalListNavHost
import com.stackpointer.list.ui.theme.DigitalListTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigitalListTheme {
                DigitalListNavHost()
            }
        }
    }
}
