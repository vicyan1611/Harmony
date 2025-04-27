package com.example.harmony.domain.use_case.server

import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ServerRepository
import com.example.harmony.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class JoinServerUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val serverRepository: ServerRepository
) {
    operator fun invoke(inviteLink: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            emit(Resource.Error("User not logged in."))
            return@flow
        }

        // --- 1. Extract Server ID from Link ---
        // Basic validation (you might want more robust parsing)
        val serverId = try {
            // Example: https://example.com/serverId123
            if (inviteLink.startsWith(Constants.INVITE_BASE_URL + "/")) {
                inviteLink.substringAfterLast("/")
            } else {
                // Handle custom scheme like harmony://join/serverId123 if needed
                throw IllegalArgumentException("Invalid invite link format.")
            }
        } catch (e: Exception) {
            emit(Resource.Error("Invalid invite link format."))
            return@flow
        }

        if (serverId.isBlank()) {
            emit(Resource.Error("Could not extract server ID from link."))
            return@flow
        }

        try {
            // --- 2. Validate Server Existence ---
            // Check if user is already a member (optional but good)
             val joinedServers = userRepository.getListJoinedServerIdByUser(currentUser.id).last() // Assuming synchronous or collecting first result
             if (joinedServers is Resource.Success && joinedServers.data?.contains(serverId) == true) {
                 emit(Resource.Error("You are already a member of this server."))
                 return@flow
             }

            // Check if server exists
            val serverResult = serverRepository.getServerById(serverId).last() // Get first non-loading state
            if (serverResult is Resource.Error || serverResult.data == null) {
                emit(Resource.Error("Server not found or invalid link."))
                return@flow
            }

            // Check if user is the owner
            if (serverResult.data.ownerId == currentUser.id) {
                emit(Resource.Error("You cannot join a server you own."))
                return@flow
            }

            // --- 3. Add User to Server ---
            // Add server ID to user's list
            val userUpdateResult = userRepository.appendListJoinedServerIds(currentUser.id, serverId).last()
            if (userUpdateResult is Resource.Error) {
                emit(Resource.Error(userUpdateResult.message ?: "Failed to update user profile."))
                return@flow
            }

             val serverUpdateResult = serverRepository.addMemberToServer(serverId, currentUser.id).last()
             if (serverUpdateResult is Resource.Error) {
                // Optional: Rollback user update if server update fails? Complex.
                emit(Resource.Error(serverUpdateResult.message ?: "Failed to update server members."))
                return@flow
             }

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }
}