package ar.unq.ttip.neohub.dto

data class ActionRequest(
    val deviceId: Long,
    val actionType: String,
    val parameters: String
)