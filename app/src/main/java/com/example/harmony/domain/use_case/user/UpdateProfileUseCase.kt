// domain/use_case/user/UpdateProfileUseCase.kt
package com.example.harmony.domain.use_case.user

import android.net.Uri
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
     operator fun invoke(username: String?, avatarUri: Uri?): Flow<Resource<User>> {
        // Basic validation could go here if needed
        return userRepository.updateProfile(username, avatarUri)
    }
}