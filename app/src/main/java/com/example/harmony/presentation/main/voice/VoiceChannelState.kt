package com.example.harmony.presentation.main.voice


import com.example.harmony.data.rtc.AgoraRtcManager
import com.example.harmony.data.rtc.VoiceParticipant

data class VoiceChannelState(
    val serverId: String = "",
    val channelId: String = "",
    val channelName: String = "Voice Channel",
    val participants: List<VoiceParticipant> = emptyList(),
    val connectionState: AgoraRtcManager.ConnectionState = AgoraRtcManager.ConnectionState.DISCONNECTED,
    val isLocalMuted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)