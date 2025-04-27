package com.example.harmony.presentation.auth.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harmony.core.common.Constants.ERROR_FIELDS_EMPTY
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.use_case.LoginUseCase
import com.example.harmony.domain.use_case.auth.GoogleSignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import kotlin.jvm.java
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.NoCredentialException

import com.example.harmony.R



@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginUseCase: LoginUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val credentialManager = CredentialManager.create(context)

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
            is LoginEvent.OnGoogleSignInClick -> {
                initiateGoogleSignIn()
            }
        }
    }

    private fun initiateGoogleSignIn() {
        _state.update { it.copy(isGoogleLoading = true, error = null) } // Set loading state

        // Get Web Client ID from strings.xml
        val serverClientId = context.getString(R.string.default_web_client_id)

        // Configure Google Sign-In options for Credential Manager
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        // Build the credential request
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Launch coroutine to call Credential Manager
        viewModelScope.launch {
            try {
                // Call Credential Manager suspend function
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = context, // Pass Activity context (or App context if suitable)
                )
                // Handle the successful result
                handleGoogleCredentialResult(result)
            } catch (e: GetCredentialException) {
                // Handle failures
                handleGoogleCredentialError(e)
            }
        }
    }

    private fun handleGoogleCredentialResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            // Handle Google ID token credential
            is GoogleIdTokenCredential -> {
                val googleIdToken = credential.idToken
                Log.d("LoginViewModel", "Got Google ID Token: $googleIdToken")
                firebaseSignInWithGoogleToken(googleIdToken)
            }
            // Handle Password credential if you also support password saving/retrieval
            // is PasswordCredential -> { ... }
            else -> {
                // Handle other credential types or unexpected cases
                Log.e("LoginViewModel", "Unexpected credential type: ${credential::class.java.name}")
                _state.update { it.copy(isGoogleLoading = false, error = "Unsupported credential type") }
            }
        }
    }

    private fun handleGoogleCredentialError(e: GetCredentialException) {
        // Log the specific error
        Log.e("LoginViewModel", "Google Sign-In Credential Error", e)

        val errorMessage = when (e) {
            is GetCredentialCancellationException -> "Sign-in cancelled by user."
            is NoCredentialException -> "No Google account found on device."
            is GetCredentialInterruptedException -> "Sign-in interrupted. Please try again." // E.g., incoming call
            is GetCredentialProviderConfigurationException -> "Sign-in provider configuration error." // Check Firebase/Google Cloud setup
            is GetCredentialUnknownException -> "An unknown sign-in error occurred."
            else -> "Google Sign-In failed: ${e.localizedMessage}"
        }
        _state.update { it.copy(isGoogleLoading = false, error = errorMessage) }
    }


    private fun firebaseSignInWithGoogleToken(idToken: String) {
        // Pass the obtained ID Token to the Use Case
        googleSignInUseCase(idToken).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    // isGoogleLoading should already be true
                    _state.update { it.copy(isGoogleLoading = true, error = null) }
                }
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isGoogleLoading = false,
                            isSuccess = true, // Trigger navigation
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isGoogleLoading = false,
                            error = result.message // Show error from Firebase Auth/Firestore
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun login() {
        _state.update { it.copy(isGoogleLoading = false) }
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

