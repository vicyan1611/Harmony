package com.example.harmony.domain.use_case

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> {
        return authRepository.logout()
    }
}