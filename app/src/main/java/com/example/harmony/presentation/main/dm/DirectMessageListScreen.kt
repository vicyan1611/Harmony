package com.example.harmony.presentation.main.dm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.domain.model.DirectMessageConversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageListScreen(
    onNavigateToDmChat: (conversationId: String) -> Unit,
    onNavigateToUserSearch: () -> Unit,
    viewModel: DirectMessageListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Direct Messages") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToUserSearch) { // Example: Navigate to search to find user for new DM
                Icon(Icons.Default.Add, contentDescription = "New DM")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                state.conversations.isEmpty() -> Text(
                    "No conversations yet.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.conversations, key = { it.id }) { conversation ->
                            DmConversationItem(
                                conversation = conversation,
                                currentUserId = state.currentUser?.id ?: "",
                                onClick = { onNavigateToDmChat(conversation.id) }
                            )
                            HorizontalDivider() // Add dividers between items
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DmConversationItem(
    conversation: DirectMessageConversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherParticipant = conversation.getOtherParticipant(currentUserId)
    val otherParticipantName = otherParticipant?.displayName ?: "Unknown User"
    val otherParticipantPhoto = otherParticipant?.photoUrl

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundedAvatar(
            avatarImageUrl = otherParticipantPhoto ?: "",
            char = otherParticipantName.firstOrNull()?.uppercaseChar() ?: '?',
            size = 48.dp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(otherParticipantName, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conversation.lastMessageText ?: "No messages yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}