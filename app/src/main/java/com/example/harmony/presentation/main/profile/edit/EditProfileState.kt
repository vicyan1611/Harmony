// presentation/main/profile/edit/EditProfileState.kt
package com.example.harmony.presentation.main.profile.edit

import android.net.Uri
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.domain.model.AppTheme
import com.example.harmony.domain.model.UserSettings // Import UserSettings

data class EditProfileState(
    val isLoading: Boolean = false,

    // Profile fields
    val initialUsername: String = "",
    val username: String = "",
    val currentAvatarUrl: String? = null,
    val selectedAvatarUri: Uri? = null,

    // Settings fields
    val initialUserSettings: UserSettings? = null, // <-- Thêm trường này
    val initialLanguage: AppLanguage? = null, // Giữ lại để so sánh dễ dàng nếu muốn
    val selectedLanguage: AppLanguage? = null,
    val initialTheme: AppTheme? = null, // Giữ lại để so sánh dễ dàng nếu muốn
    val selectedTheme: AppTheme? = null,

    val error: String? = null,
    val updateSuccess: Boolean = false
)