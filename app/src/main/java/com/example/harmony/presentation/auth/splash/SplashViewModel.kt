package com.example.harmony.presentation.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // Add a small delay to show splash screen
            delay(1500)
            _isAuthenticated.update { authRepository.isUserAuthenticated() }
            _isLoading.update { false }
        }
    }
}