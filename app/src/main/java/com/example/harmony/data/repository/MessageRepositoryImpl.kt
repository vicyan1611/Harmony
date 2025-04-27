package com.example.harmony.data.repository

import com.example.harmony.core.common.Constants.CHANNELS_COLLECTION
import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.MESSAGES_COLLECTION
import com.example.harmony.core.common.Constants.SERVERS_COLLECTION
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessageRepository {

    override fun sendMessage(
        serverId: String,
        channelId: String,
        message: Message
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val newMessageRef = firestore.collection(SERVERS_COLLECTION).document(serverId)
                .collection(CHANNELS_COLLECTION).document(channelId)
                .collection(MESSAGES_COLLECTION).document()

            val messageData = mapOf(
                "channelId" to channelId,
                "senderId" to message.senderId,
                "senderDisplayName" to message.senderDisplayName,
                "senderPhotoUrl" to message.senderPhotoUrl,
                "text" to message.text,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Use FieldValue for server timestamp
            )

            newMessageRef.set(messageData).await()
            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
        }
    }

    override fun getMessages(serverId: String, channelId: String): Flow<Resource<List<Message>>> = callbackFlow {
        val query = firestore.collection(SERVERS_COLLECTION).document(serverId)
            .collection(CHANNELS_COLLECTION).document(channelId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)


        val listenerRegistration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {

                trySend(Resource.Error(error.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
                close(error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val messages = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                }
                trySend(Resource.Success(messages))
            } else {
                trySend(Resource.Success(emptyList()))
            }

        }
        awaitClose { listenerRegistration.remove() }
    }
}