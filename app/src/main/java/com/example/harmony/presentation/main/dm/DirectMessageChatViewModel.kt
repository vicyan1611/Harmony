package com.example.harmony.presentation.main.dm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.model.ParticipantInfo
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.DirectMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DirectMessageChatState(
    val messages: List<Message> = emptyList(),
    val currentMessageInput: String = "",
    val isLoadingMessages: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val error: String? = null,
    val conversationId: String = "",
    val otherParticipantName: String = "Direct Message",
    val otherParticipantPhotoUrl: String? = null,
    val currentUser: User? = null
)

sealed class DirectMessageChatEvent {
    data class OnMessageInputChange(val input: String) : DirectMessageChatEvent()
    object OnSendMessageClick : DirectMessageChatEvent()
}

@HiltViewModel
class DirectMessageChatViewModel @Inject constructor(
    private val dmRepository: DirectMessageRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""

    private val _state = MutableStateFlow(DirectMessageChatState(conversationId = conversationId))
    val state: StateFlow<DirectMessageChatState> = _state

    init {
        if (conversationId.isNotEmpty()) {
            loadCurrentUserAndInitialData() // Combined loading logic
        } else {
            _state.update { it.copy(error = "Conversation ID missing") }
        }
    }

    private fun loadCurrentUserAndInitialData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _state.update { it.copy(currentUser = user) }
            if (user != null) {
                // Fetch details and messages concurrently or sequentially
                fetchConversationDetails(user.id) // Pass current user ID
                fetchMessages()
            } else {
                _state.update { it.copy(error = "Could not load current user.") }
            }
        }
    }

    fun onEvent(event: DirectMessageChatEvent) {
        when (event) {
            is DirectMessageChatEvent.OnMessageInputChange -> {
                _state.update { it.copy(currentMessageInput = event.input) }
            }
            is DirectMessageChatEvent.OnSendMessageClick -> {
                sendMessage()
            }
        }
    }

    private fun fetchConversationDetails(currentUserId: String) {
        dmRepository.getDirectMessageConversation(conversationId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoadingDetails = true) }
                }
                is Resource.Success -> {
                    val conversation = result.data
                    val otherParticipantInfo = conversation?.getOtherParticipant(currentUserId)
                    _state.update {
                        it.copy(
                            isLoadingDetails = false,
                            otherParticipantName = otherParticipantInfo?.displayName ?: "Unknown User",
                            otherParticipantPhotoUrl = otherParticipantInfo?.photoUrl,
                            // Clear error if details load successfully after a previous error
                            error = if (it.error?.contains("Conversation") == true) null else it.error
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingDetails = false,
                            // Set error specific to details loading
                            error = "Details Error: ${result.message}"
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchMessages() {
        // Slightly update state names for clarity
        dmRepository.getDirectMessages(conversationId).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.update { it.copy(isLoadingMessages = true) } // Use isLoadingMessages
                is Resource.Success -> _state.update {
                    it.copy(isLoadingMessages = false, messages = result.data ?: emptyList())
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoadingMessages = false, error = "Messages Error: ${result.message}")
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun sendMessage() {
        val currentUser = state.value.currentUser ?: return
        val text = state.value.currentMessageInput.trim()
        if (text.isBlank()) return

        // Ensure participant details are loaded before sending
        val otherParticipantName = state.value.otherParticipantName
        if (otherParticipantName == "Direct Message" || otherParticipantName == "Unknown User") {
            _state.update { it.copy(error = "Cannot send message: Participant details not loaded.") }
            return
        }

        val message = Message(
            senderId = currentUser.id,
            senderDisplayName = currentUser.displayName,
            senderPhotoUrl = currentUser.photoUrl,
            text = text
        )

        val currentUserInfo = ParticipantInfo(currentUser.displayName, currentUser.photoUrl)
        // Use the loaded details from the state
        val otherUserInfo = ParticipantInfo(state.value.otherParticipantName, state.value.otherParticipantPhotoUrl)

        dmRepository.sendDirectMessage(conversationId, message, currentUserInfo, otherUserInfo).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(currentMessageInput = "") }
                }
                is Resource.Success -> {
                    // Message sent successfully
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = "Failed to send: ${result.message}") }
                }
            }
        }.launchIn(viewModelScope)
    }

}