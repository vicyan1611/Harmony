// In: harmony/presentation/MainActivity.kt
package com.example.harmony.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.harmony.core.theme.HarmonyTheme
import com.example.harmony.core.utils.LocaleHelper
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.domain.model.AppTheme
import com.example.harmony.presentation.main.MainViewModel
import com.example.harmony.presentation.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale



var initialLanguage = AppLanguage.ENGLISH
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private var currentAppliedLocale: Locale = Locale.getDefault()


    override fun attachBaseContext(newBase: Context) {
        // Load persisted preference or use default

        Log.d("MainActivity", "Attaching Base Context with Locale: ${initialLanguage.localeTag}")
        currentAppliedLocale = Locale(initialLanguage.localeTag)
        super.attachBaseContext(LocaleHelper.updateLocale(newBase, initialLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate - Current Context Locale: ${resources.configuration.locales[0]}")

        setContent {
            // Observe states from the ViewModel
            val language by mainViewModel.appLanguage.collectAsState()
            val theme by mainViewModel.appTheme.collectAsState()
            val isLoading by mainViewModel.isLoading.collectAsState()
            // Error is still observed but won't block NavGraph
            val error by mainViewModel.error.collectAsState()


            val useDarkTheme = when (theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
            }


            val configurationLocale = LocalConfiguration.current.locales[0]

            LaunchedEffect(language) {
                val newLocale = Locale(language.localeTag)
                Log.d("MainActivity", "LaunchedEffect: Language State: $language, New Locale: $newLocale, Applied Locale: $currentAppliedLocale, Config Locale: $configurationLocale")
                if (newLocale.language != currentAppliedLocale.language) {
                    Log.w("MainActivity", "Locale mismatch detected! Recreating activity.")
                    initialLanguage = language;
                    Locale.setDefault(newLocale);
                    recreate()
                }
            }

            // Apply the theme determined by the ViewModel state (or defaults if loading failed)
            HarmonyTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // --- Modified Logic ---
                    // Show loading indicator ONLY on initial app start while loading
                    if (isLoading && savedInstanceState == null) {
                        Log.d("MainActivity", "Showing Loading Indicator (Initial Load)")
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
                    }
                    // Otherwise, show the NavGraph regardless of error state
                    else {
                        if(error != null) {
                            Log.w("MainActivity", "Loading settings failed ($error), but proceeding to NavGraph with defaults.")
                            // You could optionally show a non-blocking Toast/Snackbar here about the error
                        } else if (!isLoading) {
                            Log.d("MainActivity", "Loading finished successfully. Showing NavGraph.")
                        } else {
                            Log.d("MainActivity", "Loading state true, but not initial load. Showing NavGraph.")
                        }
                        NavGraph() // Show NavGraph even if error is not null
                    }
                    // --- End Modified Logic ---
                }
            }
        }
    }
}



