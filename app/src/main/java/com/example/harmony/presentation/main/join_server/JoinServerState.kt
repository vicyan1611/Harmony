package com.example.harmony.presentation.main.join_server

data class JoinServerState(
    val inviteLink: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinSuccess: Boolean = false
)