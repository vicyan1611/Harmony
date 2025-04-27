// harmony/presentation/MainViewModel.kt
package com.example.harmony.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.domain.model.AppTheme // Import AppTheme
import com.example.harmony.domain.model.User // Import User
import com.example.harmony.domain.model.UserSettings
import com.example.harmony.domain.use_case.user.GetUserUseCase // Import GetUserUseCase
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi // Import needed for flatMapLatest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class) // Needed for flatMapLatest
@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase, // Inject GetUserUseCase
    private val firebaseAuth: FirebaseAuth // Inject FirebaseAuth to observe auth state
) : ViewModel() {

    // 1. Create a flow that emits the current authentication state
    private val authStateFlow: StateFlow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val isLoggedIn = auth.currentUser != null
            Log.d("MainViewModel", "AuthStateListener triggered. User logged in: $isLoggedIn")
            trySend(isLoggedIn) // Send the current state
        }
        firebaseAuth.addAuthStateListener(listener)
        Log.d("MainViewModel", "Initial auth check. User logged in: ${firebaseAuth.currentUser != null}")
        trySend(firebaseAuth.currentUser != null) // Emit initial state immediately

        awaitClose { // Remove listener when the flow is cancelled
            Log.d("MainViewModel", "Removing AuthStateListener.")
            firebaseAuth.removeAuthStateListener(listener)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), firebaseAuth.currentUser != null) // Make it a hot StateFlow


    // 2. Create the main user resource flow based on auth state

    val userResource: StateFlow<Resource<User>> = authStateFlow
        .flatMapLatest { isLoggedIn ->
            if (isLoggedIn) {
                Log.d("MainViewModel", "User is logged in, fetching user data...")
                getUserUseCase() // Fetch user data because user is logged in
            } else {
                Log.d("MainViewModel", "User is logged out, emitting error state.")
                // User is logged out, emit the error state immediately
                flowOf(Resource.Error<User>("User not logged in"))
            }
        }
        .stateIn( // Convert the resulting flow into a hot StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Keep active 5s after last collector stops
            initialValue = Resource.Loading() // Start with loading state
        )

    private val _userSettings = MutableStateFlow<UserSettings?>(null) // Cache for comparison

    // Default language
    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Default theme
    private val _appTheme = MutableStateFlow(AppTheme.LIGHT)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    // Loading state derived from userResource
    val isLoading: StateFlow<Boolean> = userResource.map { it is Resource.Loading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true) // Initial value true

    // Error state derived from userResource
    val error: StateFlow<String?> = userResource.map { if (it is Resource.Error) it.message else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --------------------------------------

    init {
        Log.d("MainViewModel", "Initializing and starting user resource collection...")
        observeUserResource() // Start observing the reactive userResource flow
    }

    private fun observeUserResource() {
        viewModelScope.launch {
            userResource.collect { result ->
                Log.d("MainViewModel", "Collected user resource: ${result::class.simpleName}")
                // Update language and theme based on successful data fetch
                if (result is Resource.Success) {
                    val user = result.data
                    val settings = user?.settings ?: UserSettings() // Use default if null
                    val previousSettings = _userSettings.value // Get cached settings

                    _userSettings.value = settings // Update cache

                    // Update language state only if it changed or was null
                    if (previousSettings?.language != settings.language) {
                        _appLanguage.value = settings.language
                        Log.d("MainViewModel", "AppLanguage updated to: ${settings.language}")
                    } else {
                        Log.d("MainViewModel", "AppLanguage unchanged: ${settings.language}")
                    }

                    // Update theme state only if it changed or was null
                    if (previousSettings?.theme != settings.theme) {
                        _appTheme.value = settings.theme
                        Log.d("MainViewModel", "AppTheme updated to: ${settings.theme}")
                    } else {
                        Log.d("MainViewModel", "AppTheme unchanged: ${settings.theme}")
                    }

                    Log.d("MainViewModel", "User settings applied successfully. User: ${user?.displayName}")

                } else if (result is Resource.Error && result.message == "User not logged in") {
                    // Explicitly reset settings to defaults when logged out
                    if (_userSettings.value != null) { // Only reset if they were previously loaded
                        Log.d("MainViewModel", "Resetting language and theme to defaults due to logout.")
                        _userSettings.value = null // Clear cache
                        _appLanguage.value = AppLanguage.ENGLISH // Reset to default
                        _appTheme.value = AppTheme.LIGHT // Reset to default
                    }
                } else if (result is Resource.Error) {
                    Log.e("MainViewModel", "Error collecting user resource: ${result.message}")
                }
            }
        }
    }

}