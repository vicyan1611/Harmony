package com.example.harmony.presentation.auth.login

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class LoginEvent {
    data class OnEmailChange(val email: String) : LoginEvent()
    data class OnPasswordChange(val password: String) : LoginEvent()
    object OnLoginClick : LoginEvent()
    object OnRegisterClick : LoginEvent()
    object OnForgotPasswordClick : LoginEvent()
    object OnGoogleSignInClick : LoginEvent()
}