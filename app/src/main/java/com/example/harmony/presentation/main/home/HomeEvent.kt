package com.example.harmony.presentation.main.home

import com.example.harmony.domain.model.ServerWithChannels

sealed class HomeEvent {
    data class OnServerSelected(val server: ServerWithChannels) : HomeEvent()
    object OnAddServerClicked : HomeEvent()
    object OnLogoutClicked : HomeEvent()
    object OnRefresh : HomeEvent()

    object OnShowAddChannelSheet : HomeEvent() // Event to show the sheet
    object OnDismissAddChannelSheet : HomeEvent() // Event to hide the sheet
    data class OnNewChannelNameChange(val name: String) : HomeEvent()
    data class OnNewChannelDescriptionChange(val description: String) : HomeEvent()
    object OnCreateChannelClicked : HomeEvent()

    object OnShowMyProfileSheet : HomeEvent()
    object OnDismissMyProfileSheet : HomeEvent()
    object OnNavigateToSettings : HomeEvent()
}