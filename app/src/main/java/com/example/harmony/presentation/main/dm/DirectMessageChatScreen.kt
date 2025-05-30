package com.example.harmony.presentation.main.dm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.core.theme.Color
import com.example.harmony.domain.model.Message
import com.example.harmony.presentation.main.chat.ChatEvent
import com.example.harmony.presentation.main.chat.MessageInputBar
import com.example.harmony.presentation.main.chat.MessageItem
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageChatScreen(
    viewModel: DirectMessageChatViewModel = hiltViewModel()
    // onNavigateBack: () -> Unit // Optional back navigation
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(DirectMessageChatEvent.OnImageSelected(uri))
    }

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
            Column {

                if (state.selectedImageUri != null) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp) // Adjust height as needed
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(state.selectedImageUri)
                                .crossfade(true) // Optional
                                .build(),
                            contentDescription = "Selected image preview",
                            modifier = Modifier.fillMaxHeight().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        // Clear Button
                        IconButton(
                            onClick = { viewModel.onEvent(DirectMessageChatEvent.OnClearSelectedImage) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha=0.5f), CircleShape)

                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selected image", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        // Show loading indicator during upload if needed
                        if (state.isSending && state.selectedImageUri != null) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).align(Alignment.Center),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                MessageInputBar(
                    message = state.currentMessageInput,
                    onMessageChange = { viewModel.onEvent(DirectMessageChatEvent.OnMessageInputChange(it)) },
                    onSendClick = { viewModel.onEvent(DirectMessageChatEvent.OnSendMessageClick) },
                    onPickImageClick = { imagePickerLauncher.launch("image/*") }, // Trigger picker
                    isSendEnabled = (state.currentMessageInput.isNotBlank() || state.selectedImageUri != null) && !state.isSending // Enable based on text/image and sending state
                )
                // Display sending errors temporarily
                if (state.error?.contains("Send Error") == true) {
                    Text(
                        text = state.error ?: "Failed to send",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp).background(MaterialTheme.colorScheme.errorContainer)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            if (state.isLoadingMessages && state.isLoadingDetails && state.messages.isEmpty()) { // Initial loading
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null && state.messages.isEmpty()) { // Initial fetch error
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Error loading messages: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (state.messages.isEmpty() && !state.isLoadingMessages && !state.isLoadingDetails) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages yet. Start the conversation!", modifier = Modifier.padding(16.dp))
                }
            }
            else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Takes remaining space
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp), // Add padding
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
    val horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isCurrentUser) Color.MessageBubbleMe else Color.MessageBubbleOther // Use your theme colors
    val textColor = MaterialTheme.colorScheme.onSurface // Adjust as needed

    val timestampFormatted = message.timestamp?.toDate()?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: ""
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement, // Align Row content
        verticalAlignment = Alignment.Bottom
    ) {
        // Show avatar only for other users
        if (!isCurrentUser) {
            RoundedAvatar(
                avatarImageUrl = message.senderPhotoUrl ?: "",
                char = message.senderDisplayName.firstOrNull()?.uppercaseChar() ?: '?',
                size = 32.dp,
                modifier = Modifier.padding(end = 8.dp).align(Alignment.Top) // Align avatar to top
            )
        }

        // Message bubble
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp) // Limit bubble width
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Show sender name only for other users in group chat style
            if (!isCurrentUser) {
                Text(
                    text = message.senderDisplayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary // Distinguish sender name
                )
            }

            // Display Image if imageUrl exists
            if (message.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(message.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Chat image",
                    modifier = Modifier
                        .fillMaxWidth() // Fill bubble width
                        .heightIn(max = 200.dp) // Limit image height
                        .padding(bottom = if (message.text.isNotBlank()) 4.dp else 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { /* TODO: Implement full screen image view? */ },
                    contentScale = ContentScale.Fit
                )
            }

            // Display Text if it exists (even with image, could be caption)
            // Since we send only image, this text will be empty for image messages
            if (message.text.isNotBlank()) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 16.sp
                )
            }

            // Timestamp (always show)
            Text(
                text = timestampFormatted,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp).align(Alignment.End) // Align timestamp
            )
        }

        // Show current user avatar on the right
        if (isCurrentUser) {
            RoundedAvatar(
                avatarImageUrl = message.senderPhotoUrl ?: "",
                char = message.senderDisplayName.firstOrNull()?.uppercaseChar() ?: '?',
                size = 32.dp,
                modifier = Modifier.padding(start = 8.dp).align(Alignment.Top) // Align avatar to top
            )
        }
    }
}

@Composable
fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onPickImageClick: () -> Unit,
    isSendEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Button to pick image
        IconButton(onClick = onPickImageClick, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Default.AddPhotoAlternate,
                contentDescription = "Attach Image",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface, // Background inside
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        IconButton(
            onClick = onSendClick,
            enabled = isSendEnabled,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSendEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Message",
                tint = if (isSendEnabled) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}