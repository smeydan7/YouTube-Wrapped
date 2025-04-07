package models

import kotlinx.serialization.Serializable

@Serializable
data class ChannelInfo(
    val name: String,
    val url: String,
    val count: Int
)