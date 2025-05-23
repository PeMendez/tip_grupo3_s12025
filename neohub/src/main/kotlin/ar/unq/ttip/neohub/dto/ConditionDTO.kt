package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Operator
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.Rule

data class ConditionDTO(
    val id: Long,
    val deviceId: Long,
    val deviceName: String,
    val attribute: String,
    val operator: String,
    val value: String
)

fun Condition.toDTO(): ConditionDTO {
    return ConditionDTO(
        id = this.id,
        deviceId = this.device.id,
        deviceName = this.device.name,
        attribute = this.attribute.toString(),
        operator = this.operator.toString(),
        value = this.value
    )
}

fun ConditionDTO.toEntity(rule: Rule, device: Device): Condition {
    return Condition(
        id = this.id,
        rule = rule,
        device = device,
        attribute = Attribute.fromString(this.attribute),
        operator = Operator.fromString(this.operator),
        value = this.value
    )
}