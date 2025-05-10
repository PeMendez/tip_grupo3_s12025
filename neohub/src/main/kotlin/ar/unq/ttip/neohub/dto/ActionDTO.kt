package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.Rule

data class ActionDTO(
    val id: Long,
    val deviceId: Long,
    val actionType: String,
    val parameters: String
)

fun Action.toDTO(): ActionDTO {
    return ActionDTO(
        id = this.id,
        deviceId = this.device.id,
        actionType = this.actionType,
        parameters = this.parameters
    )
}
fun ActionDTO.toEntity(rule: Rule, device: Device): Action {
    return Action(
        id = this.id,
        rule = rule,
        device = device,
        actionType = this.actionType,
        parameters = this.parameters
    )
}
