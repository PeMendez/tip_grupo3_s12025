package ar.unq.ttip.neohub.dto

data class userHomeRequest (
    val username: String = "",
    val action: String,
    val homeName: String? = null,
    val accessKey: String? = null
)