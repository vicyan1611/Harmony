package com.example.harmony.domain.repository

import com.example.harmony.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface PresenceRepository {
    // Sets user online in a specific voice channel
    fun setUserOnline(channelId: String, userId: String, agoraUid: Int): Flow<Resource<Unit>>

    // Sets user offline
    fun setUserOffline(channelId: String, userId: String, agoraUid: Int): Flow<Resource<Unit>>

    // Gets list of online user IDs for a channel
    fun getOnlineUsers(channelId: String): Flow<Resource<List<String>>> // Returns Firebase User IDs

    // Stores or updates the mapping between Firebase UID and Agora UID
    fun storeUserAgoraUidMapping(userId: String, agoraUid: Int): Flow<Resource<Unit>>

    // Gets Firebase UID from Agora UID (can be inefficient, use cautiously)
    fun getUserIdFromAgoraUid(agoraUid: Int): Flow<Resource<String?>>
}