package com.example.harmony.data.repository

import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Channel
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ChannelRepository
import com.example.harmony.domain.repository.ServerRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import org.w3c.dom.DocumentType
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ChannelRepository {
    companion object {
        const val CHANNEL_DESCRIPTION_FIELD = "description"
        const val CHANNEL_ID_FIELD = "id"
        const val CHANNEL_NAME_FIELD = "name"
        const val CHANNEL_SERVER_ID_FIELD = "server"
        const val SERVER_CHANNELS_LIST_FIELD =  "channels"
        const val CHANNEL_MESSAGES_LIST_FIELD =  "messages"
    }

    override fun createChannel(
        name: String,
        description: String,
        serverId: String
    ): Flow<Resource<Channel>> = flow {
        emit(Resource.Loading())

        // --- Prepare References and Data ---
        val newChannelRef = firestore.collection(Constants.SERVERS_COLLECTION).document(serverId).collection(Constants.CHANNELS_COLLECTION).document() // Auto-ID for channel
        val channelId = newChannelRef.id

        // Data for the new channel document
        val newChannelData = hashMapOf(
            CHANNEL_ID_FIELD to channelId,
            CHANNEL_NAME_FIELD to name,
            CHANNEL_DESCRIPTION_FIELD to description,
            CHANNEL_SERVER_ID_FIELD to serverId,
            CHANNEL_MESSAGES_LIST_FIELD to emptyList<DocumentType>()
            // Add other channel fields like type (TEXT/VOICE), etc.
        )

        // --- Perform Atomic Write using WriteBatch ---
        val batch = firestore.batch()

        // Operation 1: Create the new channel document
        batch.set(newChannelRef, newChannelData)

        // Operation 2: Atomically add the new channel reference to the server's 'channels' array
//        batch.update(serverRef, SERVER_CHANNELS_LIST_FIELD, FieldValue.arrayUnion(newChannelRef))

        // Commit the batch
        batch.commit().await()

        // --- Emit Success ---
        // Construct the Channel object to return (ensure Channel model matches data)
        val createdChannel = Channel(
            id = channelId,
            name = name,
            description = description
        )
        emit(Resource.Success(createdChannel))

    }.catch { exception ->
        emit(Resource.Error(exception.localizedMessage ?: "Failed to create channel"))
    }

    override fun getListOfChannelsByServerId(serverId: String): Flow<Resource<List<Channel>>> = flow {
        emit(Resource.Loading()) // Start loading

        // Query the "channels" SUBCOLLECTION under the specific server document
        val querySnapshot = firestore.collection(Constants.SERVERS_COLLECTION) // Use Constants.kt [cite: 1]
            .document(serverId) // Target the specific server
            .collection(Constants.CHANNELS_COLLECTION) // Target the "channels" subcollection [cite: 1]
            .get()
            .await() // Execute the query

        // Map the resulting documents to a list of Channel objects
        // Ensure Channel data class fields match Firestore fields
        val channelList = querySnapshot.documents.mapNotNull { document ->
            // Map Firestore document to Channel object.
            // Assuming Channel model has an 'id' field. Use @DocumentId or set manually.
            document.toObject(Channel::class.java)?.copy(id = document.id) // Example setting ID from document ID
        }
        // Emit success with the list of channels (metadata only)
        emit(Resource.Success(channelList))

    }.catch { exception ->
        // Handle exceptions during the Firestore operation or mapping
        emit(Resource.Error(exception.localizedMessage ?: "Failed to fetch channels for server $serverId"))
    }
}