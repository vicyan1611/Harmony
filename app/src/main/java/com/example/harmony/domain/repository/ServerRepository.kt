package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface ServerRepository {
    fun createServer(name: String, hostUserId: String, profilePicture: ByteArray?): Flow<Resource<String>>
}