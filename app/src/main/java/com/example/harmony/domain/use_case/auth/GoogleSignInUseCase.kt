package com.example.harmony.domain.use_case.auth

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<Resource<User>> {
        return repository.signInWithGoogle(idToken)
    }
}