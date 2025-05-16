package ar.unq.ttip.neohub.model.ruleEngine

import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceType
import jakarta.persistence.*

@Entity
data class Action(
    @Id @GeneratedValue val id: Long = 0,
    @ManyToOne @JoinColumn(name = "rule_id")
    var rule: Rule? = null,

    @ManyToOne @JoinColumn(name = "device_id")
    val device: Device,

    val actionType: ActionType,
    val parameters: String   // Parámetros opcionales para la acción
){
    fun execute(){
        device.executeAction(actionType, parameters)
    }

    fun validate() {
        val supportedActionTypes = DeviceType.getSupportedActions(device.type)
        if (!supportedActionTypes.contains(actionType)) {
            throw IllegalArgumentException("Acción $actionType no soportada por dispositivos del tipo ${device.type}")
        }
    }
}