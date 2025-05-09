package ar.unq.ttip.neohub.dto

data class ConditionRequest(
    val deviceId: Long,
    val attribute: String,
    val operator: String,
    val value: String
)
