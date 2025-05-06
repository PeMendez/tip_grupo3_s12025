package ar.unq.ttip.neohub.model.ruleEngine

import ar.unq.ttip.neohub.model.Device
import jakarta.persistence.*

@Entity
@Table(name="rule_condition")
data class Condition(
    @Id @GeneratedValue val id: Long = 0,
    @ManyToOne @JoinColumn(name = "rule_id")
    var rule: Rule? = null,

    @ManyToOne @JoinColumn(name = "device_id")
    val device: Device,

    val attribute: String,  // Ejemplo: "temperature"
    val operator: String,   // Ejemplo: ">"
    val value: String,       // Ejemplo: "25"

)
