package com.example.harmony.presentation.navigation

object NavRoutes {
    // Auth Routes
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Main Routes
    const val HOME = "home"
    const val SERVER_DETAIL = "server_detail/{serverId}"
    const val CHANNEL_DETAIL = "channel_detail/{channelId}"
    const val CHAT = "server/{serverId}/channel/{channelId}/chat"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    // Utility function for route with parameters
    fun getServerDetailRoute(serverId: String): String {
        return "server_detail/$serverId"
    }

    fun getChannelDetailRoute(channelId: String): String {
        return "channel_detail/$channelId"
    }

    fun getChatRoute(serverId: String, channelId: String): String {
        return "server/$serverId/channel/$channelId/chat"
    }
}