// Create file: harmony/domain/use_case/server/DeleteServerUseCase.kt
package com.example.harmony.domain.use_case.server

import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ChannelRepository
import com.example.harmony.domain.repository.ServerRepository
import com.example.harmony.domain.repository.UserRepository
// Import other repositories needed for cleanup (Channels, Messages, Users, Storage)
// import com.example.harmony.domain.repository.ChannelRepository
// import com.example.harmony.domain.repository.MessageRepository
// import com.example.harmony.domain.repository.UserRepository
// import com.example.harmony.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class DeleteServerUseCase @Inject constructor(
    private val serverRepository: ServerRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    // private val storageRepository: StorageRepository
) {
    /**
     * Deletes a server and potentially associated data.
     * Requires the user invoking this to be the owner of the server.
     * Note: Full cleanup (channels, messages, user lists, storage) can be complex
     * and might be better handled by Cloud Functions for atomicity and reliability.
     * This implementation focuses on deleting the main server document.
     *
     * @param serverId The ID of the server to delete.
     * @return A Flow emitting Resource<Unit> indicating success or failure.
     */
    operator fun invoke(serverId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val currentUserId = authRepository.getCurrentUser()?.id
        if (currentUserId == null) {
            emit(Resource.Error("User not logged in."))
            return@flow
        }

        try {
            // Step 1: Verify Ownership (Crucial Security Check)
            val serverResource = serverRepository.getServerById(serverId).last()
            if (serverResource is Resource.Error) {
                emit(Resource.Error("Server not found: ${serverResource.message}"))
                return@flow
            }
            val server = serverResource.data
            if (server?.ownerId != currentUserId) {
                emit(Resource.Error("Permission denied."))
                return@flow
            }

            // --- Optional Cleanup Steps (Consider Cloud Functions for robustness) ---
            // 1. Delete Profile Picture from Storage
//             if (!server.profileUrl.isNullOrBlank()) {
//                 storageRepository.deleteServerImage(serverId, server.profileUrl) // Assuming method exists
//             }

            // 2. Delete Channels and their Messages (Complex - Requires iteration)
//             val channelsResult = channelRepository.getListOfChannelsByServerId(serverId).first()
//             if (channelsResult is Resource.Success) {
//                 channelsResult.data?.forEach { channel ->
//                     // Delete messages in the channel (Requires another repo method/loop)
//                     messageRepository.deleteAllMessagesInChannel(serverId, channel.id)
//                     // Delete channel document
//                     channelRepository.deleteChannel(serverId, channel.id)
//                 }
//             }

            // 3. Remove server reference from users' joined lists (Very complex - Needs iteration over all users or a different data model)
             userRepository.removeServerFromAllUsers(serverId) // Unlikely to be efficient client-side

            // --- End Optional Cleanup Steps ---


            // Step 2: Delete the Server Document
            // Assumes ServerRepository has a `deleteServer` method
            val deleteResult = serverRepository.deleteServer(serverId).last()
            when (deleteResult) {
                is Resource.Success -> emit(Resource.Success(Unit))
                is Resource.Error -> emit(Resource.Error(deleteResult.message ?: "Failed to delete server."))
                is Resource.Loading -> emit(Resource.Loading())
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }
}