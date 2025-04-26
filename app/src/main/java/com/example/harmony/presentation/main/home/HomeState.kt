package com.example.harmony.presentation.main.home

import com.example.harmony.domain.model.User

data class HomeState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)