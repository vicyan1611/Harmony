package com.example.harmony.presentation.main.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // Import stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.harmony.R // Import R class của project bạn
import com.example.harmony.core.components.HarmonyButton
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: MyProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collectLatest { route ->
            if (route == NavRoutes.LOGIN) {
                onNavigateToLogin()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // Sử dụng stringResource cho title
                title = { Text(stringResource(id = R.string.my_profile_title)) },
                actions = {
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(
                            Icons.Filled.Edit,
                            // Sử dụng stringResource cho content description
                            contentDescription = stringResource(id = R.string.edit_profile_action)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }
                state.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${stringResource(id = R.string.error_prefix)}: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(onClick = { viewModel.refreshProfile() }) {
                            Text(stringResource(id = R.string.retry_button))
                        }
                    }
                }
                state.user != null -> {
                    val user = state.user!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))

                        RoundedAvatar(
                            size = 120.dp,
                            avatarImageUrl = user.photoUrl ?: "",
                            char = user.displayName.firstOrNull()?.uppercaseChar() ?: '?'
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            // Lấy username từ user object
                            text = user.displayName,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        user.settings?.let { settings ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    // Sử dụng stringResource cho content description
                                    contentDescription = stringResource(id = R.string.language_label),
                                    modifier = Modifier.size(20.dp).padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    // Sử dụng stringResource cho label và nối chuỗi
                                    text = "${stringResource(id = R.string.language_label)}: ${settings.language.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Icon(
                                    Icons.Default.Style,
                                    // Sử dụng stringResource cho content description
                                    contentDescription = stringResource(id = R.string.theme_label),
                                    modifier = Modifier.size(20.dp).padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    // Sử dụng stringResource cho label và nối chuỗi
                                    text = "${stringResource(id = R.string.theme_label)}: ${settings.theme.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        HarmonyButton(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            // Sử dụng stringResource cho nút Logout
                            text = stringResource(id = R.string.logout_button),
                            onClick = { viewModel.logout() },
                            isLoading = state.isLoggingOut,
                            enabled = !state.isLoggingOut
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Sử dụng stringResource
                        Text(stringResource(id = R.string.profile_unavailable))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refreshProfile() }) {
                            // Sử dụng stringResource
                            Text(stringResource(id = R.string.refresh_button))
                        }
                    }
                }
            }
        }
    }
}