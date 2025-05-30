// harmony/presentation/main/home/HomeViewModel.kt
package com.example.harmony.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ChannelRepository
import com.example.harmony.domain.repository.UserRepository
import com.example.harmony.domain.use_case.LogoutUseCase
// Import the new use case
import com.example.harmony.domain.use_case.server.GetServersAndChannelsByUserIdUseCase
import com.example.harmony.presentation.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationCommand {
    data class NavigateTo(val route: String) : NavigationCommand()
    data class NavigateBackWithResult(val result: Pair<String, Any>) : NavigationCommand()
    object NavigateBack : NavigationCommand()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val logoutUseCase: LogoutUseCase,
    private val getServersAndChannelsByUserIdUseCase: GetServersAndChannelsByUserIdUseCase,
    private val channelRepository: ChannelRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState(isRefreshing = false))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationCommand>()
    val navigationEvent: SharedFlow<NavigationCommand> = _navigationEvent.asSharedFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    init {
        loadCurrentUser()
        loadServersAndChannels() // Load servers on init
    }

    // Public function to allow triggering refresh from outside (e.g., screen resume)
    fun refreshData() {
        loadCurrentUser() // Reload user info if necessary
        loadServersAndChannels() // Reload server list
    }


    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnServerSelected -> {
                _state.update { it.copy(selectedServer = event.server) }
            }
            is HomeEvent.OnAddServerClicked -> {
                viewModelScope.launch {
                    // Navigate to the Create Server screen
                    _navigationEvent.emit(NavigationCommand.NavigateTo(NavRoutes.CREATE_SERVER))
                }
            }
            is HomeEvent.OnLogoutClicked -> {
                logout()
            }
            // Handle the refresh event
            HomeEvent.OnRefresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    loadServersAndChannels() // Reload data
                }
            }
            HomeEvent.OnShowAddChannelSheet -> {
                // Only show if a server is selected
                if (state.value.selectedServer != null) {
                    _state.update {
                        it.copy(
                            isAddChannelSheetVisible = true,
                            // Reset fields when showing
                            newChannelName = "",
                            newChannelDescription = "",
                            createChannelError = null,
                            isCreatingChannel = false
                        )
                    }
                }
                // Optionally, show a toast or message if no server is selected
            }
            HomeEvent.OnDismissAddChannelSheet -> {
                _state.update { it.copy(isAddChannelSheetVisible = false) }
            }
            is HomeEvent.OnNewChannelNameChange -> {
                if (event.name.length <= 50) { // Enforce max length
                    _state.update { it.copy(newChannelName = event.name) }
                }
            }
            is HomeEvent.OnNewChannelDescriptionChange -> {
                if (event.description.length <= 200) { // Enforce max length
                    _state.update { it.copy(newChannelDescription = event.description) }
                }
            }

            is HomeEvent.OnNewChannelTypeChange -> {
                _state.update { it.copy(newChannelType = event.type) }
            }

            HomeEvent.OnCreateChannelClicked -> {
                createChannel()
            }
            HomeEvent.OnShowMyProfileSheet -> {
                loadCurrentUser()
                if (state.value.user != null) { // Only show if user data is loaded
                    _state.update { it.copy(isMyProfileSheetVisible = true) }
                }
            }
            HomeEvent.OnDismissMyProfileSheet -> {
                _state.update { it.copy(isMyProfileSheetVisible = false) }
            }
            HomeEvent.OnNavigateToSettings -> {
                // Hide sheet before navigating
                _state.update { it.copy(isMyProfileSheetVisible = false) }
                viewModelScope.launch {
                    // You'll need to add NavRoutes.SETTINGS
                    _navigationEvent.emit(NavigationCommand.NavigateTo(NavRoutes.SETTINGS))
                }
            }
            HomeEvent.OnNavigateToConfigServer -> {
                val serverId = state.value.selectedServer?.server?.id // Get the ID
                if (serverId != null) {
                    viewModelScope.launch {
                        // Use the new helper function to build the route
                        _navigationEvent.emit(NavigationCommand.NavigateTo(NavRoutes.getConfigServerRoute(serverId)))
                    }
                } else {
                    // Handle error: No server selected
                    viewModelScope.launch {
                        _state.update { it.copy(serversLoadError = "Please select a server first.") }
                    }
                }
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
                userRepository.getCollectionUser(currentUser?.id ?: "").collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            // Handle loading if needed
                        }
                        is Resource.Success -> {
                            _state.update { it.copy(user = res.data) }
                        }
                        is Resource.Error -> {
                            _state.update { it.copy(userLoadError = res.message ?: "Failed to load user data") }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isUserLoading = false, userLoadError = "Failed to load user data") }
            }
        }
    }

    private fun loadServersAndChannels() {
        val userId = authRepository.getCurrentUser()?.id // Get current user ID
        if (userId == null) {
            // Also set isRefreshing to false here
            _state.update { it.copy(isLoadingServers = false, serversLoadError = "User not logged in", isRefreshing = false) }
            return
        }

        // Don't set isLoadingServers true if only refreshing
        if (!_state.value.isRefreshing) {
            _state.update { it.copy(isLoadingServers = true, serversLoadError = null) }
        }


        getServersAndChannelsByUserIdUseCase(userId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    // Only update if not already refreshing, otherwise keep isRefreshing = true
                    if (!_state.value.isRefreshing) {
                        _state.update { it.copy(isLoadingServers = true, serversLoadError = null) }
                    }
                }
                is Resource.Success -> {
                    val servers = result.data ?: emptyList()
                    _state.update {
                        it.copy(
                            isLoadingServers = false,
                            serversWithChannels = servers,
                            selectedServer = it.selectedServer ?: servers.firstOrNull(), // Keep selection if already made
                            isRefreshing = false // <-- Stop refreshing on success
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingServers = false,
                            serversLoadError = result.message ?: "Failed to load servers",
                            isRefreshing = false // <-- Stop refreshing on error
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun createChannel() {
        val name = state.value.newChannelName.trim()
        val description = state.value.newChannelDescription.trim()
        val type = state.value.newChannelType
        val serverId = state.value.selectedServer?.server?.id

        if (name.isBlank()) {
            _state.update { it.copy(createChannelError = "Channel name cannot be empty") }
            return
        }
        if (serverId == null) {
            _state.update { it.copy(createChannelError = "No server selected") }
            return
        }

        channelRepository.createChannel(name = name, description = description, serverId = serverId, type = type)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isCreatingChannel = true, createChannelError = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isCreatingChannel = false,
                                isAddChannelSheetVisible = false, // Close sheet on success
                                createChannelError = null
                            )
                        }
                        refreshData() // Reload server/channel list
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isCreatingChannel = false,
                                createChannelError = result.message ?: Constants.ERROR_SOMETHING_WENT_WRONG // Show error
                            )
                            // Don't close the sheet on error
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
                    _navigationEvent.emit(NavigationCommand.NavigateTo(NavRoutes.LOGIN))
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