package com.example.harmony.domain.model

import com.example.harmony.core.common.Resource
import kotlinx.coroutines.flow.Flow

data class Server (
    val name: String = "",
    val id: String = "",
    val hostUserId: String = "",
    val profilePicture: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Server

        if (name != other.name) return false
        if (id != other.id) return false
        if (hostUserId != other.hostUserId) return false
        if (profilePicture != null) {
            if (other.profilePicture == null) return false
            if (!profilePicture.contentEquals(other.profilePicture)) return false
        } else if (other.profilePicture != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + hostUserId.hashCode()
        result = 31 * result + (profilePicture?.contentHashCode() ?: 0)
        return result
    }


}