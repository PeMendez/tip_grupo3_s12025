package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.ruleEngine.Action

data class ActionDTO(
    val id: Long,
)
fun Action.toDTO(): ActionDTO {
    return ActionDTO(
        id = this.id)
}