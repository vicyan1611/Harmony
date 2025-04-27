package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getListJoinedServerIdByUser(userId: String): Flow<Resource<List<String>>>

    fun appendListJoinedServerIds(userId: String, serverId: String): Flow<Resource<Unit>>
}