package com.example.harmony.domain.use_case.server

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateServerUseCase @Inject constructor(private val serverRepository: ServerRepository) {
    operator fun invoke(name: String, hostUserId: String, profilePicture: ByteArray?): Flow<Resource<String>> {
        return serverRepository.createServer(name, hostUserId, profilePicture)
    }
}