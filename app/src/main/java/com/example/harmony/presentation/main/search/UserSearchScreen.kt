package com.example.harmony.presentation.main.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.harmony.core.components.HarmonyTextField
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.domain.model.User
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDmChat: (conversationId: String) -> Unit,
    viewModel: UserSearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is UserSearchNavigationEvent.NavigateToDmChat -> {
                    onNavigateToDmChat(event.conversationId)
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Users") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            HarmonyTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(UserSearchEvent.OnQueryChanged(it)) },
                label = "Search by Display Name",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onEvent(UserSearchEvent.ClearSearch) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                    }
                }

                state.searchQuery.length >= 2 && state.searchResults.isEmpty() && !state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No users found.")
                    }
                }

                state.searchResults.isNotEmpty() -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.searchResults, key = { it.id }) { user ->
                            UserSearchResultItem(
                                user = user,
                                onClick = { viewModel.onEvent(UserSearchEvent.OnUserSelected(user)) }
                            )
                            HorizontalDivider()
                        }
                    }
                }

                state.searchQuery.length < 2 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Enter at least 2 characters to search.")
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchResultItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundedAvatar(
            avatarImageUrl = user.photoUrl ?: "",
            char = user.displayName.firstOrNull()?.uppercaseChar() ?: '?',
            size = 40.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(user.displayName, fontWeight = FontWeight.Medium)

    }
}