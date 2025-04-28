package com.example.harmony.data.repository

import android.util.Log
import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.repository.PresenceRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PresenceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PresenceRepository {

    companion object {
        const val VOICE_PRESENCE_COLLECTION = "voice_presence" // Top-level collection
        const val PARTICIPANTS_FIELD = "participants" // Map field { userId: agoraUid }
        const val USER_AGORA_MAPPING_COLLECTION = "user_agora_mappings" // For UID mapping
    }

    override fun setUserOnline(channelId: String, userId: String, agoraUid: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val presenceDocRef = firestore.collection(VOICE_PRESENCE_COLLECTION).document(channelId)
            // Use dot notation to update a specific field within the map
            presenceDocRef.update("${PARTICIPANTS_FIELD}.$userId", agoraUid).await()
            // Also store the reverse mapping for easier lookup later (optional but helpful)
            // storeUserAgoraUidMapping(userId, agoraUid).collect() // Wait for mapping storage
            Log.d("PresenceRepository", "User $userId set online in $channelId (Agora UID: $agoraUid)")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            // If document doesn't exist or field doesn't exist, create it
            if (e.message?.contains("No document to update") == true || e.message?.contains("NOT_FOUND") == true) {
                try {
                    val presenceDocRef = firestore.collection(VOICE_PRESENCE_COLLECTION).document(channelId)
                    presenceDocRef.set(mapOf(PARTICIPANTS_FIELD to mapOf(userId to agoraUid))).await()
                    // storeUserAgoraUidMapping(userId, agoraUid).collect()
                    Log.d("PresenceRepository", "Created presence doc for $channelId, user $userId online (Agora UID: $agoraUid)")
                    emit(Resource.Success(Unit))
                } catch (e2: Exception) {
                    Log.e("PresenceRepository", "Error creating presence doc: ${e2.localizedMessage}")
                    emit(Resource.Error(e2.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
                }
            } else {
                Log.e("PresenceRepository", "Error setting user online: ${e.localizedMessage}")
                emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
            }
        }
    }.catch { e ->
        Log.e("PresenceRepository", "Flow error setting user online: ${e.localizedMessage}")
        emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
    }

    override fun setUserOffline(channelId: String, userId: String, agoraUid: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val presenceDocRef = firestore.collection(VOICE_PRESENCE_COLLECTION).document(channelId)
            // Use FieldValue.delete() to remove a field from a map
            presenceDocRef.update("${PARTICIPANTS_FIELD}.$userId", FieldValue.delete()).await()
            // Optional: Remove mapping if user is completely offline? Depends on logic.
            Log.d("PresenceRepository", "User $userId set offline in $channelId")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Log.e("PresenceRepository", "Error setting user offline: ${e.localizedMessage}")
            // Don't error harshly if document/field already deleted
            if (e.message?.contains("NOT_FOUND") == true || e.message?.contains("No document to update") == true){
                emit(Resource.Success(Unit)) // Consider it success if already gone
            } else {
                emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
            }
        }
    }.catch { e ->
        Log.e("PresenceRepository", "Flow error setting user offline: ${e.localizedMessage}")
        emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
    }

    // Listener for online users in a channel
    override fun getOnlineUsers(channelId: String): Flow<Resource<List<String>>> = callbackFlow {
        trySend(Resource.Loading())
        val presenceDocRef = firestore.collection(VOICE_PRESENCE_COLLECTION).document(channelId)

        val listener = presenceDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val participantsMap = snapshot.get(PARTICIPANTS_FIELD) as? Map<String, Any> ?: emptyMap()
                trySend(Resource.Success(participantsMap.keys.toList()))
            } else {
                trySend(Resource.Success(emptyList())) // No document or empty
            }
        }
        awaitClose { listener.remove() }
    }

    // --- UID Mapping ---
    // This is a simplified mapping stored separately.
    // A more robust solution might integrate this into the main user document.
    override fun storeUserAgoraUidMapping(userId: String, agoraUid: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val mappingDocRef = firestore.collection(USER_AGORA_MAPPING_COLLECTION).document(userId)
            mappingDocRef.set(mapOf("agoraUid" to agoraUid)).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to store UID mapping"))
        }
    }

    // Fetching Firebase UID from Agora UID (potentially slow - requires querying)
    override fun getUserIdFromAgoraUid(agoraUid: Int): Flow<Resource<String?>> = flow {
        emit(Resource.Loading())
        try {
            val querySnapshot = firestore.collection(USER_AGORA_MAPPING_COLLECTION)
                .whereEqualTo("agoraUid", agoraUid)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val userId = querySnapshot.documents.first().id
                emit(Resource.Success(userId))
            } else {
                emit(Resource.Success(null)) // No mapping found
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to get user ID from Agora UID"))
        }
    }

}
