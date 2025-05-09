package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.ruleEngine.Condition

data class ConditionDTO(
    val deviceId: Long,
    val attribute: String,
    val operator: String,
    val value: String
)

fun Condition.toDTO(): ConditionDTO {
    return ConditionDTO(
        deviceId = this.device.id,
        attribute = this.attribute,
        operator = this.operator,
        value = this.value
    )
}