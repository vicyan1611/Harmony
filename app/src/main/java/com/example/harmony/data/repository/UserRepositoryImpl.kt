// data/repository/UserRepositoryImpl.kt
package com.example.harmony.data.repository

import android.net.Uri
import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.USERS_COLLECTION
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.model.UserSettings
import com.example.harmony.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {
    override fun getUser(): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())

        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Resource.Error("User not logged in"))
            awaitClose { }
            return@callbackFlow
        }

        val userRef = firestore.collection(USERS_COLLECTION).document(userId)

        val listenerRegistration = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
                close(error) // Close the flow on error
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
                if (user != null) {
                    trySend(Resource.Success(user))
                } else {
                    trySend(Resource.Error("Failed to parse user data"))
                }
            } else {
                trySend(Resource.Error("User document does not exist"))
            }
        }

        // Close the listener when the flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }

     override fun updateProfile(displayName: String?, avatarUri: Uri?): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        val userId = auth.currentUser?.uid
        if (userId == null) {
            emit(Resource.Error("User not logged in"))
            return@flow
        }

        try {
            val userRef = firestore.collection(USERS_COLLECTION).document(userId)
            val updates = mutableMapOf<String, Any>()
            var newAvatarUrl: String? = null

            // 1. Handle Avatar Upload (if URI is provided)
            if (avatarUri != null) {
                val storageRef = storage.reference.child("profile_images/$userId/${UUID.randomUUID()}.jpg")
                // Upload the file
                val uploadTask = storageRef.putFile(avatarUri).await()
                // Get the download URL
                newAvatarUrl = uploadTask.storage.downloadUrl.await().toString()
                updates["photoUrl"] = newAvatarUrl
            }

            // 2. Handle Username Update (if provided)
            if (displayName != null) {
                updates["displayName"] = displayName
            }

            // 3. Add timestamp
            updates["updatedAt"] = System.currentTimeMillis()


            // 4. Perform Firestore Update (if there are changes)
            if (updates.isNotEmpty()) {
                userRef.set(updates, SetOptions.merge()).await() // Use set with merge to only update specified fields
            } else {
                // No updates were actually staged, maybe return current data or specific message
                // Fetching current data anyway to ensure consistency
            }

            // 5. Fetch and return the updated user data
            val updatedSnapshot = userRef.get().await()
            val updatedUser = updatedSnapshot.toObject(User::class.java)?.copy(id = updatedSnapshot.id)

            if (updatedUser != null) {
                emit(Resource.Success(updatedUser))
            } else {
                emit(Resource.Error("Failed to fetch updated user data after update"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to update profile"))
        }
    }

    override fun updateUserSettings(userId: String, newSettings: UserSettings): Flow<Resource<UserSettings>> = flow {
        emit(Resource.Loading())
        if (userId.isBlank()) {
            emit(Resource.Error("User ID is invalid"))
            return@flow
        }
        try {
            val userRef = firestore.collection(USERS_COLLECTION).document(userId)
            val settingsWithTimestamp = newSettings.copy(updatedAt = System.currentTimeMillis())
            userRef.update("settings", settingsWithTimestamp).await()
            emit(Resource.Success(settingsWithTimestamp))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to update settings"))
        }
    }
}