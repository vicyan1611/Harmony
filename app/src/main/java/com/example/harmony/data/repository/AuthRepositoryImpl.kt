package com.example.harmony.data.repository

import com.example.harmony.core.common.Constants.ERROR_SOMETHING_WENT_WRONG
import com.example.harmony.core.common.Constants.USERS_COLLECTION
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.User
import com.example.harmony.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    override fun register(username: String, email: String, password: String): Flow<Resource<User>> =
        flow {
            try {
                emit(Resource.Loading<User>())
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception(ERROR_SOMETHING_WENT_WRONG)
                val user = User(
                    id = userId,
                    displayName = username,
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