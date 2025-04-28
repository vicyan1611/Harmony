package com.example.harmony.data.rtc


import android.content.Context
import android.util.Log
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.PresenceRepository // Import PresenceRepository
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class VoiceParticipant(
    val uid: Int,
    val isMuted: Boolean = false,
    val displayName: String? = null,
    val photoUrl: String? = null
)

@Singleton
class AgoraRtcManager @Inject constructor(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val presenceRepository: PresenceRepository // Inject PresenceRepository
) {
    // Use your real App ID
    private val agoraAppId: String = "914b0ae08d0f4b40a236c0e042f1075f"

    private var rtcEngine: RtcEngine? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO) // Scope for repository calls

    // ... StateFlows (_participants, _connectionState, _isLocalMuted) ...
    private val _participants = MutableStateFlow<Map<Int, VoiceParticipant>>(emptyMap())
    val participants: StateFlow<Map<Int, VoiceParticipant>> = _participants.asStateFlow()
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    private val _isLocalMuted = MutableStateFlow(false)
    val isLocalMuted: StateFlow<Boolean> = _isLocalMuted.asStateFlow()

    private var currentChannelId: String? = null
    private var currentFirebaseUserId: String? = null // Store Firebase UID
    private var currentAgoraUid: Int = 0

    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, FAILED }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onTokenPrivilegeWillExpire(token: String?) {
            Log.w("AgoraRtcManager", "Agora Token will expire soon. Renewal logic needed.")
            // TODO: Implement token renewal logic here for production
            // 1. Make a request to your backend/cloud function to get a new token
            // 2. Call rtcEngine?.renewToken(newToken)
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            Log.i("AgoraRtcManager", "Connection state changed: State $state, Reason $reason")
            _connectionState.update {
                when (state) {
                    Constants.CONNECTION_STATE_CONNECTING,
                    Constants.CONNECTION_STATE_RECONNECTING -> ConnectionState.CONNECTING
                    Constants.CONNECTION_STATE_CONNECTED -> ConnectionState.CONNECTED
                    Constants.CONNECTION_STATE_FAILED -> ConnectionState.FAILED
                    Constants.CONNECTION_STATE_DISCONNECTED -> {
                        // If disconnected unexpectedly, try cleaning up presence
                        currentChannelId?.let { chanId ->
                            currentFirebaseUserId?.let { fbUid ->
                                if (currentAgoraUid != 0) { // Ensure we have Agora UID
                                    managerScope.launch {
                                        presenceRepository.setUserOffline(chanId, fbUid, currentAgoraUid).collect()
                                        Log.d("AgoraRtcManager", "Cleaned up presence for $fbUid on unexpected disconnect")
                                    }
                                }
                            }
                        }
                        ConnectionState.DISCONNECTED
                    }
                    else -> it
                }
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.i("AgoraRtcManager", "Remote user joined: $uid")
            // Add placeholder participant first
            _participants.update { currentMap ->
                currentMap + (uid to VoiceParticipant(uid = uid))
            }
            // Attempt to set online in presence (using Agora UID for now)
            currentChannelId?.let { chanId ->
                // Need Firebase UID for presence - fetch mapping
                managerScope.launch {
                    presenceRepository.getUserIdFromAgoraUid(uid).collectLatest { res ->
                        if (res is Resource.Success && res.data != null) {
                            val firebaseUid = res.data
                            presenceRepository.setUserOnline(chanId, firebaseUid, uid).collect()
                            // TODO: Fetch full user details from Firebase (UserRepository) using firebaseUid
                            // val userDetails = userRepository.getCollectionUser(firebaseUid).firstOrNull()?.data
                            // Update the participant in the map with display name, photoUrl etc.
                            _participants.update { currentMap ->
                                currentMap[uid]?.copy(/* displayName = userDetails?.displayName, photoUrl = userDetails?.photoUrl */)
                                    ?.let { updatedParticipant -> currentMap + (uid to updatedParticipant) }
                                    ?: currentMap
                            }
                        } else {
                            Log.w("AgoraRtcManager", "Could not find Firebase UID for Agora UID: $uid")
                            // Keep placeholder with UID only
                        }
                    }
                }
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i("AgoraRtcManager", "Remote user offline: $uid, Reason: $reason")
            _participants.update { currentMap -> currentMap - uid }
            // Remove from presence
            currentChannelId?.let { chanId ->
                managerScope.launch {
                    // Fetch mapping again to get Firebase UID for removal
                    presenceRepository.getUserIdFromAgoraUid(uid).collectLatest { res ->
                        if (res is Resource.Success && res.data != null) {
                            presenceRepository.setUserOffline(chanId, res.data, uid).collect()
                        } else {
                            Log.w("AgoraRtcManager", "Could not find Firebase UID for Agora UID $uid during offline event.")
                        }
                    }
                }
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.i("AgoraRtcManager", "Joined channel $channel successfully with uid $uid")
            _connectionState.update { ConnectionState.CONNECTED }
            currentAgoraUid = uid
            currentFirebaseUserId = authRepository.getCurrentUser()?.id // Get current Firebase UID

            // Add self to participant list
            val self = authRepository.getCurrentUser()
            _participants.update { currentMap ->
                currentMap + (uid to VoiceParticipant(
                    uid = uid,
                    displayName = self?.displayName,
                    photoUrl = self?.photoUrl
                ))
            }

            // Set self online in presence and store mapping
            currentChannelId?.let { chanId ->
                currentFirebaseUserId?.let { fbUid ->
                    managerScope.launch {
                        // Store the mapping first
                        presenceRepository.storeUserAgoraUidMapping(fbUid, uid).collect()
                        // Then set online
                        presenceRepository.setUserOnline(chanId, fbUid, uid).collect()
                        Log.d("AgoraRtcManager", "Local user $fbUid set online in $chanId (Agora UID: $uid)")
                    }
                }
            }
        }

        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            // ... (mute handling as before) ...
            Log.d("AgoraRtcManager", "Remote audio state changed: UID=$uid, State=$state, Reason=$reason")
            if (state == Constants.REMOTE_AUDIO_STATE_STOPPED && (reason == Constants.REMOTE_AUDIO_REASON_REMOTE_MUTED || reason == Constants.REMOTE_AUDIO_REASON_LOCAL_MUTED)) {
                _participants.update { currentMap ->
                    currentMap[uid]?.copy(isMuted = true)
                        ?.let { updatedParticipant -> currentMap + (uid to updatedParticipant) }
                        ?: currentMap // Keep map unchanged if user not found (shouldn't happen)
                }
            } else if (state == Constants.REMOTE_AUDIO_STATE_STARTING || state == Constants.REMOTE_AUDIO_STATE_DECODING) {
                _participants.update { currentMap ->
                    currentMap[uid]?.copy(isMuted = false)
                        ?.let { updatedParticipant -> currentMap + (uid to updatedParticipant) }
                        ?: currentMap
                }
            }
        }
    }

    fun initialize() {
        if (rtcEngine != null) return
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = agoraAppId
            config.mEventHandler = rtcEventHandler

            rtcEngine = RtcEngine.create(config)
            rtcEngine?.enableAudio()
            rtcEngine?.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING);


            Log.i("AgoraRtcManager", "Agora RTC Engine Initialized")

        } catch (e: Exception) {
            Log.e("AgoraRtcManager", "RTC Engine Initialization Failed: ${e.message}")
            rtcEngine = null
        }
    }

    fun joinChannel(channelId: String, token: String? = null) {
        // ... (checks for engine init and connection state as before) ...
        if (rtcEngine == null) {
            Log.e("AgoraRtcManager", "RTC Engine not initialized.")
            _connectionState.update { ConnectionState.FAILED }
            return
        }
        if (connectionState.value != ConnectionState.DISCONNECTED) {
            Log.w("AgoraRtcManager", "Already connected or connecting.")
            return
        }

        this.currentChannelId = channelId
        _connectionState.update { ConnectionState.CONNECTING }
        Log.i("AgoraRtcManager", "Attempting to join channel: $channelId")

        val options = ChannelMediaOptions()
        options.autoSubscribeAudio = true
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        // Use 0 for UID to let Agora assign one. We'll get it in onJoinChannelSuccess
        val result = rtcEngine?.joinChannel(token, channelId, 0, options)

        if (result != Constants.ERR_OK) {
            Log.e("AgoraRtcManager", "Failed to join channel: $channelId, Error code: $result")
            _connectionState.update { ConnectionState.FAILED }
        }
    }

    fun leaveChannel() {
        // ... (checks for engine init and connection state as before) ...
        if (rtcEngine == null || connectionState.value == ConnectionState.DISCONNECTED) {
            return
        }
        Log.i("AgoraRtcManager", "Leaving channel: $currentChannelId")

        // Update presence *before* leaving the channel
        currentChannelId?.let { chanId ->
            currentFirebaseUserId?.let { fbUid ->
                if (currentAgoraUid != 0) {
                    managerScope.launch {
                        presenceRepository.setUserOffline(chanId, fbUid, currentAgoraUid).collect {
                            Log.d("AgoraRtcManager", "Presence updated for $fbUid before leaving $chanId")
                            // Now actually leave the Agora channel after presence update attempt
                            rtcEngine?.leaveChannel()
                            // Reset state *after* leaving
                            _connectionState.update { ConnectionState.DISCONNECTED }
                            _participants.update { emptyMap() }
                            _isLocalMuted.update { false }
                            currentChannelId = null
                            currentFirebaseUserId = null
                            currentAgoraUid = 0
                        }
                    }
                    // Don't leave channel or reset state immediately here, wait for coroutine
                    return // Exit function early, coroutine will handle the rest
                }
            }
        }

        // Fallback if presence update isn't needed or fails to launch
        rtcEngine?.leaveChannel()
        _connectionState.update { ConnectionState.DISCONNECTED }
        _participants.update { emptyMap() }
        _isLocalMuted.update { false }
        currentChannelId = null
        currentFirebaseUserId = null
        currentAgoraUid = 0
    }

    fun toggleLocalAudioMute() {
        // ... (logic as before) ...
        val currentlyMuted = _isLocalMuted.value
        val targetMuteState = !currentlyMuted
        val result = rtcEngine?.muteLocalAudioStream(targetMuteState)
        if (result == Constants.ERR_OK) {
            _isLocalMuted.update { targetMuteState }
            Log.i("AgoraRtcManager", "Local audio muted: $targetMuteState")
        } else {
            Log.e("AgoraRtcManager", "Failed to toggle mute state: $result")
        }
    }

    fun destroy() {
        Log.i("AgoraRtcManager", "Destroying Agora RTC Engine")
        leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
        managerScope.cancel() // Cancel the scope
    }
}