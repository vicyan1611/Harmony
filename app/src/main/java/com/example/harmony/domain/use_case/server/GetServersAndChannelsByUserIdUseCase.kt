package com.example.harmony.domain.use_case.server

import android.util.Log
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.ServerWithChannels
import com.example.harmony.domain.repository.ChannelRepository
import com.example.harmony.domain.repository.ServerRepository
import com.example.harmony.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class GetServersAndChannelsByUserIdUseCase @Inject constructor(
    private val getServerAndChannelsByServerIdUseCase: GetServerAndChannelsByServerIdUseCase, // Use case for single server details
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<ServerWithChannels>>> = channelFlow {
        send(Resource.Loading()) // Emit loading state

        try {
            // Step 1: Get the list of joined server IDs
            // Corrected function name: getJoinedServerIds
            when (val serverIdsResult = userRepository.getListJoinedServerIdByUser(userId).last()) {
                is Resource.Error -> {
                    send(Resource.Error(serverIdsResult.message ?: "Failed to get joined server IDs."))
                    return@channelFlow
                }
                is Resource.Success -> {
                    val serverIds = serverIdsResult.data ?: emptyList()

                    if (serverIds.isEmpty()) {
                        send(Resource.Success(emptyList()))
                        return@channelFlow
                    }

                    // Step 2: Fetch details concurrently
                    val deferredResults = coroutineScope { // coroutineScope is correct
                        serverIds.map { serverId ->
                            async { // async returns Deferred<Resource<ServerWithChannels>>
                                // Call the use case AND collect its result within async
                                getServerAndChannelsByServerIdUseCase(serverId)
                                    .first { it !is Resource.Loading } // Collect first non-loading result
                            }
                        } // .map returns List<Deferred<Resource<ServerWithChannels>>>
                    }

                    // awaitAll waits for all Deferred objects to complete
                    val results: List<Resource<ServerWithChannels>> = deferredResults.awaitAll()

                    // Step 3: Process the results - Filter successes and extract data
                    val successfulServersWithChannels = results.mapNotNull { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                Log.d("GetServersAndChannels", "Success: ${resource.data}")
                                resource.data // Extract data from success
                            }
                            is Resource.Error -> {
                                // Log or handle error for individual server fetch if needed
                                println("Error fetching details for a server: ${resource.message}")
                                null // Map error/loading to null
                            }
                            is Resource.Loading -> null // Should not happen after .first{}, map to null
                        }
                    } // .mapNotNull filters out nulls and returns List<ServerWithChannels>

                    // Step 4: Emit the final list
                    send(Resource.Success(successfulServersWithChannels))
                }
                is Resource.Loading -> {
                    send(Resource.Loading()) // Should not happen
                }
            }
        } catch (e: Exception) {
            send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}