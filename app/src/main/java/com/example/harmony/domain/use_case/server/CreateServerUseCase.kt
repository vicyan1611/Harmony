package com.example.harmony.domain.use_case.server

import android.net.Uri
import androidx.core.app.PendingIntentCompat.send
import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Server
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ChannelRepository
import com.example.harmony.domain.repository.ServerRepository
import com.example.harmony.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class CreateServerUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val serverRepository: ServerRepository,
    private val channelRepository: ChannelRepository
) {
    // @returns new server id
    operator fun invoke(name: String, imageUri: Uri?): Flow<Resource<String>> = channelFlow {
        send(Resource.Loading())
        val ownerId = authRepository.getCurrentUser()?.id
        if (ownerId == null) {
            send(Resource.Error("User not logged in."))
            return@channelFlow
        }
        var profileUrl = ""
//        if (imageUri != null) {
//            // Collect upload result (simplified assuming single emission or taking first)
//            val uploadResult = storageRepository.uploadServerImage(imageUri).firstOrNull { it !is Resource.Loading }
//
//            when (uploadResult) {
//                is Resource.Success -> profileUrl = uploadResult.data ?: ""
//                is Resource.Error -> {
//                    send(Resource.Error("Image upload failed: ${uploadResult.message}"))
//                    return@channelFlow // Stop if upload fails
//                }
//                else -> { // Handle null or unexpected loading state
//                    send(Resource.Error("Image upload failed."))
//                    return@channelFlow
//                }
//            }
//        }
        serverRepository.createServer(name, ownerId, profileUrl).collect { serverResult ->
            when (serverResult) {
                is Resource.Success -> {
                    val server = serverResult.data
                    if (server != null && server.id.isNotBlank()) {
                        val inviteLink = "${Constants.INVITE_BASE_URL}/${server.id}"
                        userRepository.appendListJoinedServerIds(ownerId, server.id).collect {
                            send(Resource.Success(server.id))
                        }
                        channelRepository.createChannel("general", "", server.id).collect { data ->
                            send(Resource.Success(server.id))
                        }
                        send(Resource.Success(inviteLink)) // Emit the constructed link
                    } else {
                        send(Resource.Error("Failed to get valid server details."))
                    }

                }
                is Resource.Error -> {
                    // Forward the error
                    send(Resource.Error(serverResult.message ?: "Failed to create server."))
                }
                is Resource.Loading -> {
                    // Forward loading state
                    send(Resource.Loading())
                }
            }
        }
    }
}