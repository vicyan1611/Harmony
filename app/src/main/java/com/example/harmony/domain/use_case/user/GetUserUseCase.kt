// domain/use_case/user/GetUserUseCase.kt
package com.example.harmony.domain.use_case.user

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // Operator fun allows calling the Use Case like a function: getUserUseCase()
    operator fun invoke(): Flow<Resource<User>> {
        // Use Case simply calls the corresponding function from the Repository
        return userRepository.getUser()
    }
}