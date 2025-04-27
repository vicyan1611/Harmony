// harmony/data/repository/UserRepositoryImpl.kt
package com.example.harmony.data.repository

import android.net.Uri
import android.util.Log // Import Log
import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.USERS_COLLECTION
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.model.UserSettings
import com.example.harmony.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration // Import ListenerRegistration
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

    // This flow now primarily focuses on listening to Firestore *if* the user is logged in when called.
    // It will emit an error and complete if the user is not logged in at call time.
    override fun getUser(): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading()) // Start with loading

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d("UserRepositoryImpl", "getUser: User not logged in, sending error.")
            trySend(Resource.Error("User not logged in"))
            close() // Close the flow as there's nothing more to listen to *for this specific call*
            return@callbackFlow
        }

        Log.d("UserRepositoryImpl", "getUser: User logged in ($userId), attaching Firestore listener.")
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)
        var listenerRegistration: ListenerRegistration? = null // Define here

        listenerRegistration = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserRepositoryImpl", "getUser: Firestore listener error", error)
                trySend(Resource.Error(error.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
                // Optionally close on persistent errors, but maybe let it retry?
                // close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
                if (user != null) {
                    Log.d("UserRepositoryImpl", "getUser: Sending user data: ${user.displayName}")
                    trySend(Resource.Success(user))
                } else {
                    Log.w("UserRepositoryImpl", "getUser: Failed to parse user data from snapshot.")
                    trySend(Resource.Error("Failed to parse user data"))
                }
            } else {
                // Snapshot might be null briefly during setup or if document deleted
                Log.w("UserRepositoryImpl", "getUser: User document does not exist for user $userId.")
                trySend(Resource.Error("User document does not exist"))
            }
        }

        // This is crucial: close the Firestore listener when the flow collection stops.
        awaitClose {
            Log.d("UserRepositoryImpl", "getUser: Closing Firestore listener for user $userId.")
            listenerRegistration?.remove()
        }
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
                Log.d("UserRepositoryImpl", "updateProfile: Updating profile with: $updates")
                userRef.set(updates, SetOptions.merge()).await() // Use set with merge to only update specified fields
            } else {
                Log.d("UserRepositoryImpl", "updateProfile: No profile changes detected.")
            }

            // 5. Fetch and return the updated user data
            val updatedSnapshot = userRef.get().await()
            val updatedUser = updatedSnapshot.toObject(User::class.java)?.copy(id = updatedSnapshot.id)

            if (updatedUser != null) {
                Log.d("UserRepositoryImpl", "updateProfile: Successfully updated and returning user.")
                emit(Resource.Success(updatedUser))
            } else {
                Log.w("UserRepositoryImpl", "updateProfile: Failed to fetch updated user data after update.")
                emit(Resource.Error("Failed to fetch updated user data after update"))
            }

        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "updateProfile: Error updating profile", e)
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
            Log.d("UserRepositoryImpl", "updateUserSettings: Updating settings for user $userId: $settingsWithTimestamp")
            userRef.update("settings", settingsWithTimestamp).await()
            emit(Resource.Success(settingsWithTimestamp))
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "updateUserSettings: Error updating settings", e)
            emit(Resource.Error(e.localizedMessage ?: "Failed to update settings"))
        }
    }
}