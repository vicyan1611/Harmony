// In: harmony/presentation/main/profile/edit/EditProfileViewModel.kt
package com.example.harmony.presentation.main.profile.edit

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.domain.model.AppTheme
import com.example.harmony.domain.model.User
import com.example.harmony.domain.model.UserSettings
import com.example.harmony.domain.use_case.user.GetUserUseCase
import com.example.harmony.domain.use_case.user.UpdateProfileUseCase
import com.example.harmony.domain.use_case.user.UpdateUserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase,
) : ViewModel() {

    companion object { private const val TAG = "EditProfileVM" }

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        // Call the function to load data when ViewModel is created
        loadUserProfileAndSettings()
    }

    fun onEvent(event: EditProfileEvent) {
        // Reset errors/success flags on new events if appropriate
        // _state.update { it.copy(error = null, updateSuccess = false) } // Consider if this is needed here or inside handlers
        when (event) {
            is EditProfileEvent.LoadUserProfile -> loadUserProfileAndSettings() // Handle explicit reload event
            is EditProfileEvent.OnUsernameChange -> {
                _state.update { it.copy(username = event.username, error = null) } // Clear error on change
            }
            is EditProfileEvent.OnAvatarUriChange -> {
                _state.update { it.copy(selectedAvatarUri = event.uri, error = null) } // Clear error on change
            }
            is EditProfileEvent.ChangeLanguage -> {
                _state.update { it.copy(selectedLanguage = event.language, error = null) } // Clear error on change
            }
            is EditProfileEvent.ChangeTheme -> {
                _state.update { it.copy(selectedTheme = event.theme, error = null) } // Clear error on change
            }
            is EditProfileEvent.OnSaveChangesClick -> saveChanges()
        }
    }

    private fun loadUserProfileAndSettings() {
        Log.d(TAG, "Loading user profile and settings...")
        // Reset state flags before loading
        getUserUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true, error = null, updateSuccess = false) }
                }
                is Resource.Success -> {
                    val user = result.data
                    val settings = user?.settings ?: UserSettings() // Use default settings if null
                    Log.d(TAG, "Loaded data: User=${user?.displayName}, Settings=$settings")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            initialUsername = user?.displayName ?: "",
                            username = user?.displayName ?: "", // Update username field as well
                            currentAvatarUrl = user?.photoUrl,
                            initialUserSettings = settings,
                            initialLanguage = settings.language,
                            selectedLanguage = settings.language, // Initialize selected with initial
                            initialTheme = settings.theme,
                            selectedTheme = settings.theme,       // Initialize selected with initial
                            selectedAvatarUri = null,           // Clear any previously selected URI
                            error = null                        // Clear previous errors on success
                        )
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Error loading data: ${result.message}")
                    _state.update {
                        it.copy(isLoading = false, error = result.message ?: "Failed to load data")
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun saveChanges() {
        val currentState = state.value
        val initialSettings = currentState.initialUserSettings ?: run {
            _state.update { it.copy(isLoading = false, error = "Cannot save settings: initial state missing") }
            return
        }

        val languageChanged = currentState.selectedLanguage != null && currentState.selectedLanguage != initialSettings.language
        val themeChanged = currentState.selectedTheme != null && currentState.selectedTheme != initialSettings.theme
        val usernameChanged = currentState.username.isNotBlank() && currentState.username != currentState.initialUsername
        val avatarChanged = currentState.selectedAvatarUri != null

        val hasSettingsChanges = languageChanged || themeChanged
        val hasProfileChanges = usernameChanged || avatarChanged


        if (!hasProfileChanges && !hasSettingsChanges) {
            _state.update { it.copy(error = "No changes to save") }
            return
        }
        if (currentState.username.isBlank()) {
            _state.update { it.copy(isLoading = false, error = "Username cannot be empty") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, updateSuccess = false) }
            var profileUpdateError: String? = null
            var updatedUser: User? = null
            var updatedSettingsResult: UserSettings? = null // To store result from Firestore if needed later=
            var settingsUpdateError: String? = null
            var settingsToSave = initialSettings // Use current or initial settings
            var savedSettings: UserSettings? = null // To store the final saved state

            // 1. Update Profile (if needed)
            if (hasProfileChanges) {
                Log.d(TAG, "Saving profile changes...")
                val newUsername = currentState.username.takeIf { usernameChanged }
                updateProfileUseCase(newUsername, currentState.selectedAvatarUri).collect { result ->
                    when (result) {
                        is Resource.Loading -> {}
                        is Resource.Success -> updatedUser = result.data
                        is Resource.Error -> profileUpdateError = result.message ?: "Failed to update profile"
                    }
                }
                if (profileUpdateError != null) {
                    Log.e(TAG, "Profile update failed: $profileUpdateError")
                    _state.update { it.copy(isLoading = false, error = profileUpdateError) }
                    return@launch
                }
                Log.d(TAG, "Profile changes saved successfully.")
            }

            // 2. Update Settings (if needed)
            if (hasSettingsChanges) {
                Log.d(TAG, "Saving settings changes...")
                settingsToSave = initialSettings.copy(
                    language = currentState.selectedLanguage ?: initialSettings.language,
                    theme = currentState.selectedTheme ?: initialSettings.theme
                )

                updateUserSettingsUseCase(settingsToSave).collect { result -> // Use settingsToSave here
                    when (result) {
                        is Resource.Loading -> {}
                        is Resource.Success -> updatedSettingsResult = result.data // Store successful result
                        is Resource.Error -> settingsUpdateError = result.message ?: "Failed to update settings"
                    }
                }
                if (settingsUpdateError != null) {
                    Log.e(TAG, "Settings update failed: $settingsUpdateError")
                    _state.update { it.copy(isLoading = false, error = settingsUpdateError) }
                    return@launch
                }
                Log.d(TAG, "Settings changes saved successfully to Firestore.")

            }


            // 3. Update final state
            val finalUserDisplayName = updatedUser?.displayName ?: currentState.username
            val finalUserAvatarUrl = updatedUser?.photoUrl ?: currentState.currentAvatarUrl
            val finalSettings = updatedSettingsResult ?: settingsToSave

            Log.d(TAG, "Updating final state. Success=true")
            _state.update {
                it.copy(
                    isLoading = false,
                    updateSuccess = true,
                    initialUsername = finalUserDisplayName,
                    username = finalUserDisplayName,
                    currentAvatarUrl = finalUserAvatarUrl,
                    selectedAvatarUri = null,
                    initialUserSettings = finalSettings,
                    initialLanguage = finalSettings.language,
                    selectedLanguage = finalSettings.language,
                    initialTheme = finalSettings.theme,
                    selectedTheme = finalSettings.theme,
                    error = null
                )
            }
        }
    }
}