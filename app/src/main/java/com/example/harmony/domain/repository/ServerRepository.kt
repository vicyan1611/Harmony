package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Channel
import com.example.harmony.domain.model.Server
import kotlinx.coroutines.flow.Flow

interface ServerRepository {

    fun createServer(name: String, hostUserId: String, profilePicture: String): Flow<Resource<Server>>

    fun countServersHostedByUser(userId: String): Flow<Resource<Long>>

    fun getServerById(serverId: String): Flow<Resource<Server>>

//    fun getServerListByUser(): Flow<Resource<List<Server>>>

    fun deleteServer(serverId: String): Flow<Resource<Unit>>

    fun updateServer(serverId: String, name: String?, profileUrl: String?): Flow<Resource<Unit>>

    fun addMemberToServer(serverId: String, memberId: String): Flow<Resource<Unit>>
}