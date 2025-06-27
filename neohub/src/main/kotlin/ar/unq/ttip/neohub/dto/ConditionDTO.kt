package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Operator
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.DeviceCondition
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.model.ruleEngine.TimeCondition

data class ConditionDTO(
    val id: Long,
    val deviceId: Long? = null, //para condiciones que no son de device
    val deviceName: String? = null, //para condiciones que no son de device
    val attribute: String,
    val operator: String,
    val value: String
)

fun Condition.toDTO(): ConditionDTO {
    return when (this) {
        is DeviceCondition -> ConditionDTO(
            id = this.id,
            deviceId = this.device.id,
            deviceName = this.device.name,
            attribute = this.attribute.toString(),
            operator = this.operator.toString(),
            value = this.value
        )
        is TimeCondition -> ConditionDTO(
            id = this.id,
            deviceId = null, // No aplica para TimeCondition
            deviceName = null, // No aplica para TimeCondition
            attribute = Attribute.TIME.toString(), // No aplica para TimeCondition
            operator = this.operator.toString(),
            value = this.value
        )
        else -> throw IllegalArgumentException("Tipo de condición no soportado: ${this::class.simpleName}")
    }
}
fun ConditionDTO.toEntity(rule: Rule, device: Device?): Condition {
    return if (this.deviceId != null && this.deviceName != null) {
        // Es una DeviceCondition
        DeviceCondition(
            rule = rule,
            device = device ?: throw IllegalArgumentException("El dispositivo es requerido para DeviceCondition"),
            attribute = Attribute.fromString(this.attribute),
            operator = Operator.fromString(this.operator),
            value = this.value
        )
    } else {
        // Es una TimeCondition
        TimeCondition(
            rule = rule,
            operator = Operator.fromString(this.operator),
            value = this.value
        )
    }
}