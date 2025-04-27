package com.example.harmony.domain.model

import android.net.Uri

data class Server (
    val name: String = "",
    val id: String = "",
    val hostUserId: String = "",
    val profilePicture: String = "",
    val members: Array<User> = arrayOf()
)