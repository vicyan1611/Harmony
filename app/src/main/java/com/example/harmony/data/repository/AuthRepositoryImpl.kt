package com.example.harmony.data.repository

import android.util.Log
import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.USERS_COLLECTION
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading<User>())
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception(ERROR_SOMETHING_WENT_WRONG)

            val userDoc = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            val user = userDoc.toObject(User::class.java)?.copy(id = userId)
                ?: throw Exception(ERROR_SOMETHING_WENT_WRONG)
            emit(Resource.Success<User>(user))
        } catch (e: Exception) {
            emit(Resource.Error<User>(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
        }
    }

    override fun signInWithGoogle(idToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Firebase user is null after Google Sign-In")


            val user = getOrCreateUserInFirestore(firebaseUser)

            emit(Resource.Success(user))

        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Google Sign-In Error: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
        }
    }

    private suspend fun getOrCreateUserInFirestore(firebaseUser: FirebaseUser): User {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
        val snapshot = userDocRef.get().await()

        if (snapshot.exists()) {
            val existingUser = snapshot.toObject(User::class.java)

            val updatedUser = existingUser?.copy(
                displayName = firebaseUser.displayName ?: existingUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl
            ) ?: throw IllegalStateException("Existing user data null")


            if (updatedUser.displayName != existingUser.displayName || updatedUser.photoUrl != existingUser.photoUrl) {
                userDocRef.set(updatedUser, SetOptions.merge()).await()
            }
            return updatedUser

        } else {

            val newUser = User(
                id = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: "Harmony User", // Provide a default
                email = firebaseUser.email ?: "", // Google usually provides email
                photoUrl = firebaseUser.photoUrl?.toString(),
                createdAt = System.currentTimeMillis(), // Set creation time
                updatedAt = System.currentTimeMillis()
            )
            userDocRef.set(newUser).await()
            return newUser
        }
    }

    override fun register(displayName: String, email: String, password: String): Flow<Resource<User>> =
        flow {
            try {
                emit(Resource.Loading<User>())
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: throw Exception(ERROR_SOMETHING_WENT_WRONG)
                val userId = firebaseUser.uid

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    // .setPhotoUri(Uri.parse("your_photo_url")) // Optionally set photo URL here too
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                val user = User(
                    id = userId,
                    displayName = displayName,
                    email = email
                )
                firestore.collection(USERS_COLLECTION).document(userId).set(user).await()
                emit(Resource.Success<User>(user))
            } catch (e: Exception) {

                emit(Resource.Error(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
            }
        }

    override fun logout(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            auth.signOut()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: ERROR_SOMETHING_WENT_WRONG))
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString()
        )
    }
    
    override fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
}