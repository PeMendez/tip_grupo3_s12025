package ar.unq.ttip.neohub.dto

data class CreateRuleRequest(
    val name: String,
    val conditions: List<ConditionRequest>,
    val actions: List<ActionRequest>
)