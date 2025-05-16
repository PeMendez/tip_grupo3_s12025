package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
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
        val isValid = message.isBlank() || message.toIntOrNull() == null
        if (!isValid) {
            setAttributeValue(message)
        } else {
            println("Mensaje no válido para $name")
        }
    }

    override fun executeAction(actionType: ActionType, parameters: String) {
        println("$name ejecutando acción $actionType con parámetros $parameters")

        when (actionType) {
            ActionType.SET_BRIGHTNESS -> {
                // Valida que el parámetro sea un valor numérico válido antes de procesarlo
                val brightnessValue = parameters.toIntOrNull()
                    ?: throw IllegalArgumentException("Parámetro inválido para SET_BRIGHTNESS: $parameters")

                // Enviar el mensaje de actualización al dimmer
                handleIncomingMessage(brightnessValue.toString())
            }
            else -> {
                println("Acción no soportada: $actionType")
                throw UnsupportedOperationException("El Dimmer no soporta la acción: $actionType")
            }
        }
    }

    override fun getAttributeValue(attribute: Attribute): Any {
        return brightness
    }

    override fun setAttributeValue(valor: String) {
        val percentage = valor.toInt()
        brightness = if (percentage<=0) 0
        else if (percentage>=100) 100
        else percentage
    }

    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(brightness = brightness)
    }
}