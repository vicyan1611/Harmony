package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getListJoinedServerIdByUser(userId: String): Flow<Resource<List<String>>>

    fun appendListJoinedServerIds(userId: String, serverId: String): Flow<Resource<Unit>>

    fun getCollectionUser(userId: String): Flow<Resource<User>>
}