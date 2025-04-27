package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Server
import kotlinx.coroutines.flow.Flow

interface ServerRepository {

    fun createServer(name: String, hostUserId: String, profilePicture: String): Flow<Resource<Server>>

    fun countServersHostedByUser(userId: String): Flow<Resource<Long>>

    fun getServer(serverId: String): Flow<Resource<Server>>

    fun getServerListByUser(): Flow<Resource<List<Server>>>
}