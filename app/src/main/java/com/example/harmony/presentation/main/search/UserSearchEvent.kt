package com.example.harmony.presentation.main.search

import com.example.harmony.domain.model.User

sealed class UserSearchEvent {
    data class OnQueryChanged(val query: String) : UserSearchEvent()
    data class OnUserSelected(val selectedUser: User) : UserSearchEvent()
    object ClearSearch : UserSearchEvent()
}