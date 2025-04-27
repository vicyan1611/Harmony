package com.example.harmony.presentation.main.chat

import android.net.Uri
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.model.User

data class ChatState(
    val messages: List<Message> = emptyList(),
    val currentMessageInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val channelName: String = "Channel",
    val currentUser: User? = null,
    val selectedImageUri: Uri? = null,
    val isSending: Boolean = false
)