package ar.unq.ttip.neohub.model

import jakarta.persistence.*

@Entity
data class Rule(
    @Id @GeneratedValue val id: Long = 0,
    val name: String,

    // Relación con el dispositivo que genera el evento
    @ManyToOne
    @JoinColumn(name = "trigger_device_id")
    val triggerDevice: Device,

    // Relación con el dispositivo que ejecuta la acción
    @ManyToOne
    @JoinColumn(name = "action_device_id")
    val actionDevice: Device,

    // Tipo de evento y acción
    val triggerCondition: String,
    val action: String
)
