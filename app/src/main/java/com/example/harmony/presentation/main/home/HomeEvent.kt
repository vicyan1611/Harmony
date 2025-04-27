package com.example.harmony.presentation.main.home

import com.example.harmony.domain.model.ServerWithChannels

sealed class HomeEvent {
    data class OnServerSelected(val server: ServerWithChannels) : HomeEvent()
    object OnAddServerClicked : HomeEvent()
    object OnLogoutClicked : HomeEvent()
    object OnRefresh : HomeEvent()
}