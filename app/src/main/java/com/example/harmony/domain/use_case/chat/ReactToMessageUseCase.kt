package com.example.harmony.domain.use_case.chat


import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.DirectMessageRepository
import com.example.harmony.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReactToMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val dmRepository: DirectMessageRepository
) {
    /**
     * Adds, updates, or removes a reaction for a message in either a server channel or a DM conversation.
     *
     * @param serverId The ID of the server (if reacting in a server channel). Null for DMs.
     * @param channelId The ID of the channel (if reacting in a server channel). Null for DMs.
     * @param conversationId The ID of the DM conversation (if reacting in a DM). Null for server channels.
     * @param messageId The ID of the message to react to.
     * @param emojiIndex The index (0-5) of the emoji reaction. Null to remove the current user's reaction.
     * @return A Flow emitting Resource<Unit> indicating success or failure.
     */
    operator fun invoke(
        serverId: String? = null,
        channelId: String? = null,
        conversationId: String? = null,
        messageId: String,
        emojiIndex: Int? // Null means remove reaction
    ): Flow<Resource<Unit>> {
        // Input validation
        if (messageId.isBlank()) {
            return flow { emit(Resource.Error("Message ID cannot be blank.")) }
        }
        if ((serverId == null || channelId == null) && conversationId == null) {
            return flow { emit(Resource.Error("Must provide either server/channel IDs or conversation ID.")) }
        }
        if (serverId != null && channelId != null && conversationId != null) {
            return flow { emit(Resource.Error("Cannot provide both server/channel and conversation IDs.")) }
        }
        if (emojiIndex != null && (emojiIndex < 0 || emojiIndex > 5)) { // Assuming 6 emojis (0-5)
            return flow { emit(Resource.Error("Invalid emoji index. Must be between 0 and 5.")) }
        }


        return when {
            // Reacting in a Server Channel
            serverId != null && channelId != null -> {
                messageRepository.updateMessageReaction(
                    serverId = serverId,
                    channelId = channelId,
                    messageId = messageId,
                    emojiIndex = emojiIndex
                )
            }
            // Should not happen due to earlier validation, but added for completeness
            else -> {
                flow { emit(Resource.Error("Invalid parameters for reaction.")) }
            }
        }
    }
}