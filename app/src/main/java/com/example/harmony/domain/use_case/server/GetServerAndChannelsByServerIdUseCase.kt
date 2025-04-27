package com.example.harmony.domain.use_case.server

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Channel
import com.example.harmony.domain.model.Server
import com.example.harmony.domain.model.ServerWithChannels
import com.example.harmony.domain.repository.ChannelRepository
import com.example.harmony.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetServerAndChannelsByServerIdUseCase @Inject constructor(
    private val serverRepository: ServerRepository,
    private val channelRepository: ChannelRepository
) {
    operator fun invoke(serverId: String): Flow<Resource<ServerWithChannels>> {
        // Fetch server and channels flows
        val serverFlow: Flow<Resource<Server>> = serverRepository.getServerById(serverId)
        val channelsFlow: Flow<Resource<List<Channel>>> = channelRepository.getListOfChannelsByServerId(serverId)

        // Combine the latest emissions from both flows
        return combine(serverFlow, channelsFlow) { serverResult, channelsResult ->
            // Determine the combined resource state
            when {
                // If both are successful, combine data
                serverResult is Resource.Success && channelsResult is Resource.Success -> {
                    if (serverResult.data != null) {
                        Resource.Success(
                            ServerWithChannels(
                                server = serverResult.data,
                                channels = channelsResult.data ?: emptyList()
                            )
                        )
                    } else {
                        Resource.Error("Server data missing") // Should ideally not happen if Success
                    }
                }
                // If either resulted in an error, return error
                serverResult is Resource.Error -> Resource.Error(serverResult.message ?: "Failed to load server")
                channelsResult is Resource.Error -> Resource.Error(channelsResult.message ?: "Failed to load channels")
                // Otherwise, it's loading
                else -> Resource.Loading()
            }
        }
    }
}