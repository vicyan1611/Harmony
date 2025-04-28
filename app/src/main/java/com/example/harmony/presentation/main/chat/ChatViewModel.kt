package com.example.harmony.presentation.main.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.MessageRepository
import com.example.harmony.domain.use_case.chat.SendChatMessageUseCase
import com.example.harmony.domain.repository.UserRepository
import com.example.harmony.domain.use_case.chat.ReactToMessageUseCase
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
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val reactToMessageUseCase: ReactToMessageUseCase,
    private val userRepository: UserRepository
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
            val user = authRepository.getCurrentUser() ?: return@launch
            userRepository.getCollectionUser(user.id).collect { res ->
                when (res) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                currentUser = res.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = res.message
                            )
                        }
                    }
                }
            }
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
            is ChatEvent.OnPickImageClick -> {
                // Handled in UI by launching picker
            }
            is ChatEvent.OnImageSelected -> {
                _state.update { it.copy(selectedImageUri = event.uri) }
            }
            is ChatEvent.OnClearSelectedImage -> {
                _state.update { it.copy(selectedImageUri = null) }
            }
            is ChatEvent.OnReactToMessage -> {
                reactToMessage(event.messageId, event.emojiIndex)
            }
            is ChatEvent.OnRemoveReaction -> {
                removeReaction(event.messageId)
            }
        }
    }

    private fun sendMessage() {
        val currentInput = state.value.currentMessageInput
        val imageUri = state.value.selectedImageUri
        val currentServerId = serverId ?: return
        val currentChannelId = channelId ?: return

        if (state.value.isSending || (currentInput.isBlank() && imageUri == null)) {
            return
        }

        _state.update { it.copy(isSending = true, error = null) } // Set sending state


        sendChatMessageUseCase(
            serverId = currentServerId,
            channelId = currentChannelId,
            text = currentInput,
            imageUri = imageUri
        ).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isSending = false,
                            currentMessageInput = "", // Clear input on success
                            selectedImageUri = null // Clear image on success
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isSending = false,
                            error = "Send Error: ${result.message}" // Show error
                        )
                    }
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _state.update { it.copy(error = null) }
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun reactToMessage(messageId: String, emojiIndex: Int) {
        val currentUserId = state.value.currentUser?.id ?: return
        val message = state.value.messages.find { it.id == messageId } ?: return

        // Check if user is clicking the same emoji they already reacted with
        if (message.currentUserReactionIndex == emojiIndex) {
            removeReaction(messageId) // Call remove logic
        } else {
            // Optimistic UI Update (optional but improves UX)
            updateLocalMessageReaction(messageId, emojiIndex)

            // Call Use Case
            reactToMessageUseCase(
                serverId = serverId,
                channelId = channelId,
                messageId = messageId,
                emojiIndex = emojiIndex
            ).onEach { result ->
                if (result is Resource.Error) {
                    // Revert optimistic update on error (optional)
                    updateLocalMessageReaction(messageId, message.currentUserReactionIndex) // Revert to original
                    Log.e("ChatViewModel", "Failed to react: ${result.message}")
                    // Show error to user?
                    _state.update { it.copy(error = "Reaction failed: ${result.message}") }
                    viewModelScope.launch { kotlinx.coroutines.delay(3000); _state.update { it.copy(error = null) } }
                }
                // Success case is handled by Firestore listener updating the state naturally,
                // or by the optimistic update if you keep it.
            }.launchIn(viewModelScope)
        }
    }

    private fun removeReaction(messageId: String) {
        val currentUserId = state.value.currentUser?.id ?: return
        val message = state.value.messages.find { it.id == messageId } ?: return

        // Optimistic UI Update
        updateLocalMessageReaction(messageId, null) // Set to null locally

        // Call Use Case with null index
        reactToMessageUseCase(
            serverId = serverId,
            channelId = channelId,
            messageId = messageId,
            emojiIndex = null // Indicate removal
        ).onEach { result ->
            if (result is Resource.Error) {
                // Revert optimistic update on error (optional)
                updateLocalMessageReaction(messageId, message.currentUserReactionIndex) // Revert to original
                Log.e("ChatViewModel", "Failed to remove reaction: ${result.message}")
                // Show error to user?
                _state.update { it.copy(error = "Remove reaction failed: ${result.message}") }
                viewModelScope.launch { kotlinx.coroutines.delay(3000); _state.update { it.copy(error = null) } }
            }
            // Success is handled by listener or optimistic update
        }.launchIn(viewModelScope)
    }

    // Helper for optimistic UI update
    private fun updateLocalMessageReaction(messageId: String, newEmojiIndex: Int?) {
        _state.update { currentState ->
            val updatedMessages = currentState.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.apply { currentUserReactionIndex = newEmojiIndex } // Update transient field
                } else {
                    msg
                }
            }
            currentState.copy(messages = updatedMessages)
        }
    }
}