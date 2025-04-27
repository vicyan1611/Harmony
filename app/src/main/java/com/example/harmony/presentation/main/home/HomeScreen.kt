package com.example.harmony.presentation.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            if (route == NavRoutes.LOGIN) {
                onNavigateToLogin()
            }
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )
            } else if (state.user != null) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome to Harmony!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Logged in as: ${state.user?.displayName ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "(Email: ${state.user?.email ?: "N/A"})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    HarmonyButton(
                        text = "Logout",
                        onClick = { viewModel.logout() },
                        isLoading = isLoggingOut,
                        enabled = !isLoggingOut
                    )
                }
            }
        }
    }

}