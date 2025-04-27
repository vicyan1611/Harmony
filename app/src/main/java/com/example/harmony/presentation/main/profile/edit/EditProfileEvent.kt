// presentation/main/profile/edit/EditProfileEvent.kt
package com.example.harmony.presentation.main.profile.edit

import android.net.Uri
import com.example.harmony.domain.model.AppLanguage
import com.example.harmony.domain.model.AppTheme

sealed class EditProfileEvent {
    object LoadUserProfile : EditProfileEvent() // Load cả profile và settings ban đầu
    data class OnUsernameChange(val username: String) : EditProfileEvent()
    data class OnAvatarUriChange(val uri: Uri?) : EditProfileEvent()
    data class ChangeLanguage(val language: AppLanguage) : EditProfileEvent() // Event mới
    data class ChangeTheme(val theme: AppTheme) : EditProfileEvent() // Event mới
    object OnSaveChangesClick : EditProfileEvent()
}