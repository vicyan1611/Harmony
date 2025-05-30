package com.example.harmony.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Message (
    val id: String = "",
    val channelId: String = "",
    val senderId: String = "",
    val senderDisplayName: String = "",
    val senderPhotoUrl: String? = null,
    val text: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val imageUrl: String? = null,

    val reactions: Map<String, Int> = emptyMap(),

    @get:Exclude @set:Exclude var currentUserReactionIndex: Int? = null
)