package com.example.harmony.presentation.main.create_server
import android.net.Uri
data class CreateServerUiState(
    val serverName: String = "",
    val selectedImageUri: Uri? = null, // Keep Uri for the UI
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdServerInviteLink: String? = null // To show on the invite screen
)