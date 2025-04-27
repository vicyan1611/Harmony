package com.example.harmony.presentation.main.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.core.theme.Color
import com.example.harmony.domain.model.Message
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.channelName) },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            MessageInputBar(
                message = state.currentMessageInput,
                onMessageChange = { viewModel.onEvent(ChatEvent.OnMessageInputChange(it)) },
                onSendClick = { viewModel.onEvent(ChatEvent.OnSendMessageClick) }
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            if (state.isLoading && state.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Takes remaining space
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between messages
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageItem(message = message, currentUserId = state.currentUser?.id ?: "")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, currentUserId: String) {
    val isCurrentUser = message.senderId == currentUserId
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isCurrentUser) Color.MessageBubbleMe else Color.MessageBubbleOther
    val textColor = MaterialTheme.colorScheme.onSurface

    val timestampFormatted = message.timestamp?.toDate()?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: ""

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(alignment),
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isCurrentUser) {
                RoundedAvatar(
                    avatarImageUrl = message.senderPhotoUrl ?: "",
                    char = message.senderDisplayName.firstOrNull()?.uppercaseChar() ?: '?',
                    size = 32.dp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bubbleColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 280.dp), // Limit bubble width
                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
            ) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderDisplayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary // Distinguish sender name
                    )
                }
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 16.sp
                )
                Text(
                    text = timestampFormatted,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp)) // Add space between bubble and avatar
                RoundedAvatar(
                    avatarImageUrl = message.senderPhotoUrl ?: "",
                    char = message.senderDisplayName.firstOrNull()?.uppercaseChar() ?: '?',
                    size = 32.dp
                )
            }
        }
    }
}

@Composable
fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1F),
            placeholder = { Text("Type a message...") },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                focusedContainerColor = MaterialTheme.colorScheme.surface, // Background inside the text field
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            maxLines = 4
        )
        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendClick,
            enabled = message.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (message.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceDim)

        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Message",
                tint = (if (message.isNotBlank()) MaterialTheme.colorScheme.surfaceDim else MaterialTheme.colorScheme.primary)
            )
        }
    }
}