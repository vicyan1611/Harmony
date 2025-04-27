package com.example.harmony.data.repository

import com.example.harmony.core.common.Constants.DIRECT_MESSAGES_COLLECTION
import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.MESSAGES_COLLECTION
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.DirectMessageConversation
import com.example.harmony.domain.model.Message
import com.example.harmony.domain.model.ParticipantInfo
import com.example.harmony.domain.repository.DirectMessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DirectMessageRepositoryImpl @Inject constructor(private val firestore: FirebaseFirestore) : DirectMessageRepository {

    private fun getConversationId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    override fun getDirectMessageConversations(userId: String): Flow<Resource<List<DirectMessageConversation>>> =
        callbackFlow {
            trySend(Resource.Loading())

            val query = firestore.collection(DIRECT_MESSAGES_COLLECTION)
                .whereArrayContains("participants", userId)
                .orderBy("lastActivity", Query.Direction.DESCENDING)

            val listenerRegistration = query.addSnapshotListener {snapshots, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val conversations = snapshots.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        val participants = data["participants"] as? List<String> ?: emptyList()
                        val participantDetailsRaw =
                            data["participantDetails"] as? Map<String, Map<String, Any?>>
                                ?: emptyMap()
                        val participantDetails = participantDetailsRaw.mapValues { (_, value) ->
                            ParticipantInfo(
                                displayName = value["displayName"] as? String ?: "",
                                photoUrl = value["photoUrl"] as? String
                            )
                        }
                        val lastMessageText = data["lastMessageText"] as? String
                        val lastMessageSenderId = data["lastMessageSenderId"] as? String
                        val lastActivity = data["lastActivity"] as? Timestamp
                        val createdAt = data["createdAt"] as? Timestamp

                        DirectMessageConversation(
                            id = doc.id,
                            participants = participants,
                            participantDetails = participantDetails,
                            lastMessageText = lastMessageText,
                            lastMessageSenderId = lastMessageSenderId,
                            lastActivity = lastActivity,
                            createdAt = createdAt
                        )
                    }
                    trySend(Resource.Success(conversations))
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }
            awaitClose { listenerRegistration.remove() }
        }


    override fun getDirectMessageConversation(conversationId: String): Flow<Resource<DirectMessageConversation>> = callbackFlow {
        trySend(Resource.Loading())

        val docRef = firestore.collection(DIRECT_MESSAGES_COLLECTION).document(conversationId)

        val listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: run {
                    trySend(Resource.Error("Conversation data is null"))
                    return@addSnapshotListener
                }
                val participants = data["participants"] as? List<String> ?: emptyList()
                val participantDetailsRaw = data["participantDetails"] as? Map<String, Map<String, Any?>> ?: emptyMap()
                val participantDetails = participantDetailsRaw.mapValues { (_, value) ->
                    ParticipantInfo(
                        displayName = value["displayName"] as? String ?: "",
                        photoUrl = value["photoUrl"] as? String
                    )
                }
                val lastMessageText = data["lastMessageText"] as? String
                val lastMessageSenderId = data["lastMessageSenderId"] as? String
                val lastActivity = data["lastActivity"] as? Timestamp
                val createdAt = data["createdAt"] as? Timestamp

                val conversation = DirectMessageConversation(
                    id = snapshot.id,
                    participants = participants,
                    participantDetails = participantDetails,
                    lastMessageText = lastMessageText,
                    lastMessageSenderId = lastMessageSenderId,
                    lastActivity = lastActivity,
                    createdAt = createdAt
                )
                trySend(Resource.Success(conversation))

            } else if (snapshot != null && !snapshot.exists()) {
                trySend(Resource.Error("Conversation not found"))
            } else {
                trySend(Resource.Error("Snapshot was null"))
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override fun getDirectMessages(conversationId: String): Flow<Resource<List<Message>>> = callbackFlow {
        trySend(Resource.Loading())
        val query = firestore.collection(DIRECT_MESSAGES_COLLECTION).document(conversationId)
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


    override fun sendDirectMessage(
        conversationId: String,
        message: Message,
        currentUserInfo: ParticipantInfo,
        otherUserInfo: ParticipantInfo
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val conversationRef = firestore.collection(DIRECT_MESSAGES_COLLECTION).document(conversationId)
            val newMessageRef = conversationRef.collection(MESSAGES_COLLECTION).document()

            val messageData = mapOf(
                "senderId" to message.senderId,
                "senderDisplayName" to message.senderDisplayName,
                "senderPhotoUrl" to message.senderPhotoUrl,
                "text" to message.text,
                "timestamp" to FieldValue.serverTimestamp()
            )

            newMessageRef.set(messageData).await()

            val conversationUpdateData = mapOf(
                "lastMessageText" to message.text,
                "lastMessageSenderId" to message.senderId,
                "lastActivity" to FieldValue.serverTimestamp()

            )

            conversationRef.set(conversationUpdateData, SetOptions.merge()).await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
        }
    }

    override fun getOrCreateDirectMessageConversation(
        userId1: String,
        userId2: String,
        userInfo1: ParticipantInfo,
        userInfo2: ParticipantInfo
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val conversationId = getConversationId(userId1, userId2)
        val conversationRef = firestore.collection(DIRECT_MESSAGES_COLLECTION).document(conversationId)

        try {
            val snapshot = conversationRef.get().await()
            if (snapshot.exists()) {
                emit(Resource.Success(conversationId))
            } else {
                val newConversationData = mapOf(
                    "participants" to listOf(userId1, userId2),
                    "participantDetails" to mapOf(
                        userId1 to mapOf("displayName" to userInfo1.displayName, "photoUrl" to userInfo1.photoUrl),
                        userId2 to mapOf("displayName" to userInfo2.displayName, "photoUrl" to userInfo2.photoUrl)
                    ),
                    "lastActivity" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp()

                )
                conversationRef.set(newConversationData).await()
                emit(Resource.Success(conversationId))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
        }
    }


}