package com.example.harmony.data.repository

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ServerRepositoryImpl @Inject constructor (

) : ServerRepository {
    override fun createServer(
        name: String,
        hostUserId: String,
        profilePicture: ByteArray?
    ): Flow<Resource<String>> {
        TODO("Not yet implemented")
    }
}