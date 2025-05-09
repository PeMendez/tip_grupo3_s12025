package ar.unq.ttip.neohub.model.ruleEngine

class RuleEngine {
    fun evaluate(rule: Rule): Boolean {
        val failedConditions = rule.conditions.filterNot { it.evaluate() }
        val conditionsMet = failedConditions.isEmpty()

        if(conditionsMet) { rule.actions.forEach{it.execute()}}
        return conditionsMet
    }
}
