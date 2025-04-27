package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.DirectMessageConversation
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.model.ParticipantInfo
import kotlinx.coroutines.flow.Flow

interface DirectMessageRepository {

    fun getDirectMessageConversations(userId: String): Flow<Resource<List<DirectMessageConversation>>>

    // Gets the details of a specific DM conversation
    fun getDirectMessageConversation(conversationId: String): Flow<Resource<DirectMessageConversation>>

    // Gets messages within a specific DM conversation
    fun getDirectMessages(conversationId: String): Flow<Resource<List<Message>>>

    fun sendDirectMessage(
        conversationId: String,
        message: Message,
        currentUserInfo: ParticipantInfo, // Pass current user info
        otherUserInfo: ParticipantInfo    // Pass other user info
    ): Flow<Resource<Unit>>

    fun getOrCreateDirectMessageConversation(
        userId1: String,
        userId2: String,
        userInfo1: ParticipantInfo,
        userInfo2: ParticipantInfo
    ): Flow<Resource<String>>
}