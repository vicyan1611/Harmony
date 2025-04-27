package com.example.harmony.domain.model

import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

data class ParticipantInfo(
    val displayName: String = "",
    val photoUrl: String? = null
)

data class DirectMessageConversation(
    val id: String = "", // Conversation ID (e.g., "uid1_uid2", uid1 < uid2)
    val participants: List<String> = emptyList(), // List of User IDs
    // Stores basic info needed for list display, avoiding extra user lookups
    val participantDetails: Map<String, ParticipantInfo> = emptyMap(),
    val lastMessageText: String? = null,
    val lastMessageSenderId: String? = null,
    @ServerTimestamp
    val lastActivity: Timestamp? = null,
    val createdAt: Timestamp? = null
) {
    // Helper function to get the other participant's details
    fun getOtherParticipant(currentUserId: String): ParticipantInfo? {
        val otherUserId = participants.firstOrNull { it != currentUserId }
        return participantDetails[otherUserId]
    }
    // Helper function to get the other participant's ID
    fun getOtherParticipantId(currentUserId: String): String? {
        return participants.firstOrNull { it != currentUserId }
    }
}