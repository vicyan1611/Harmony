package com.example.harmony.domain.repository

import android.net.Uri
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
//    A Flow emitting Resource<Unit> indicating success or failure.
    fun sendMessage(serverId: String, channelId: String, message: Message, imageUri: Uri? = null): Flow<Resource<Unit>>

//    A Flow emitting Resource<List<Message>> containing the list of messages or an error.
    fun getMessages(serverId: String, channelId: String): Flow<Resource<List<Message>>>

    fun updateMessageReaction(
        serverId: String,
        channelId: String,
        messageId: String,
        emojiIndex: Int? // Null to remove reaction
    ): Flow<Resource<Unit>>
}