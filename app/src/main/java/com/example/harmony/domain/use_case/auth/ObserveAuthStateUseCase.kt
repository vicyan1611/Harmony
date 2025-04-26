package com.example.harmony.domain.use_case.auth

import com.example.harmony.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//class ObserveAuthStateUseCase @Inject constructor(
//    private val authRepository: AuthRepository
//) {
//    operator fun invoke(): Flow<Boolean> =
//        authRepository.getCurrentUser().map { user -> user != null }
//}