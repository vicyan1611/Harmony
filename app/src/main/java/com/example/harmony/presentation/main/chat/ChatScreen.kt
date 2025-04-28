package com.example.harmony.presentation.main.chat

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.harmony.core.components.RoundedAvatar
import com.example.harmony.core.components.RoundedButton
import com.example.harmony.core.theme.Color
import com.example.harmony.domain.model.Message
import com.example.harmony.presentation.main.chat.messages.MessageSetting
import com.example.harmony.presentation.main.chat.messages.standardEmojis
import com.example.harmony.presentation.main.my_profile.MyProfile
import com.example.harmony.presentation.main.other_profile.OtherUserProfile
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle() // Use lifecycle-aware collection
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for MessageSetting bottom sheet
    var showMessageSettingSheet by remember { mutableStateOf(false) }
    val messageSettingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMessageIdForSheet by remember { mutableStateOf<String?>(null) }

    // Effect to handle programmatic sheet dismissal if needed
    LaunchedEffect(showMessageSettingSheet) {
        scope.launch {
            if (showMessageSettingSheet && !messageSettingSheetState.isVisible) {
                selectedMessageIdForSheet?.let { messageSettingSheetState.show() }
            } else if (!showMessageSettingSheet && messageSettingSheetState.isVisible) {
                messageSettingSheetState.hide()
            }
        }
    }
    // Handle sheet dismissal by user interaction (swipe/click outside)
    LaunchedEffect(messageSettingSheetState.targetValue) {
        if (messageSettingSheetState.targetValue == SheetValue.Hidden && showMessageSettingSheet) {
            showMessageSettingSheet = false
            selectedMessageIdForSheet = null
        }
    }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(ChatEvent.OnImageSelected(uri))
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            val lastIndex = state.messages.size - 1
            // Only scroll if not already near the bottom to avoid disrupting user scrolling up
            if (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == lastIndex -1 || !lazyListState.canScrollForward) {
                lazyListState.animateScrollToItem(lastIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.channelName) },
                navigationIcon = {
                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Column {

                if (state.selectedImageUri != null) {
                    Box(
                        modifier = Modifier
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
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        // Clear Button
                        IconButton(
                            onClick = { viewModel.onEvent(ChatEvent.OnClearSelectedImage) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                                    CircleShape
                                )

                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear selected image",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        // Show loading indicator during upload if needed
                        if (state.isSending && state.selectedImageUri != null) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.Center),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                MessageInputBar(
                    message = state.currentMessageInput,
                    onMessageChange = { viewModel.onEvent(ChatEvent.OnMessageInputChange(it)) },
                    onSendClick = { viewModel.onEvent(ChatEvent.OnSendMessageClick) },
                    onPickImageClick = { imagePickerLauncher.launch("image/*") }, // Trigger picker
                    isSendEnabled = (state.currentMessageInput.isNotBlank() || state.selectedImageUri != null) && !state.isSending // Enable based on text/image and sending state
                )
                // Display sending errors temporarily
                if (state.error?.contains("Send Error") == true) {
                    Text(
                        text = state.error ?: "Failed to send",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .background(MaterialTheme.colorScheme.errorContainer)
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
            if (state.isLoading && state.messages.isEmpty()) { // Initial loading
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
            } else if (state.messages.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No messages yet. Start the conversation!",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
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
                        MessageItem(message = message, currentUserId = state.currentUser?.id ?: "",
                            onLongClick = {
                                selectedMessageIdForSheet = message.id
                                showMessageSettingSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showMessageSettingSheet && selectedMessageIdForSheet != null) {
        MessageSetting(
            messageId = selectedMessageIdForSheet!!,
            sheetState = messageSettingSheetState,
            onDismissRequest = {
                showMessageSettingSheet = false
                selectedMessageIdForSheet = null
            },
            onEditMsg = { /* TODO */ },
            onReactSelected = { emojiIndex ->
                selectedMessageIdForSheet?.let { msgId ->
                    viewModel.onEvent(ChatEvent.OnReactToMessage(msgId, emojiIndex))
                }
                // Sheet dismissal is handled by the LaunchedEffect on targetValue
                // and the onClick within MessageSetting itself
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MessageItem(message: Message, currentUserId: String, onLongClick: () -> Unit) {
    val isCurrentUser = message.senderId == currentUserId
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isCurrentUser) Color.MessageBubbleMe else Color.MessageBubbleOther
    val textColor = MaterialTheme.colorScheme.onSurface

    val timestampFormatted = message.timestamp?.toDate()?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: ""
    val context = LocalContext.current

    // State for profile sheets (keep as is or refactor if needed)
    var otherUserProfileSheetState by remember { mutableStateOf(false) }
    var myUserProfileSheetState by remember { mutableStateOf(false) }

    // Determine the reaction emoji to display
    val reactionEmoji = message.currentUserReactionIndex?.let { index ->
        if (index in standardEmojis.indices) standardEmojis[index] else null // Get emoji from list
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement, // Align Row content
        verticalAlignment = Alignment.Bottom
    ) {
        // Show avatar only for other users
        if (!isCurrentUser) {
            RoundedButton(
                onClick = { otherUserProfileSheetState = true },
                modifier = Modifier.padding(end = 8.dp),
                size = 32.dp,
            ) {
                RoundedAvatar(
                    avatarImageUrl = message.senderPhotoUrl ?: "",
                    char = message.senderDisplayName.firstOrNull()?.uppercaseChar() ?: '?',
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Message bubble
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp) // Limit bubble width
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        onLongClick()
                    }
                )
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
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.End) // Align timestamp
            )
        }

        if (reactionEmoji != null) {
            // Position the reaction slightly offset below the bubble
            Box(
                modifier = Modifier
                    .padding(start = if (!isCurrentUser) 4.dp else 0.dp, end = if(isCurrentUser) 4.dp else 0.dp) // Indent slightly from bubble edge
                    .offset(y = (-4).dp) // Overlap slightly with bottom of bubble
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    //.align(if (isCurrentUser) Alignment.End else Alignment.Start) // Align reaction like bubble
            ) {
                Text(text = reactionEmoji, fontSize = 40.sp)
            }
        }

        if (message?.reactions != null) {
            // Position the reaction slightly offset below the bubble
            Box(
                modifier = Modifier
                    .padding(start = if (!isCurrentUser) 4.dp else 0.dp, end = if(isCurrentUser) 4.dp else 0.dp) // Indent slightly from bubble edge
                    .offset(y = (-4).dp) // Overlap slightly with bottom of bubble
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                //.align(if (isCurrentUser) Alignment.End else Alignment.Start) // Align reaction like bubble
            ) {
                Text(text = message.reactions.size.toString(), fontSize = 40.sp)
            }
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp)) // Add space between bubble and avatar
            RoundedButton(
                onClick = { myUserProfileSheetState = true },
                modifier = Modifier.padding(end = 8.dp),
                size = 32.dp,
            ) {
                RoundedAvatar(
                    avatarImageUrl = message.senderPhotoUrl ?: "",
                    char = message.senderDisplayName.firstOrNull()?.uppercaseChar() ?: '?',
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }


    if (otherUserProfileSheetState) {
        OtherUserProfile(
            displayedName = message.senderDisplayName,
            username = message.senderDisplayName,
            avatarUrl = message.senderPhotoUrl ?: "",
            onDismissRequest = { otherUserProfileSheetState = false },
            modifier = Modifier
        )
    }

    if (myUserProfileSheetState) {
        MyProfile(
            displayedName = message.senderDisplayName,
            username = message.senderDisplayName,
            avatarUrl = message.senderPhotoUrl ?: "",
            onDismissRequest = { myUserProfileSheetState = false },
            modifier = Modifier,
            hasSettings = false
        )
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
