package com.example.harmony.domain.repository

import android.net.Uri
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getListJoinedServerIdByUser(userId: String): Flow<Resource<List<String>>>

    fun appendListJoinedServerIds(userId: String, serverId: String): Flow<Resource<Unit>>

    // Function declaration to get user information, returning Flow of Resource<User>
    fun getUser(): Flow<Resource<User>>
    fun updateProfile(displayName: String?, avatarUri: Uri?): Flow<Resource<User>>
    fun updateUserSettings(userId: String, newSettings: UserSettings): Flow<Resource<UserSettings>>
    fun getCollectionUser(userId: String): Flow<Resource<User>>

    fun searchUsers(query: String): Flow<Resource<List<User>>>

    fun removeServerFromAllUsers(serverId: String): Flow<Resource<Unit>>
}