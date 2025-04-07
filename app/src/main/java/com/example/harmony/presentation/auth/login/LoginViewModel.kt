package com.example.harmony.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Constants.ERROR_FIELDS_EMPTY
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.use_case.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }

            is LoginEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }

            is LoginEvent.OnLoginClick -> {
                login()
            }

            is LoginEvent.OnRegisterClick -> {
                // Navigation to register screen will be handled in the UI
            }

            is LoginEvent.OnForgotPasswordClick -> {
                // Navigation to forgot password screen will be handled in the UI
            }
        }
    }

    private fun login() {
        val email = state.value.email
        val password = state.value.password

        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = ERROR_FIELDS_EMPTY) }
            return
        }

        loginUseCase(email, password).onEach { result ->
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