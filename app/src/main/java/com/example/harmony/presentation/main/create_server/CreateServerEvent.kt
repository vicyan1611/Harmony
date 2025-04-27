package com.example.harmony.presentation.main.create_server

import android.net.Uri

sealed class CreateServerEvent {
    // Event when the server name input changes
    data class NameChanged(val name: String) : CreateServerEvent()

    // Event when an image is selected or cleared
    data class ImageSelected(val uri: Uri?) : CreateServerEvent()

    // Event when the user clicks the final "Create" button
    object CreateServerClicked : CreateServerEvent()

    // Event when the user clicks "Done" action on the invite screen
    object InviteDismissed : CreateServerEvent()
}