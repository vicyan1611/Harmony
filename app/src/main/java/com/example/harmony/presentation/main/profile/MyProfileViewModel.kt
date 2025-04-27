// presentation/main/profile/MyProfileViewModel.kt
package com.example.harmony.presentation.main.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.use_case.LogoutUseCase // Use existing LogoutUseCase
import com.example.harmony.domain.use_case.user.GetUserUseCase // Use existing GetUserUseCase
import com.example.harmony.presentation.main.MainViewModel
import com.example.harmony.presentation.navigation.NavRoutes // Import NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {


    private val _state = MutableStateFlow(MyProfileState())
    val state: StateFlow<MyProfileState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {

        getUserUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true, error = null) }
                }
                is Resource.Success -> {
                    _state.update {
                        it.copy(isLoading = false, user = result.data, error = null)
                    }
                    Log.d("MyProfileViewModel", "User: ${result.data}")

                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.message ?: "Failed to load profile")
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun logout() {
        logoutUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoggingOut = true, error = null) }
                }
                is Resource.Success -> {
                    _state.update { it.copy(isLoggingOut = false) }
                    _navigationEvent.emit(NavRoutes.LOGIN) // Navigate to login after logout
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoggingOut = false, error = result.message ?: "Logout failed") }
                }
            }
        }.launchIn(viewModelScope)
    }

    // Optional: Add a refresh function if needed
    fun refreshProfile() {
        loadUserProfile()
    }
}