package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class SmartOutlet(
    name: String,
    room: Room?= null
): Device(name=name, type = "smartOutlet", room = room){
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
        when (message.lowercase()) {
            "turn_on" -> turnOn()
            "turn_off" -> turnOff()
            "toggle" -> toggle()
            else -> println("Mensaje desconocido para SmartOutlet '$name': $message")
        }
    }

    override fun getAttribute(attribute: String): String {
        //Se podría hacer que te de su estado como un atributo, por ahora no.
        throw UnsupportedOperationException("$type no soporta atributos para consultar.")
    }

    override fun executeAction(actionType: String, parameters: String) {
        println("$name ejecutando $actionType...")
        handleIncomingMessage(actionType)
    }
}