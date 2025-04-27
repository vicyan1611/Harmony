package com.example.harmony.data.repository

import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.UserRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
): UserRepository {
    companion object {
        const val USER_LIST_JOINED_SERVER_IDS_FIELD = "listJoinedServerIds"
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        val currentUserId = authRepository.getCurrentUser()?.id

        try {
            if (query.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val usersSnapshot = firestore.collection(Constants.USERS_COLLECTION)
                .orderBy("displayName")
                .whereGreaterThanOrEqualTo("displayName", query)
                .whereLessThanOrEqualTo("displayName", query + '\uf8ff')
                .limit(20)
                .get()
                .await()

            val users = usersSnapshot.documents.mapNotNull { doc ->
                if (doc.id != currentUserId) {
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                } else {
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

    override fun getCollectionUser(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
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
                emit(Resource.Error("User document not found."))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }.catch { exception ->
        // Catch exceptions specific to the flow itself
        emit(Resource.Error(exception.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
    }
}