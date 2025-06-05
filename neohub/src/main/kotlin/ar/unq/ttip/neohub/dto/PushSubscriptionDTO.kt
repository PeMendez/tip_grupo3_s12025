package ar.unq.ttip.neohub.dto

data class PushSubscriptionDto(
    val endpoint: String,
    val expirationTime: Long?, // Puede ser null
    val keys: PushSubscriptionKeysDto,
    // Proximamente: Para asociar la suscripción a un usuario desde el frontend
    // val userId: String? = null
)
