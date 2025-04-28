package com.example.harmony.data.repository

import android.net.Uri
import android.util.Log
import com.example.harmony.core.common.Constants.CHANNELS_COLLECTION
import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.MESSAGES_COLLECTION
import com.example.harmony.core.common.Constants.SERVERS_COLLECTION
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.MessageRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val authRepository: AuthRepository
) : MessageRepository {

    override fun sendMessage(
        serverId: String,
        channelId: String,
        message: Message,
        imageUri: Uri?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            emit(Resource.Error("User not authenticated"))
            return@flow
        }

        try {

            var finalImageUrl: String? = null

            if (imageUri != null) {
                val imageId = UUID.randomUUID().toString()
                val imagePath = "chat_images/${currentUser.id}/$imageId.jpg"
                val storageRef = storage.reference.child(imagePath)

                storageRef.putFile(imageUri).await()

                finalImageUrl = storageRef.downloadUrl.await()?.toString()
                if (finalImageUrl == null) {
                    emit(Resource.Error("Failed to get image download URL"))
                    return@flow
                }
            }


            val newMessageRef = firestore.collection(SERVERS_COLLECTION).document(serverId)
                .collection(CHANNELS_COLLECTION).document(channelId)
                .collection(MESSAGES_COLLECTION).document()

            val messageData = mutableMapOf<String, Any?>(
                "channelId" to channelId,
                "senderId" to message.senderId,
                "senderDisplayName" to message.senderDisplayName,
                "senderPhotoUrl" to message.senderPhotoUrl,
                "text" to message.text,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "reactions" to emptyMap<String, Int>()
            )

            if (finalImageUrl != null) {
                messageData["imageUrl"] = finalImageUrl
            } else if (message.text.isBlank()) {
                emit(Resource.Error("Cannot send empty message"))
                return@flow
            }

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

        val currentUserId = authRepository.getCurrentUser()?.id ?: return@callbackFlow
        val listenerRegistration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {

                trySend(Resource.Error(error.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
                close(error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val messages = snapshots.documents.mapNotNull { doc ->
                    Log.d("TTT", doc.toString())
                    try {
                        val tmp = doc.toObject(Message::class.java)?.copy(id = doc.id)
                        val reactions = tmp?.reactions
                        if (reactions!= null) {
                            tmp.currentUserReactionIndex = reactions[currentUserId]
                        }
                        tmp
                    } catch (e: Exception) {
                        println("Error parsing DM document ${doc.id}: ${e.message}")
                        null
                    }
                }
                trySend(Resource.Success(messages))
            } else {
                trySend(Resource.Success(emptyList()))
            }

        }
        awaitClose { listenerRegistration.remove() }
    }

    override fun updateMessageReaction(
        serverId: String,
        channelId: String,
        messageId: String,
        emojiIndex: Int?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val currentUserId = authRepository.getCurrentUser()?.id
        if (currentUserId == null) {
            emit(Resource.Error("User not authenticated"))
            return@flow
        }

        try {
            val messageRef = firestore.collection(SERVERS_COLLECTION).document(serverId)
                .collection(CHANNELS_COLLECTION).document(channelId)
                .collection(MESSAGES_COLLECTION).document(messageId)

            // Use dot notation for map field updates
            val reactionField = "reactions.$currentUserId"

            if (emojiIndex != null) {
                // Add or update the reaction
                messageRef.update(reactionField, emojiIndex).await()
            } else {
                // Remove the reaction using FieldValue.delete()
                messageRef.update(reactionField, FieldValue.delete()).await()
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
        }
    }
}