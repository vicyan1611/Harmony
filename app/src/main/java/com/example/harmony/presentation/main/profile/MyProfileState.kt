package com.example.harmony.presentation.main.profile

import com.example.harmony.domain.model.User

data class MyProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoggingOut: Boolean = false // Add logout state if needed
)