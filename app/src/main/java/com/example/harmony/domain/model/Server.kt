package com.example.harmony.domain.model

import android.net.Uri

data class Server (
    val name: String = "",
    val id: String = "",
    val ownerId: String = "",
    val profileUrl : String = "",
    val memberIds: List<String> = emptyList()
)