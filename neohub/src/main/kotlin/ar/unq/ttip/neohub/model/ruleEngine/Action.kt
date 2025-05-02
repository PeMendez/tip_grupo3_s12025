package ar.unq.ttip.neohub.model.ruleEngine

import ar.unq.ttip.neohub.model.Device
import jakarta.persistence.*

@Entity
data class Action(
    @Id @GeneratedValue val id: Long = 0,
    @ManyToOne @JoinColumn(name = "rule_id")
    var rule: Rule? = null,

    @ManyToOne @JoinColumn(name = "device_id")
    val device: Device,

    val actionType: String,  // Ejemplo: "turn_on", "set_state"
    val parameters: String   // Parámetros opcionales para la acción
)
