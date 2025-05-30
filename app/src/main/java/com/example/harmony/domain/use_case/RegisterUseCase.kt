package com.example.harmony.domain.use_case

import android.util.Log
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(displayName: String, email: String, password: String): Flow<Resource<User>> {
        Log.d("Register test", "Use case invoke")
        return authRepository.register(displayName, email, password)
    }
}