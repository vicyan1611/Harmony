package com.example.harmony.domain.repository

import com.example.harmony.domain.model.User
import com.example.harmony.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>
    fun register(username: String, email: String, password: String): Flow<Resource<User>>
    fun logout(): Flow<Resource<Unit>>
    fun getCurrentUser(): User?
    fun isUserAuthenticated(): Boolean
}