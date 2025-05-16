package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class SmartOutlet(
    name: String,
    room: Room?= null
): Device(name=name, type = DeviceType.SMART_OUTLET, room = room){
    var isOn: Boolean = false

    fun turnOn(){
        isOn = true
        println("$name turned on")
    }

    fun turnOff(){
        isOn = false
        println("$name turned off")
    }

    fun toggle(){
        isOn = !isOn
        println("$name toggled to ${if (isOn) "ON" else "OFF"}")
    }

    override fun handleIncomingMessage(message: String) {
        /*when (message.lowercase()) {
            "turn_on" -> turnOn()
            "turn_off" -> turnOff()
            "toggle" -> toggle()
            else -> println("Mensaje desconocido para SmartOutlet '$name': $message")
        }*/
        setAttributeValue(message)
    }

    override fun executeAction(actionType: ActionType, parameters: String) {
        println("$name ejecutando $actionType...")
        handleIncomingMessage(actionType.toString())
    }


    override fun getAttributeValue(attribute: Attribute): Any {
        return isOn
    }

    override fun setAttributeValue(valor: String) {
        when (valor) {
            "turn_on" -> turnOn()
            "turn_off" -> turnOff()
            "toggle" -> toggle()
            else -> println("Mensaje desconocido para SmartOutlet '$name': $valor")
        }
    }


    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(status = isOn)
    }
}