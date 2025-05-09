package ar.unq.ttip.neohub.model.ruleEngine

import jakarta.persistence.*

@Entity
data class Rule(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,

    // Condiciones asociadas a la regla
    @OneToMany(mappedBy = "rule", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val conditions: List<Condition> = mutableListOf(),

    // Acciones asociadas a la regla
    @OneToMany(mappedBy = "rule", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val actions: List<Action> = mutableListOf()
){
    fun evaluate(): Boolean {
        val failedConditions = conditions.filterNot { it.evaluate() }
        val conditionsMet = failedConditions.isEmpty() // para que en un futuro pueda depurarse a ver lo que falló.
        if(conditionsMet) { actions.forEach{it.execute()}}
        return conditionsMet
    }

    override fun toString(): String {
        return name
    }
}
