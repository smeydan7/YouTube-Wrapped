package models

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeActivity(
    val header: String? = null,
    val title: String? = null,
    val titleUrl: String? = null,
    val description: String? = null,
    val time: String? = null,
    val products: List<String>? = null,
    val activityControls: List<String>? = null,
    val subtitles: List<Subtitle>? = null,
    val details: List<Detail>? = null
)

@Serializable
data class Subtitle(
    val name: String,
    val url: String? = null
)

@Serializable
data class Detail(
    val name: String
)

@Serializable
data class Video(
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

@Serializable
data class ThumbnailResponse(
    val thumbnailUrl: String
)
