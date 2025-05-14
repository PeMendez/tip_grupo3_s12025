package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Atributo
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class Dimmer( name: String,
              room: Room? = null
) : Device(name = name, type = DeviceType.DIMMER, room = room) {

    var brightness: Int = 0

    fun updateBrightness(percentage: Int) {
        brightness = if (percentage<=0) 0
        else if (percentage>=100) 100
        else percentage
    }

    override fun handleIncomingMessage(message: String) {
        /*val newBrightness = message.toIntOrNull()
        if (newBrightness != null) {
            updateBrightness(newBrightness)
        } else {
            println("Mensaje no válido para $name")
        }*/
        val newBrightness = message.isBlank()
        if (!newBrightness) {
            setValorAtributo(message)
        } else {
            println("Mensaje no válido para $name")
        }
    }

    override fun executeAction(actionType: String, parameters: String) {
        println("$name ejecutando accion $actionType")
        handleIncomingMessage(actionType)
    }

    override fun getAtributo(): Atributo {
        return Atributo.INTENSIDAD
    }

    override fun getValorAtributo(atributo: Atributo): Any {
        return brightness
    }

    override fun setValorAtributo(valor: String) {
        val percentage = valor.toInt()
        brightness = if (percentage<=0) 0
        else if (percentage>=100) 100
        else percentage
    }


    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(brightness = brightness)
    }
}