package com.example.harmony.domain.model

data class ServerWithChannels (
    val server: Server,
    val channels: List<Channel>
)