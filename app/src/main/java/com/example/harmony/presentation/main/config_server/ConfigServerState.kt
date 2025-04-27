package com.example.harmony.presentation.main.config_server

import android.net.Uri
import com.example.harmony.domain.model.Server

data class ConfigServerState(
    val serverId: String = "",
    val serverDetails: Server? = null,
    val currentName: String = "", // Name currently displayed/being edited
    val currentProfileUrl: String? = null, // Current profile URL
    val selectedImageUri: Uri? = null, // New image selected by user
    val isLoadingDetails: Boolean = false,
    val isUpdating: Boolean = false, // Combined loading state for rename/pic change
    val isDeleting: Boolean = false,
    val error: String? = null,
    val deleteError: String? = null, // Specific error for delete operation
    val updateError: String? = null, // Specific error for update operation
    val navigateBack: Boolean = false, // Flag to trigger navigation back
    val showDeleteConfirmation: Boolean = false,
    val canUserConfigure: Boolean = false // Determines if controls are enabled
)