package com.example.harmony.presentation.main.voice

sealed class VoiceChannelEvent {
    object JoinChannel : VoiceChannelEvent()
    object LeaveChannel : VoiceChannelEvent()
    object ToggleMute : VoiceChannelEvent()

}