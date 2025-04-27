// Create file: harmony/domain/use_case/server/UpdateServerUseCase.kt
package com.example.harmony.domain.use_case.server

import android.net.Uri
import com.example.harmony.core.common.Constants
import com.example.harmony.core.common.Resource
import com.example.harmony.domain.model.Server
import com.example.harmony.domain.repository.AuthRepository
import com.example.harmony.domain.repository.ServerRepository
// import com.example.harmony.domain.repository.StorageRepository // Needed for image upload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class UpdateServerUseCase @Inject constructor(
    private val serverRepository: ServerRepository,
    private val authRepository: AuthRepository
    // private val storageRepository: StorageRepository // Inject when implemented
) {
    /**
     * Updates server details (name and/or profile picture).
     * Requires the user invoking this to be the owner of the server.
     *
     * @param serverId The ID of the server to update.
     * @param newName The new name for the server. Can be null if only updating the picture.
     * @param newImageUri The URI of the new profile picture. Can be null if only updating the name.
     * @return A Flow emitting Resource<Unit> indicating success or failure.
     */
    operator fun invoke(
        serverId: String,
        newName: String?,
        newImageUri: Uri?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val currentUserId = authRepository.getCurrentUser()?.id
        if (currentUserId == null) {
            emit(Resource.Error("User not logged in."))
            return@flow
        }

        // Basic validation
        if (newName == null && newImageUri == null) {
            emit(Resource.Error("No changes provided to update."))
            return@flow
        }
        if (newName != null && newName.isBlank()) {
            emit(Resource.Error("Server name cannot be blank."))
            return@flow
        }


        try {
            // Step 1: Verify Ownership (Crucial Security Check)
            // It's safer to re-fetch the server details here to ensure the current user is still the owner
            // or pass the owner check responsibility to the repository layer.
            // Assuming repository method handles owner check or we fetch here.
            val serverResource = serverRepository.getServerById(serverId).last()
            if (serverResource is Resource.Error || serverResource.data?.ownerId != currentUserId) {
                emit(Resource.Error("Permission denied or server not found."))
                return@flow
            }

            var newImageUrl: String? = null

            // Step 2: Upload new image if provided (Placeholder)
            if (newImageUri != null) {
                // TODO: Implement image upload logic using StorageRepository
                // val uploadResult = storageRepository.uploadServerImage(serverId, newImageUri).first { it !is Resource.Loading }
                // if (uploadResult is Resource.Success) {
                //     newImageUrl = uploadResult.data
                // } else {
                //     emit(Resource.Error("Image upload failed: ${uploadResult.message}"))
                //     return@flow
                // }
//                emit(Resource.Error("Image upload not implemented yet."))
//                return@flow // Remove this when image upload is ready
            }

            // Step 4: Call Repository to update Firestore
            // Assumes ServerRepository has an `updateServer` method
             val updateResult = serverRepository.updateServer(serverId, newName, newImageUrl).last()

            when(updateResult) {
                is Resource.Success -> emit(Resource.Success(Unit))
                is Resource.Error -> emit(Resource.Error(updateResult.message ?: "Failed to update server details."))
                is Resource.Loading -> emit(Resource.Loading()) // Should ideally not happen if we await first non-loading
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: Constants.ERROR_SOMETHING_WENT_WRONG))
        }
    }
}