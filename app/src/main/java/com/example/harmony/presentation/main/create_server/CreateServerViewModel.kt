package com.example.harmony.presentation.main.create_server

import androidx.lifecycle.ViewModel
import com.example.harmony.core.common.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.harmony.domain.use_case.server.CreateServerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define commands for clarity
sealed class NavigationCommand {
    data class NavigateTo(val route: String) : NavigationCommand()
    // --- Modify NavigateBack ---
    data class NavigateBackWithResult(val result: Pair<String, Any>) : NavigationCommand()
    object NavigateBack : NavigationCommand() // Keep simple back navigation if needed elsewhere
}

@HiltViewModel
class CreateServerViewModel @Inject constructor (val createServerUseCase: CreateServerUseCase): ViewModel() {
    private val _uiState = MutableStateFlow(CreateServerUiState())
    val uiState: StateFlow<CreateServerUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationCommand>() // Emits the route string
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: CreateServerEvent) {
        when (event) {
            is CreateServerEvent.NameChanged -> {
                _uiState.update { it.copy(serverName = event.name) }
            }

            is CreateServerEvent.ImageSelected -> {
                _uiState.update { it.copy(selectedImageUri = event.uri) }
            }

            is CreateServerEvent.CreateServerClicked -> {
                createServer()
            }

            is CreateServerEvent.InviteDismissed -> { // Handle dismiss
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationCommand.NavigateBackWithResult("server_created" to true))
                }
            }
        }
    }

    private fun createServer() {
        val imageUri = uiState.value.selectedImageUri
        val serverName = uiState.value.serverName
        if (serverName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Server name cannot be empty") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // Assume CreateServerUseCase handles image upload/conversion and returns Resource<Server> (including id to create invite link)
        createServerUseCase(serverName, imageUri).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            createdServerInviteLink = result.data // Assuming data is the invite link
                        )
                    }
                    _navigationEvent.emit(NavigationCommand.NavigateTo(ServerCreationScreen.ServerInvite.route))
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }
}