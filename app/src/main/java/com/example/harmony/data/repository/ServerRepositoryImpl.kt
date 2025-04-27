package com.example.harmony.data.repository

import android.net.Uri
import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Channel
import com.example.harmony.domain.model.Server
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ServerRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ServerRepositoryImpl @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ServerRepository {
    companion object {
        const val SERVER_HOST_USER_ID_FIELD = "ownerId"
        const val SERVER_PROFILE_URL_FIELD = "profileUrl"
        const val SERVER_MEMBERS_LIST_FIELD = "members"
        const val SERVER_ID_FIELD = "id"
        const val SERVER_NAME_FIELD = "name"
        const val MAX_N_SERVER_PER_USER = 20

//        const val CHANNEL_DESCRIPTION_FIELD = "description"
//        const val CHANNEL_ID_FIELD = "id"
//        const val CHANNEL_NAME_FIELD = "name"
//        const val CHANNEL_SERVER_ID_FIELD = "server"
//        const val CHANNEL_MESSAGES_LIST_FIELD =  "messages"
    }
    override fun createServer(
        name: String,
        hostUserId: String,
        profilePicture: String
    ): Flow<Resource<Server>> = flow {
        try {
            emit(Resource.Loading<Server>())
            // --- Step 1: Check Server Limit ---
            val countQuery = firestore.collection(Constants.SERVERS_COLLECTION)
                .whereEqualTo(SERVER_HOST_USER_ID_FIELD, hostUserId) // Use constant
            val countSnapshot = countQuery.count().get(AggregateSource.SERVER).await()
            val currentServerCount = countSnapshot.count

            if (currentServerCount >= MAX_N_SERVER_PER_USER) { // Use constant
                // Emit specific error and finish the flow
                emit(Resource.Error<Server>("User has reached maximum number of servers ($MAX_N_SERVER_PER_USER)"))
                return@flow
            }

            // --- Step 2: Create Server Document (if limit not reached) ---
            val serverDocument = firestore.collection(Constants.SERVERS_COLLECTION).document() // Auto-generate ID
            val serverId = serverDocument.id

            // Prepare data - Use consistent field names from your constants
            val newServerData = hashMapOf(
                SERVER_HOST_USER_ID_FIELD to hostUserId,
                SERVER_NAME_FIELD to name,
                SERVER_ID_FIELD to serverId,
                SERVER_PROFILE_URL_FIELD to profilePicture,
                // SERVER_CHANNELS_LIST_FIELD to emptyList<DocumentReference>(),
                SERVER_MEMBERS_LIST_FIELD to emptyList<DocumentReference>()
            )

            // Write to Firestore
            serverDocument.set(newServerData).await() // Perform the actual write

            // --- Step 3: Emit Success with Correct Data ---
            val createdServer = Server( // Create Server object from actual data
                id = serverId,
                name = name,
                ownerId = hostUserId, // Ensure this matches your Server data class field name
                profileUrl = profilePicture, // Ensure this matches your Server data class field name
                // channels = emptyList(), // Ensure this matches your Server data class field name
                memberIds = emptyList() // Ensure this matches your Server data class field name
            )
            emit(Resource.Success<Server>(createdServer))

        } catch (t: Throwable) {
            emit(Resource.Error<Server>(t.message.toString()))
        }
    }

    override fun countServersHostedByUser(userId: String): Flow<Resource<Long>> = flow {
        emit(Resource.Loading<Long>())
        val countQuery = firestore.collection(Constants.SERVERS_COLLECTION)
            .whereEqualTo(SERVER_HOST_USER_ID_FIELD, userId)
        val countSnapshot = countQuery.count().get(AggregateSource.SERVER).await()
        emit(Resource.Success<Long>(countSnapshot.count))
    }.catch { exception ->
        emit(Resource.Error<Long>(exception.localizedMessage ?: "Failed to count servers"))
    }

    override fun getServerById(serverId: String): Flow<Resource<Server>> = flow {
        try {
            val documentSnapshot = firestore.collection(Constants.SERVERS_COLLECTION)
                .document(serverId)
                .get()
                .await()
            val server = documentSnapshot.toObject(Server::class.java)
            if (server != null) {
                emit(Resource.Success<Server>(server))
            } else {
                throw Exception("Server not found or data conversion failed.")
            }
        } catch (t: Throwable) {
            emit(Resource.Error<Server>(t.message.toString()))
        }
    }

    override fun updateServer(serverId: String, name: String?, profileUrl: String?): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading<Unit>())
            val serverRef =
                Firebase.firestore.collection(Constants.SERVERS_COLLECTION).document(serverId)
            if (name != null) {
                serverRef.update(SERVER_NAME_FIELD, name).await()
            }
            if (profileUrl != null) {
                serverRef.update(SERVER_PROFILE_URL_FIELD, profileUrl).await()
            }
            emit(Resource.Success<Unit>(Unit))
        } catch (t: Throwable) {
            emit(Resource.Error<Unit>(t.message.toString()))
        }
    }

    override fun deleteServer(serverId: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading<Unit>())
            val serverRef =
                Firebase.firestore.collection(Constants.SERVERS_COLLECTION).document(serverId)
            serverRef.delete().await()
            emit(Resource.Success<Unit>(Unit))
        } catch (t: Throwable) {
            emit(Resource.Error<Unit>(t.message.toString()))
        }
    }

