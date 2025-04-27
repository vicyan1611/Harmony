package com.example.harmony.presentation.main.join_server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.use_case.server.JoinServerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinServerViewModel @Inject constructor(
    private val joinServerUseCase: JoinServerUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JoinServerState())
    val state: StateFlow<JoinServerState> = _state.asStateFlow()

    fun onEvent(event: JoinServerEvent) {
        when (event) {
            is JoinServerEvent.OnLinkChange -> {
                _state.update { it.copy(inviteLink = event.link, error = null) } // Clear error on change
            }
            is JoinServerEvent.OnJoinClick -> {
                joinServer()
            }
            is JoinServerEvent.OnDismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun joinServer() {
        val link = state.value.inviteLink.trim()
        if (link.isBlank()) {
            _state.update { it.copy(error = "Invite link cannot be empty.") }
            return
        }

        joinServerUseCase(link).onEach { result ->
            _state.update {
                when (result) {
                    is Resource.Loading -> it.copy(isLoading = true, error = null)
                    is Resource.Success -> it.copy(isLoading = false, error = null, joinSuccess = true)
                    is Resource.Error -> it.copy(isLoading = false, error = result.message)
                }
            }
        }.launchIn(viewModelScope)
    }
}