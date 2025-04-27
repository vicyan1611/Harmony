// harmony/data/repository/UserRepositoryImpl.kt
package com.example.harmony.data.repository

import android.net.Uri
import android.util.Log // Import Log
import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.USERS_COLLECTION
import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.model.UserSettings
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration // Import ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
class UserRepositoryImpl @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {
    private val authRepository: AuthRepository
): UserRepository {
    companion object {
        const val USER_LIST_JOINED_SERVER_IDS_FIELD = "listJoinedServerIds"
    }

    // This flow now primarily focuses on listening to Firestore *if* the user is logged in when called.
    // It will emit an error and complete if the user is not logged in at call time.
    override fun getUser(): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading()) // Start with loading
    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        val currentUserId = authRepository.getCurrentUser()?.id

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d("UserRepositoryImpl", "getUser: User not logged in, sending error.")
            trySend(Resource.Error("User not logged in"))
            close() // Close the flow as there's nothing more to listen to *for this specific call*
            return@callbackFlow
        }
        try {
            if (query.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

        Log.d("UserRepositoryImpl", "getUser: User logged in ($userId), attaching Firestore listener.")
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)
        var listenerRegistration: ListenerRegistration? = null // Define here
            val usersSnapshot = firestore.collection(Constants.USERS_COLLECTION)
                .orderBy("displayName")
                .whereGreaterThanOrEqualTo("displayName", query)
                .whereLessThanOrEqualTo("displayName", query + '\uf8ff')
                .limit(20)
                .get()
                .await()

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
            val users = usersSnapshot.documents.mapNotNull { doc ->
                if (doc.id != currentUserId) {
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                } else {
                    Log.w("UserRepositoryImpl", "getUser: Failed to parse user data from snapshot.")
                    trySend(Resource.Error("Failed to parse user data"))
                    null
                }
            }
            emit(Resource.Success(users))

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }

    }

    override fun getListJoinedServerIdByUser(userId: String): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            val documentSnapshot = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                // Attempt to get the field as a List of DocumentReferences
                @Suppress("UNCHECKED_CAST")
                val serverRefs = documentSnapshot.get(USER_LIST_JOINED_SERVER_IDS_FIELD) as? List<DocumentReference>

                if (serverRefs != null) {
                    // Map the references to their document IDs (which are the server IDs)
                    val serverIds = serverRefs.map { it.id }
                    emit(Resource.Success(serverIds))
                } else {
                    // Field might be missing or not a List<DocumentReference>
                    emit(Resource.Success(emptyList())) // Return empty list if field is missing/null
                }
            } else {
                // Snapshot might be null briefly during setup or if document deleted
                Log.w("UserRepositoryImpl", "getUser: User document does not exist for user $userId.")
                trySend(Resource.Error("User document does not exist"))
                emit(Resource.Error("User document not found."))
            }
        } catch (e: ClassCastException) {
            // Specific handling if the field is not the expected type
            emit(Resource.Error("Firestore field '$USER_LIST_JOINED_SERVER_IDS_FIELD' is not a List of References."))
        } catch (e: Exception) {
            // Handle other exceptions (network, etc.)
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }.catch { exception ->
        emit(Resource.Error(exception.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
    }

        // This is crucial: close the Firestore listener when the flow collection stops.
        awaitClose {
            Log.d("UserRepositoryImpl", "getUser: Closing Firestore listener for user $userId.")
            listenerRegistration?.remove()
    override fun appendListJoinedServerIds(userId: String, serverId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading()) // Indicate the operation is starting
        try {
            // Get references to the user and server documents
            val userDocRef = firestore.collection(Constants.USERS_COLLECTION).document(userId)
            val serverDocRef = firestore.collection(Constants.SERVERS_COLLECTION).document(serverId)

            // Atomically add the server reference to the user's listJoinedServers array
            // FieldValue.arrayUnion ensures the reference is only added if it's not already present
            userDocRef.update(USER_LIST_JOINED_SERVER_IDS_FIELD, FieldValue.arrayUnion(serverDocRef))
                .await() // Wait for the update operation to complete

            emit(Resource.Success(Unit)) // Indicate success
        } catch (e: Exception) {
            // Handle potential exceptions (e.g., network error, permissions issue, document not found)
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }.catch { exception ->
        // Catch exceptions specific to the flow itself
        emit(Resource.Error(exception.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
    }

    override fun updateProfile(displayName: String?, avatarUri: Uri?): Flow<Resource<User>> = flow {
    override fun getCollectionUser(userId: String): Flow<Resource<User>> = flow {
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
            val documentSnapshot = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)
                if (user != null) {
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("User data is null."))
                }
            } else {
                Log.w("UserRepositoryImpl", "updateProfile: Failed to fetch updated user data after update.")
                emit(Resource.Error("Failed to fetch updated user data after update"))
                emit(Resource.Error("User document not found."))
            }

        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "updateProfile: Error updating profile", e)
            emit(Resource.Error(e.localizedMessage ?: "Failed to update profile"))
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }.catch { exception ->
        // Catch exceptions specific to the flow itself
        emit(Resource.Error(exception.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
    }

    override fun updateUserSettings(userId: String, newSettings: UserSettings): Flow<Resource<UserSettings>> = flow {
    override fun removeServerFromAllUsers(serverId: String): Flow<Resource<Unit>> = flow {
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
        try {
            // 1. Get the reference to the server being removed
            val serverDocRef = firestore.collection(Constants.SERVERS_COLLECTION).document(serverId)

            // 2. Query users containing this server reference (INEFFICIENT & REQUIRES INDEX)
            // This query requires a composite index on 'listJoinedServerIds' in Firestore.
            // It fetches potentially ALL user documents containing the reference.
            val usersQuery = firestore.collection(Constants.USERS_COLLECTION)
                .whereArrayContains(USER_LIST_JOINED_SERVER_IDS_FIELD, serverDocRef)

            val usersSnapshot = usersQuery.get().await()

            if (usersSnapshot.isEmpty) {
                // No users found with this server, operation successful.
                emit(Resource.Success(Unit))
                return@flow
            }

            // 3. Use WriteBatch for atomic removal (up to 500 writes per batch)
            var batch: WriteBatch = firestore.batch()
            var writeCount = 0

            for (userDoc in usersSnapshot.documents) {
                val userRef = userDoc.reference
                // Add an update operation to remove the server reference from the array
                batch.update(userRef, USER_LIST_JOINED_SERVER_IDS_FIELD, FieldValue.arrayRemove(serverDocRef))
                writeCount++

                // Firestore batches have a limit (e.g., 500 writes). Commit and create a new batch if needed.
                if (writeCount >= 499) { // Commit slightly before limit for safety
                    batch.commit().await()
                    batch = firestore.batch() // Start a new batch
                    writeCount = 0
                }
            }

            // 4. Commit any remaining operations in the last batch
            if (writeCount > 0) {
                batch.commit().await()
            }

            emit(Resource.Success(Unit)) // Indicate overall success

        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "updateUserSettings: Error updating settings", e)
            emit(Resource.Error(e.localizedMessage ?: "Failed to update settings"))
            // Handle potential exceptions (network, permissions, query errors, batch errors)
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }.catch { exception ->
        // Catch exceptions specific to the flow itself
        emit(Resource.Error(exception.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
    }
}