package ar.unq.ttip.neohub.dto

data class PushSubscriptionKeysDto(
    val p256dh: String,
    val auth: String
)