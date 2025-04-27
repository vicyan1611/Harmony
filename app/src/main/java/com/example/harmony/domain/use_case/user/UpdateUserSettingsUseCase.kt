package com.example.harmony.domain.use_case.user

import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.UserSettings
import com.example.harmony.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateUserSettingsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth // Inject FirebaseAuth
) {
    operator fun invoke(userSettings: UserSettings): Flow<Resource<UserSettings>> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Trả về lỗi nếu user chưa đăng nhập
            return flow { emit(Resource.Error("User not authenticated")) }
        }
        // Gọi hàm repository với userId và settings mới
        return userRepository.updateUserSettings(userId, userSettings)
    }
}