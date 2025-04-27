package com.example.harmony.presentation.main.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.ParticipantInfo
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.DirectMessageRepository
import com.example.harmony.domain.use_case.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class UserSearchNavigationEvent {
    data class NavigateToDmChat(val conversationId: String) : UserSearchNavigationEvent()
}

@HiltViewModel
@OptIn(FlowPreview::class)
class UserSearchViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val dmRepository: DirectMessageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UserSearchState())
    val state: StateFlow<UserSearchState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<UserSearchNavigationEvent>()
    val navigationEvent: SharedFlow<UserSearchNavigationEvent> = _navigationEvent.asSharedFlow()


    init {
        loadCurrentUser()
        // Debounce search queries
        viewModelScope.launch {
            _state.map { it.searchQuery }
                .distinctUntilChanged()
                .debounce(300)
                .collectLatest { query ->
                    if (query.length >= 2) { // Start search after 2 chars
                        performSearch(query)
                    } else {
                        _state.update { it.copy(searchResults = emptyList(), error = null, isLoading = false) }
                    }
                }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _state.update {
                it.copy(
                    currentUserId = user?.id,
                    currentUserInfo = user?.let { u -> ParticipantInfo(u.displayName, u.photoUrl) }
                )
            }
        }
    }

    private fun performSearch(query: String) {
        searchUsersUseCase(query).onEach { result ->
            _state.update {
                when (result) {
                    is Resource.Loading -> it.copy(isLoading = true, error = null)
                    is Resource.Success -> it.copy(isLoading = false, searchResults = result.data ?: emptyList())
                    is Resource.Error -> it.copy(isLoading = false, error = result.message, searchResults = emptyList())
                }
            }
        }.launchIn(viewModelScope)
    }


    fun onEvent(event: UserSearchEvent) {
        when (event) {
            is UserSearchEvent.OnQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is UserSearchEvent.OnUserSelected -> {
                createOrGetConversation(event.selectedUser)
            }
            is UserSearchEvent.ClearSearch -> {
                _state.update { it.copy(searchQuery = "", searchResults = emptyList(), error = null) }
            }
        }
    }

    private fun createOrGetConversation(selectedUser: User) {
        val currentUser = state.value.currentUserInfo ?: return // Need current user info
        val currentUserId = state.value.currentUserId ?: return // Need current user id

        val selectedUserInfo = ParticipantInfo(selectedUser.displayName, selectedUser.photoUrl)

        dmRepository.getOrCreateDirectMessageConversation(
            userId1 = currentUserId,
            userId2 = selectedUser.id,
            userInfo1 = currentUser,
            userInfo2 = selectedUserInfo
        ).onEach { result ->
            _state.update { // Handle loading/error for conversation creation
                when (result) {
                    is Resource.Loading -> it.copy(isLoading = true, error = null) // Reuse isLoading flag?
                    is Resource.Success -> {
                        val conversationId = result.data
                        if (conversationId != null) {
                            _navigationEvent.emit(UserSearchNavigationEvent.NavigateToDmChat(conversationId))
                        } else {
                            // Handle case where conversation ID is null unexpectedly
                            it.copy(isLoading = false, error = "Failed to get conversation ID")
                        }
                        it.copy(isLoading = false) // Reset loading on success before navigation emit
                    }
                    is Resource.Error -> it.copy(isLoading = false, error = "DM Error: ${result.message}")
                }
            }
        }.launchIn(viewModelScope)
    }


}