//    override fun getServerListByUser(): Flow<Resource<List<Server>>> = flow {
//        // override fun getServerListByUser(): Flow<Resource<ArrayList<Server>>> = flow { // If ArrayList is strictly needed
//        emit(Resource.Loading())
//
//        // Get current user ID (requires AuthRepository)
//        val currentUserId = authRepository.getCurrentUser()?.id // Assuming this method exists
//
//        if (currentUserId == null) {
//            emit(Resource.Error("User not logged in."))
//            return@flow
//        }
//
//        // Construct DocumentReference for the current user
//        val currentUserRef = firestore.collection(Constants.USERS_COLLECTION).document(currentUserId)
//
//        val (memberServers, hostedServers) = coroutineScope {
//            val memberQuery = firestore.collection(Constants.SERVERS_COLLECTION)
//                .whereArrayContains(SERVER_MEMBERS_LIST_FIELD, currentUserRef)
//                .get()
//            val hostQuery = firestore.collection(Constants.SERVERS_COLLECTION)
//                .whereEqualTo(SERVER_HOST_USER_ID_FIELD, currentUserId)
//                .get()
//            val memberSnapshot = async { memberQuery.await() }
//            val hostSnapshot = async { hostQuery.await() }
//
//            Pair(memberSnapshot.await(), hostSnapshot.await())
//        }
//
//        val combinedServers = mutableMapOf<String, Server>()
//        memberServers.documents.forEach { document ->
//            val server = document.toObject(Server::class.java) //?.copy(id = document.id) // Adjust if ID needs manual setting
//            if (server != null) {
//                combinedServers[document.id] = server // Add/overwrite in map
//            }
//        }
//
//        hostedServers.documents.forEach { document ->
//            val server = document.toObject(Server::class.java) //?.copy(id = document.id) // Adjust if ID needs manual setting
//            if (server != null) {
//                combinedServers[document.id] = server // Add/overwrite in map (handles duplicates)
//            }
//        }
//
//        // Emit success with the list
//        emit(Resource.Success(combinedServers.values.toList()))
//    }.catch { exception ->
//        emit(Resource.Error(exception.localizedMessage ?: "Failed to fetch server list"))
//    }

    override fun addMemberToServer(serverId: String, memberId: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading<Unit>())
            val serverRef =
                Firebase.firestore.collection(Constants.SERVERS_COLLECTION).document(serverId)
            serverRef.update(SERVER_MEMBERS_LIST_FIELD, FieldValue.arrayUnion(memberId)).await()
            emit(Resource.Success<Unit>(Unit))
        } catch (t: Throwable) {
            emit(Resource.Error<Unit>(t.message.toString()))
        }
    }
}