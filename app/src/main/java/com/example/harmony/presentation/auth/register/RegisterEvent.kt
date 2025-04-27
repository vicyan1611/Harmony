package com.example.harmony.presentation.auth.register

sealed class RegisterEvent {
    data class OnDisplayNameChange(val displayName: String) : RegisterEvent()
    data class OnEmailChange(val email: String) : RegisterEvent()
    data class OnPasswordChange(val password: String) : RegisterEvent()
    data class OnConfirmPasswordChange(val confirmPassword: String) : RegisterEvent()
    object OnRegisterClick : RegisterEvent()
    object OnLoginClick : RegisterEvent()
}