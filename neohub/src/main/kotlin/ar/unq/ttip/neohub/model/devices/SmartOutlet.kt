package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Atributo
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
        //Primero debería enviar mensaje ON por MQTT
        isOn = true
        println("$name turned on")
    }

    fun turnOff(){
        //Primero debería enviar mensaje OFF por MQTT
        isOn = false
        println("$name turned off")
    }

    fun toggle(){
        //Primero debería enviar mensaje TOGGLE por MQTT
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
        setValorAtributo(message)
    }

    override fun executeAction(actionType: String, parameters: String) {
        println("$name ejecutando $actionType...")
        handleIncomingMessage(actionType)
    }


    override fun getAtributo(): Atributo {
        return Atributo.IS_ON
    }

    override fun getValorAtributo(atributo: Atributo): Any {
        return isOn
    }

    override fun setValorAtributo(valor: String) {
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