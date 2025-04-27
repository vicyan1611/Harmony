package com.example.harmony.presentation.main.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state

    private val serverId: String = savedStateHandle.get<String>("serverId") ?: ""
    private val channelId: String = savedStateHandle.get<String>("channelId") ?: ""

    init {
        if (serverId.isNotEmpty() && channelId.isNotEmpty()) {
            loadCurrentUser()
            fetchMessages()

        } else {
            _state.update { it.copy(error = "Server or Channel ID missing") }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _state.update { it.copy(currentUser = user) }
        }
    }

    private fun fetchMessages() {
        messageRepository.getMessages(serverId, channelId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                }

                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            messages = result.data ?: emptyList(),
                            error = null
                        )
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.OnMessageInputChange -> {
                _state.update { it.copy(currentMessageInput = event.input) }
            }

            is ChatEvent.OnSendMessageClick -> {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        val currentUser = state.value.currentUser ?: return
        Log.d("ChatViewModel", "currentUser.email ${currentUser.email}")
        val text = state.value.currentMessageInput.trim()
        if (text.isBlank()) return

        val message = Message(
            channelId = channelId,
            senderId = currentUser.id,
            senderDisplayName = currentUser.displayName,
            senderPhotoUrl = currentUser.photoUrl,
            text = text
        )

        messageRepository.sendMessage(serverId, channelId, message).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    // Optionally show sending indicator
                    _state.update { it.copy(currentMessageInput = "") }
                }
                is Resource.Success -> {

                }
                is Resource.Error -> {
                    _state.update { it.copy(error = "Failed to send: ${result.message}") }

                }
            }
        }.launchIn(viewModelScope)

    }
}