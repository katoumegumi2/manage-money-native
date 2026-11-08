package com.taufik.managemoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.data.settings.ThemeMode
import com.taufik.managemoney.ui.MoneyApp
import com.taufik.managemoney.ui.theme.ManageMoneyTheme
import com.taufik.managemoney.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by themeViewModel.prefs.collectAsStateWithLifecycle()
            val dark = when (prefs.mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            ManageMoneyTheme(
                darkTheme = dark,
                accent = androidx.compose.ui.graphics.Color(prefs.accentArgb),
                flowColor = prefs.flowColor,
            ) {
                MoneyApp()
            }
        }
    }
}
