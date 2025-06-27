package ar.unq.ttip.neohub.dto

data class ConditionRequest(
    val type: String,
    val deviceId: Long? = null,
    val attribute: String,
    val operator: String,
    val value: String
)
