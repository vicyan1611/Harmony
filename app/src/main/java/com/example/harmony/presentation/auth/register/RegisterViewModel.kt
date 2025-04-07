package com.example.harmony.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Constants.ERROR_FIELDS_EMPTY
import com.example.harmony.core.common.Constants.ERROR_PASSWORDS_NOT_MATCHING
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.use_case.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val registerUseCase: RegisterUseCase) :
    ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.OnUsernameChange -> {
                _state.update { it.copy(username = event.username) }
            }

            is RegisterEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }

            is RegisterEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }

            is RegisterEvent.OnConfirmPasswordChange -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword) }
            }

            is RegisterEvent.OnRegisterClick -> {
                register()
            }

            is RegisterEvent.OnLoginClick -> {
                // Navigation to login screen will be handled in the UI
            }
        }
    }

    private fun register() {
        val username = state.value.username
        val email = state.value.email
        val password = state.value.password
        val confirmPassword = state.value.confirmPassword

        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _state.update { it.copy(error = ERROR_FIELDS_EMPTY) }
            return
        }

        if (password != confirmPassword) {
            _state.update { it.copy(error = ERROR_PASSWORDS_NOT_MATCHING) }
            return
        }

        registerUseCase(username, email, password).onEach { result ->
            when (result) {
                is Resource.Loading<User> -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                }

                is Resource.Success<User> -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            error = null
                        )
                    }
                }

                is Resource.Error<User> -> {
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

}