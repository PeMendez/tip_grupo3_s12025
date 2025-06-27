package ar.unq.ttip.neohub.model.ruleEngine

import jakarta.persistence.*

@Entity
@Table(name="rule_condition")
@Inheritance(strategy = InheritanceType.JOINED) // Permite subclases con tablas separadas
abstract class Condition(

    @Id @GeneratedValue val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "rule_id")
    var rule: Rule? = null,

    val type: ConditionType,
) {
    abstract fun evaluate(): Boolean
    abstract fun validate()
}
