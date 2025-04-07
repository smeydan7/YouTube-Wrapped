package models

import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
    val title: String,
    val url: String,
    val count: Int
)

@Serializable
data class Channel(
    val name: String,
    val url: String,
    val count: Int
)
