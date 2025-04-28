// harmony/presentation/main/home/HomeState.kt
package com.example.harmony.presentation.main.home

import com.example.harmony.domain.model.ChannelType
import com.example.harmony.domain.model.ServerWithChannels
import com.example.harmony.domain.model.User

data class HomeState(
    // Existing fields
    val isUserLoading: Boolean = false, // Renamed for clarity
    val user: User? = null,
    val userLoadError: String? = null, // Renamed for clarity

    // New fields for servers and channels
    val isLoadingServers: Boolean = false,
    val serversLoadError: String? = null,
    val serversWithChannels: List<ServerWithChannels> = emptyList(),
    val selectedServer: ServerWithChannels? = null,
    val isRefreshing: Boolean = false,

    val isAddChannelSheetVisible: Boolean = false,
    val newChannelName: String = "",
    val newChannelDescription: String = "",
    val newChannelType: ChannelType = ChannelType.TEXT,
    val isCreatingChannel: Boolean = false,
    val createChannelError: String? = null,

    val isMyProfileSheetVisible: Boolean = false
)