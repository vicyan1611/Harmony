package com.example.harmony.presentation.main.config_server

import android.net.Uri

sealed class ConfigServerEvent {
    data class LoadServerDetails(val serverId: String) : ConfigServerEvent()
    data class OnNameChange(val newName: String) : ConfigServerEvent()
    data class OnProfilePictureSelected(val uri: Uri?) : ConfigServerEvent() // Placeholder event
    object OnSaveChangesClick : ConfigServerEvent()
    object OnDeleteServerClick : ConfigServerEvent()
    object OnConfirmDeleteServer : ConfigServerEvent()
    object OnCancelDeleteServer : ConfigServerEvent()
    object OnDismissError : ConfigServerEvent()
    object OnNavigationHandled : ConfigServerEvent()
}