package com.example.harmony.domain.use_case

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        return authRepository.login(email, password)
    }
}