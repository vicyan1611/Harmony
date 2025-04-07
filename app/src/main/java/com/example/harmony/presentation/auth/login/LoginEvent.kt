package com.example.harmony.presentation.auth.login

sealed class LoginEvent {
    data class OnEmailChange(val email: String) : LoginEvent()
    data class OnPasswordChange(val password: String) : LoginEvent()
    object OnLoginClick : LoginEvent()
    object OnRegisterClick : LoginEvent()
    object OnForgotPasswordClick : LoginEvent()
}