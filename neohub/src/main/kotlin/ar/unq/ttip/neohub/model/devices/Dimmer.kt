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

    private fun updateBrightness(percentage: String) { // Lo hacemos privado o protegido, llamado desde updateAttribute
        try {
            val percentage = percentage.toInt()
            this.brightness = when {
                percentage <= 0 -> 0
                percentage >= 100 -> 100
                else -> percentage
            }
            println("INFO: Brillo del Dimmer '${this.name}' actualizado a: ${this.brightness}%")
        } catch (e: NumberFormatException) {
            println("ERROR: Valor de brillo '$percentage' no es un número entero válido para Dimmer '${this.name}'.")
        }
    }

    override fun handleAttributeUpdate(attribute: String, value: String): Boolean {
        return when (attribute.lowercase()) { // Usar lowercase para ser flexible con las claves
            "brightness" -> {
                updateBrightness(value)
                true // Atributo reconocido y procesado
            }
            // Aquí podrían manejarse otros atributos específicos del dimmer, ej: "color_temp"
            else -> super.handleAttributeUpdate(attribute, value) // Llamar al de la clase base si no se reconoce aquí
        }
    }

    override fun executeAction(actionType: ActionType, parameters: String) {
        println("$name ejecutando acción $actionType con parámetros $parameters")

        when (actionType) {
            ActionType.SET_BRIGHTNESS -> {
                // Enviar el mensaje de actualización al dimmer
                updateBrightness(parameters)
            }
            else -> {
                throw UnsupportedOperationException("El Dimmer no soporta la acción: $actionType")
            }
        }
    }

    override fun getAttributeValue(attribute: Attribute): Any {
        when (attribute){
            Attribute.BRIGHTNESS -> {return brightness}
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun setAttributeValue(attribute: Attribute, value: String) {
        when (attribute){
            Attribute.BRIGHTNESS -> {updateBrightness(value)}
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(brightness = brightness)
    }
}