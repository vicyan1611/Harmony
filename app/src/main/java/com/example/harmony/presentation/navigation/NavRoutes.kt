package com.example.harmony.presentation.navigation

object NavRoutes {
    // Auth Routes
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Main Routes
    const val HOME = "home"
    const val CREATE_SERVER = "create_server/"
    const val CONFIG_SERVER = "config_server/{serverId}"
    const val SERVER_DETAIL = "server_detail/{serverId}"
    const val CHANNEL_DETAIL = "channel_detail/{channelId}"
    const val CHAT = "server/{serverId}/channel/{channelId}/chat"
    const val VOICE_CHANNEL = "server/{serverId}/channel/{channelId}/voice"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val JOIN_SERVER = "join_server"

    // Direct Message Routes
    const val DM_LIST = "dm_list"
    const val DM_CHAT = "dm_chat/{conversationId}"
    // Optional: Add a route for user search to initiate DMs
    const val USER_SEARCH = "user_search"

    const val EDIT_PROFILE = "edit_profile"
    const val MY_PROFILE = "my_profile"

    // Utility function for route with parameters
    fun getServerDetailRoute(serverId: String): String {
        return "server_detail/$serverId"
    }

    fun getConfigServerRoute(serverId: String): String {
        // Replace the placeholder with the actual serverId
        return "config_server/$serverId"
    }

    fun getChannelDetailRoute(channelId: String): String {
        return "channel_detail/$channelId"
    }

    fun getChatRoute(serverId: String, channelId: String): String {
        return "server/$serverId/channel/$channelId/chat"
    }

    fun getVoiceChannelRoute(serverId: String, channelId: String): String {
        return "server/$serverId/channel/$channelId/voice"
    }

    fun getDmChatRoute(conversationId: String): String {
        return "dm_chat/$conversationId"
    }
}