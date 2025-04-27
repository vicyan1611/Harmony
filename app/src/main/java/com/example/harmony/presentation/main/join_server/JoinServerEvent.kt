package com.example.harmony.presentation.main.join_server

sealed class JoinServerEvent {
    data class OnLinkChange(val link: String) : JoinServerEvent()
    object OnJoinClick : JoinServerEvent()
    object OnDismissError : JoinServerEvent()
}