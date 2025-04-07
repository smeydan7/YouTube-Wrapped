package dataClasses

data class userStats(
    val name: String,
    val topCreators: List<String>,
    val topVideos: List<String>,
    val totalVideos: Int
)
