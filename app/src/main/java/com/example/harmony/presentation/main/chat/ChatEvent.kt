package com.example.harmony.presentation.main.chat

import android.net.Uri

sealed class ChatEvent {
    data class OnMessageInputChange(val input: String) : ChatEvent()
    object OnSendMessageClick : ChatEvent()
    object OnPickImageClick : ChatEvent()
    data class OnImageSelected(val uri: Uri?) : ChatEvent()
    object OnClearSelectedImage : ChatEvent()
}