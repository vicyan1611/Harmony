package com.example.harmony.presentation.main.voice

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.data.rtc.AgoraRtcManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceChannelViewModel @Inject constructor(
    private val agoraRtcManager: AgoraRtcManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: String = savedStateHandle.get<String>("serverId") ?: ""
    private val channelId: String = savedStateHandle.get<String>("channelId") ?: ""

    private val _state =
        MutableStateFlow(VoiceChannelState(serverId = serverId, channelId = channelId))
    val state: StateFlow<VoiceChannelState> = _state.asStateFlow()

    private var observerJob: Job? = null

    init {
        if (serverId.isNotEmpty() && channelId.isNotEmpty()) {
            // TODO: Fetch actual channel name from repository if needed
            // _state.update { it.copy(channelName = fetchedName) }
            startObservingRtcState()
            // Automatically attempt to join when ViewModel is created
            onEvent(VoiceChannelEvent.JoinChannel)
        } else {
            _state.update { it.copy(error = "Server or Channel ID missing") }
        }
    }

    private fun startObservingRtcState() {
        observerJob?.cancel() // Cancel previous observers if any
        observerJob = viewModelScope.launch {
            // Observe participants
            launch {
                agoraRtcManager.participants.collect { participantsMap ->
                    _state.update { it.copy(participants = participantsMap.values.toList()) }
                    // TODO: Here you could fetch display names/photos from your UserRepository
                    // based on the UIDs if you have a mapping mechanism.
                }
            }
            // Observe connection state
            launch {
                agoraRtcManager.connectionState.collect { connectionState ->
                    _state.update {
                        it.copy(
                            connectionState = connectionState,
                            isLoading = connectionState == AgoraRtcManager.ConnectionState.CONNECTING,
                            error = if (connectionState == AgoraRtcManager.ConnectionState.FAILED) "Connection failed" else it.error
                        )
                    }
                }
            }
            // Observe local mute state
            launch {
                agoraRtcManager.isLocalMuted.collect { isMuted ->
                    _state.update { it.copy(isLocalMuted = isMuted) }
                }
            }
        }
    }

    fun onEvent(event: VoiceChannelEvent) {
        when (event) {
            VoiceChannelEvent.JoinChannel -> {
                // TODO: Fetch token if using token authentication
                val token: String? = null // Replace with actual token fetching if needed
                agoraRtcManager.joinChannel(channelId, token)
            }

            VoiceChannelEvent.LeaveChannel -> {
                agoraRtcManager.leaveChannel()
                // ViewModel will be cleared soon, state reset happens in manager
            }

            VoiceChannelEvent.ToggleMute -> {
                agoraRtcManager.toggleLocalAudioMute()
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d("VoiceChannelViewModel", "onCleared called, leaving channel.")
        agoraRtcManager.leaveChannel()
        observerJob?.cancel()
        // Optionally destroy engine if singleton manager isn't desired for longer lifecycle
        // agoraRtcManager.destroy()
    }
}