// Create file: harmony/presentation/main/config_server/ConfigServerViewModel.kt
package com.example.harmony.presentation.main.config_server

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Server
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ServerRepository
import com.example.harmony.domain.use_case.server.DeleteServerUseCase
import com.example.harmony.domain.use_case.server.UpdateServerUseCase
// Assume these use cases exist or repository methods are available
// import com.example.harmony.domain.use_case.server.DeleteServerUseCase
// import com.example.harmony.domain.use_case.server.UpdateServerUseCase
// import com.example.harmony.domain.use_case.server.GetServerDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
class ConfigServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository, // Using repository directly for simplicity
    private val authRepository: AuthRepository,
    private val updateServerUseCase: UpdateServerUseCase, // Inject UseCases if created
    private val deleteServerUseCase: DeleteServerUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serverId: String = savedStateHandle.get<String>("serverId") ?: ""

    // Add a flag specifically for successful operation needing refresh
    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<com.example.harmony.presentation.main.create_server.NavigationCommand>() // Emits the route string
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _state = MutableStateFlow(ConfigServerState(serverId = serverId))
    val state: StateFlow<ConfigServerState> = _state.asStateFlow()

    init {
        if (serverId.isNotEmpty()) {
            loadServerDetails()
        } else {
            _state.update { it.copy(error = "Server ID is missing.") }
        }
    }

    fun onEvent(event: ConfigServerEvent) {
        when (event) {
            is ConfigServerEvent.LoadServerDetails -> loadServerDetails()
            is ConfigServerEvent.OnNameChange -> {
                _state.update { it.copy(currentName = event.newName, updateError = null) }
            }
            is ConfigServerEvent.OnProfilePictureSelected -> {
                _state.update { it.copy(selectedImageUri = event.uri, updateError = null) }
                _state.update { it.copy(updateError = "Changing profile picture is not implemented yet.") }
            }
            is ConfigServerEvent.OnSaveChangesClick -> updateServerDetails()
            is ConfigServerEvent.OnDeleteServerClick -> _state.update { it.copy(showDeleteConfirmation = true, deleteError = null) }
            is ConfigServerEvent.OnConfirmDeleteServer -> deleteServer()
            is ConfigServerEvent.OnCancelDeleteServer -> _state.update { it.copy(showDeleteConfirmation = false) }
            is ConfigServerEvent.OnDismissError -> _state.update { it.copy(error = null, deleteError = null, updateError = null, /* Reset navigateBack if needed */ navigateBack = false) }
            // Add event to reset the success flag after navigation has occurred
            ConfigServerEvent.OnNavigationHandled -> _operationSuccess.value = false
        }
    }

    private fun loadServerDetails() {
        val currentUserId = authRepository.getCurrentUser()?.id

        serverRepository.getServerById(serverId).onEach { result ->
            _state.update { // Use update for atomic state changes
                when (result) {
                    is Resource.Loading -> it.copy(isLoadingDetails = true, error = null)
                    is Resource.Success -> {
                        val server = result.data
                        val canConfigure = server?.ownerId == currentUserId
                        it.copy(
                            isLoadingDetails = false,
                            serverDetails = server,
                            currentName = server?.name ?: "",
                            currentProfileUrl = server?.profileUrl,
                            canUserConfigure = canConfigure,
                            error = if (!canConfigure && server != null) "You don't have permission to configure this server." else null
                        )
                    }
                    is Resource.Error -> it.copy(
                        isLoadingDetails = false,
                        error = result.message ?: Constants.ERROR_SOMETHING_WENT_WRONG
                    )
                }
            }
        }.launchIn(viewModelScope)
    }


    private fun updateServerDetails() {
        if (!_state.value.canUserConfigure) return

        val originalServer = _state.value.serverDetails ?: return
        val newName = _state.value.currentName.trim()
        val newImageUri = _state.value.selectedImageUri

        if (newName.isBlank()) {
            _state.update { it.copy(updateError = "Server name cannot be empty.") }
            return
        }

        if (newName == originalServer.name && newImageUri == null) {
            _state.update { it.copy(updateError = "No changes detected.") }
            return // No changes made
        }

        // Use the refactored UseCase from the previous step
        updateServerUseCase(originalServer.id, newName, newImageUri)
            .onEach { res -> // Use onEach, it's cleaner for multiple states
                _state.update { // Update state within onEach
                    when (res) {
                        is Resource.Loading -> it.copy(isUpdating = true, updateError = null)
                        is Resource.Success -> {
                            // Signal success for navigation and refresh
                            _operationSuccess.value = true // Use the dedicated flag
                            it.copy(isUpdating = false, updateError = null, navigateBack = true) // Trigger navigation state
                        }
                        is Resource.Error -> it.copy(isUpdating = false, updateError = res.message)
                    }
                }
            }.launchIn(viewModelScope) // Launch the collection
    }


    private fun deleteServer() {
        if (!_state.value.canUserConfigure) return

        // Use the refactored UseCase from the previous step
        deleteServerUseCase(serverId)
            .onEach { res -> // Use onEach
                when (res) {
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(
                                isDeleting = true,
                                showDeleteConfirmation = false, // Hide dialog once loading starts
                                deleteError = null
                            )
                        }
                    }
                    is Resource.Success -> {
                        // Signal success for navigation and refresh
                        _operationSuccess.value = true // Use the dedicated flag
                        _state.update {
                            it.copy(
                                isDeleting = false,
                                deleteError = null,
                                navigateBack = true // Trigger navigation state
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isDeleting = false,
                                // Keep confirmation dialog hidden on error? Or show it again? Your choice.
                                showDeleteConfirmation = false,
                                deleteError = res.message
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope) // Launch the collection
    }
}