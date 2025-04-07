package com.example.harmony.domain.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)