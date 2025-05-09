package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.ruleEngine.Rule

data class RuleDTO(
    val id: Long,
    val name: String,
    val conditions: List<ConditionDTO>,
    val actions: List<ActionDTO>
)

fun Rule.toDTO(): RuleDTO {
    return RuleDTO(
        id,
        name,
        conditions.map { it.toDTO() },
        actions.map { it.toDTO() }
    )
}

