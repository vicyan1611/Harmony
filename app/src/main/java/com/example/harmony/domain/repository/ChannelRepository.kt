package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Channel
import com.example.harmony.domain.model.ChannelType
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun createChannel(name: String, description: String, serverId: String, type: ChannelType): Flow<Resource<Channel>>

//    fun getAChannelByServerIdAndChannelId(serverId: String, channelId: String): Flow<Resource<Channel>>

    fun getListOfChannelsByServerId(serverId: String): Flow<Resource<List<Channel>>>
}