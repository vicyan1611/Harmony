package com.example.harmony.domain.model

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val settings: UserSettings? = UserSettings(),
    val bio: String = ""
)
{
    constructor() : this("", "", "", null, 0L, 0L, UserSettings())
}