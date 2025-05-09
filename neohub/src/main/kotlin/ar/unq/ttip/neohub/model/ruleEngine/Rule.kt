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
    override fun toString(): String {
        return name
    }
}
