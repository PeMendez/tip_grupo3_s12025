package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.ruleEngine.Condition

data class ConditionDTO(
    val id: Long
)

fun Condition.toDTO(): ConditionDTO {
    return ConditionDTO(
        id = this.id
    )
}