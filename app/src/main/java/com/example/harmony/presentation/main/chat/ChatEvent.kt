package com.example.harmony.presentation.main.chat

sealed class ChatEvent {
    data class OnMessageInputChange(val input: String) : ChatEvent()
    object OnSendMessageClick : ChatEvent()
}