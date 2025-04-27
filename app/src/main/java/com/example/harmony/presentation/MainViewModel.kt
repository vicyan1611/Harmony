// In: harmony/presentation/MainViewModel.kt
package com.example.harmony.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.domain.model.AppTheme // Import AppTheme
import com.example.harmony.domain.model.UserSettings
import com.example.harmony.domain.use_case.user.GetUserUseCase // Import GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase // Inject GetUserUseCase
) : ViewModel() {

    private val _userSettings = MutableStateFlow<UserSettings?>(null)

    // --- State Flows for UI Observation ---
    // Expose language, defaulting to English (or system default)
    // Consider using AppLanguage.getDefault() for a better initial default
    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Expose theme, defaulting to Light
    private val _appTheme = MutableStateFlow(AppTheme.LIGHT) // Default theme
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    private val _isLoading = MutableStateFlow(true) // Start loading initially
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    // --------------------------------------

    init {
        Log.d("MainViewModel", "Initializing and loading user settings...")
        loadUserSettings()
    }

    private fun loadUserSettings() {
        getUserUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    if (_userSettings.value == null) { // Only show loading if not already loaded
                        _isLoading.value = true
                    }
                    _error.value = null
                    Log.d("MainViewModel", "Loading user settings...")
                }
                is Resource.Success -> {
                    _isLoading.value = false
                    val settings = result.data?.settings ?: UserSettings() // Use default if null
                    val previousSettings = _userSettings.value

                    _userSettings.value = settings // Store full settings

                    // Update language state only if it changed
                    if (previousSettings?.language != settings.language) {
                        _appLanguage.value = settings.language
                        Log.d("MainViewModel", "AppLanguage updated to: ${settings.language}")
                    } else {
                        Log.d("MainViewModel", "AppLanguage unchanged: ${settings.language}")
                    }

                    // Update theme state only if it changed
                    if (previousSettings?.theme != settings.theme) {
                        _appTheme.value = settings.theme
                        Log.d("MainViewModel", "AppTheme updated to: ${settings.theme}")
                    } else {
                        Log.d("MainViewModel", "AppTheme unchanged: ${settings.theme}")
                    }

                    Log.d("MainViewModel", "User settings loaded successfully. User: ${result.data?.displayName}")
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _error.value = result.message ?: "Failed to load user settings"
                    Log.e("MainViewModel", "Error loading settings: ${_error.value}")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun refreshSettings() {
        Log.d("MainViewModel", "Refreshing settings...")
        _isLoading.value = true // Indicate refresh attempt
        _userSettings.value = null // Clear previous value maybe? Depends on desired UX
        loadUserSettings()
    }
}