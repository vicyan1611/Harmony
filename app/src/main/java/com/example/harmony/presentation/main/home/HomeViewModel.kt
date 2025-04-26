package com.example.harmony.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.harmony.domain.use_case.LogoutUseCase
import com.example.harmony.presentation.navigation.NavRoutes
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = authRepository.getCurrentUser()
                _state.update { it.copy(isLoading = false, user = currentUser) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to load user data") }
            }
        }
    }

    fun logout() {
        logoutUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoggingOut.update { true }
                    _state.update { it.copy(error = null) }
                }

                is Resource.Success -> {
                    _isLoggingOut.update { false }
                    _navigationEvent.emit(NavRoutes.LOGIN)
                }

                is Resource.Error -> {
                    _isLoggingOut.update { false }
                    _state.update { it.copy(error = result.message ?: "Logout failed") }
                }
            }
        }.launchIn(viewModelScope)
    }

}