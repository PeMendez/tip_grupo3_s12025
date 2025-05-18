package ar.unq.ttip.neohub.model.ruleEngine

import ar.unq.ttip.neohub.model.Device
import jakarta.persistence.*

@Entity
data class Rule(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,

    // Condiciones asociadas a la regla
    @OneToMany(mappedBy = "rule", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val conditions: MutableList<Condition> = mutableListOf(),

    // Acciones asociadas a la regla
    @OneToMany(mappedBy = "rule", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val actions: MutableList<Action> = mutableListOf()
) {
    /*fun evaluateAndExecute(): Boolean {
        validateConditions() //Esto tiene que arrojar una excepcion si falla.
        val failedConditions = conditions.filterNot { it.evaluate() }
        val conditionsMet = failedConditions.isEmpty() // para que en un futuro pueda depurarse a ver lo que falló.
        if(conditionsMet) { actions.forEach{it.execute()}}
        return conditionsMet
    }*/
    fun evaluateAndExecute(): List<Device> {
        validateConditions()
        val failedConditions = conditions.filterNot { it.evaluate() }

        if (failedConditions.isEmpty()) {
            return actions.mapNotNull { it.execute() } // Devuelve dispositivos modificados
        }
        return emptyList()
    }

    fun validateConditions(){
        conditions.forEach { it.validate() }
    }

    override fun toString(): String {
        return name
    }

    fun validateActions() {
        actions.forEach { it.validate() }
    }
}
