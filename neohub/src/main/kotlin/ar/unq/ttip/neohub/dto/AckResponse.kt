package ar.unq.ttip.neohub.dto

data class AckResponse(
    val success: Boolean,
    val data: Map<Long, Boolean>? = null,
    val error: String? = null
)
