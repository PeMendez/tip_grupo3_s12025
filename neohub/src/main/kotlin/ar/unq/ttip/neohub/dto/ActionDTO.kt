package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.ruleEngine.Action

data class ActionDTO(
    val deviceId: Long,
    val actionType: String,
    val parameters: String
)

fun Action.toDTO(): ActionDTO {
    return ActionDTO(
        deviceId = this.device.id,
        actionType = this.actionType,
        parameters = this.parameters
    )
}