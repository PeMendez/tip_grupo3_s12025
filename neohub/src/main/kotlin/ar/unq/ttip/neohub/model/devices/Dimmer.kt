package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class Dimmer( name: String,
              room: Room? = null
) : Device(name = name, type = "dimmer", room = room) {

    var brightness: Int = 0

    fun updateBrightness(percentage: Int) {
        brightness = if (percentage<=0) 0
        else if (percentage>=100) 100
        else percentage
    }

    override fun handleIncomingMessage(message: String) {
        val newBrightness = message.toIntOrNull()
        if (newBrightness != null) {
            updateBrightness(newBrightness)
        } else {
            println("Mensaje no válido para $name")
        }
    }

    override fun getAttribute(attribute: String): String {
        throw(UnsupportedOperationException("$type no soporta atributos para consultar."))
    }

    override fun executeAction(actionType: String, parameters: String) {
        println("$name ejecutando accion $actionType")
        handleIncomingMessage(actionType)
    }
}