package com.example.harmony.domain.model

enum class ChannelType { TEXT, VOICE }

data class Channel (
    val id: String = "",
    val description: String = "",
    val name: String = "",
    val type: ChannelType = ChannelType.TEXT
)