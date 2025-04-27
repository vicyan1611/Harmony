package com.example.harmony.presentation.main.dm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.DirectMessageConversation
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


data class DirectMessageListState(
    val isLoading: Boolean = false,
    val conversations: List<DirectMessageConversation> = emptyList(),
    val error: String? = null,
    val currentUser: User? = null
)

@HiltViewModel
class DirectMessageListViewModel @Inject constructor(
    private val dmRepository: DirectMessageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DirectMessageListState())
    val state: StateFlow<DirectMessageListState> = _state

    init {
        loadCurrentUserAndConversations()
    }

    private fun loadCurrentUserAndConversations() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _state.update { it.copy(currentUser = user) }
            user?.id?.let { fetchConversations(it) }
        }
    }

    private fun fetchConversations(userId: String) {
        dmRepository.getDirectMessageConversations(userId).onEach { result ->
            when (result) {
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, conversations = result.data ?: emptyList(), error = null)
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }.launchIn(viewModelScope)
    }
}