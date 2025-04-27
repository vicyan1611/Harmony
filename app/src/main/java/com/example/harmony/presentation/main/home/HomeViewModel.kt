// harmony/presentation/main/home/HomeViewModel.kt
package com.example.harmony.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.ServerWithChannels // Import ServerWithChannels
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.use_case.LogoutUseCase
// Import the new use case
import com.example.harmony.domain.use_case.server.GetServersAndChannelsByUserIdUseCase
import com.example.harmony.presentation.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define Events for UI interactions
sealed class HomeEvent {
    data class OnServerSelected(val server: ServerWithChannels) : HomeEvent()
    object OnAddServerClicked : HomeEvent()
    object OnLogoutClicked : HomeEvent()
    // Add other events like OnChannelSelected if needed later
}


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase,
    // Inject the use case
    private val getServersAndChannelsByUserIdUseCase: GetServersAndChannelsByUserIdUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    init {
        loadCurrentUser()
        loadServersAndChannels() // Load servers on init
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnServerSelected -> {
                _state.update { it.copy(selectedServer = event.server) }
            }
            is HomeEvent.OnAddServerClicked -> {
                viewModelScope.launch {
                    // Navigate to the Create Server screen
                    _navigationEvent.emit(NavRoutes.CREATE_SERVER)
                }
            }
            is HomeEvent.OnLogoutClicked -> {
                logout()
            }
        }
    }


    private fun loadCurrentUser() {
        viewModelScope.launch {
            // Use specific state flags
            _state.update { it.copy(isUserLoading = true, userLoadError = null) }
            try {
                // Assuming getCurrentUser might fetch from Firestore and needs to be async
                // For simplicity, using the potentially cached version. Adapt if fetch is needed.
                val currentUser = authRepository.getCurrentUser() // Ensure this provides needed data or make it async
                _state.update { it.copy(isUserLoading = false, user = currentUser) }

            } catch (e: Exception) {
                _state.update { it.copy(isUserLoading = false, userLoadError = "Failed to load user data") }
            }
        }
    }

    private fun loadServersAndChannels() {
        val userId = authRepository.getCurrentUser()?.id // Get current user ID
        if (userId == null) {
            _state.update { it.copy(isLoadingServers = false, serversLoadError = "User not logged in") }
            return
        }

        getServersAndChannelsByUserIdUseCase(userId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoadingServers = true, serversLoadError = null) }
                }
                is Resource.Success -> {
                    val servers = result.data ?: emptyList()
                    _state.update {
                        it.copy(
                            isLoadingServers = false,
                            serversWithChannels = servers,
                            // Optionally select the first server by default?
                            // selectedServer = servers.firstOrNull()
                            selectedServer = it.selectedServer ?: servers.firstOrNull() // Keep selection if already made
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingServers = false,
                            serversLoadError = result.message ?: "Failed to load servers"
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }


    private fun logout() { // Renamed from original HomeViewModel's logout
        logoutUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoggingOut.update { true }
                    // Clear server error on logout attempt
                    _state.update { it.copy(serversLoadError = null, userLoadError = null) }
                }
                is Resource.Success -> {
                    _isLoggingOut.update { false }
                    _navigationEvent.emit(NavRoutes.LOGIN)
                }
                is Resource.Error -> {
                    _isLoggingOut.update { false }
                    // Show logout error, could be in userLoadError field or a separate one
                    _state.update { it.copy(userLoadError = result.message ?: "Logout failed") }
                }
            }
        }.launchIn(viewModelScope)
    }
}