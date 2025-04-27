package com.example.harmony.presentation.main.search


import com.example.harmony.domain.model.ParticipantInfo
import com.example.harmony.domain.model.User

data class UserSearchState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val currentUserInfo: ParticipantInfo? = null // For creating conversation
)