package com.example.harmony.presentation.main.dm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.harmony.presentation.main.chat.MessageInputBar
import com.example.harmony.presentation.main.chat.MessageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageChatScreen(
    viewModel: DirectMessageChatViewModel = hiltViewModel()
    // onNavigateBack: () -> Unit // Optional back navigation
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.otherParticipantName) }, // Use fetched name
                // Optional: Add back navigation button
                // navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            MessageInputBar(
                message = state.currentMessageInput,
                onMessageChange = { viewModel.onEvent(DirectMessageChatEvent.OnMessageInputChange(it)) },
                onSendClick = { viewModel.onEvent(DirectMessageChatEvent.OnSendMessageClick) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoadingMessages && state.isLoadingDetails && state.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null && state.messages.isEmpty()) { // Show error only if loading failed initially
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        // Reuse the existing message item composable
                        MessageItem(message = message, currentUserId = state.currentUser?.id ?: "")
                    }
                }
                // Display sending errors temporarily at the bottom?
                if (state.error != null && state.messages.isNotEmpty()) {
                    Snackbar { Text("Error: ${state.error}") } // Example temporary error display
                    // TODO: Clear the error state after showing it
                }
            }
        }
    }
}