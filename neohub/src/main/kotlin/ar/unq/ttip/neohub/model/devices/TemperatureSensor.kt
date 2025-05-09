package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class TemperatureSensor(
    name: String,
    room: Room? = null
) : Device(name = name, type = "temperatureSensor", room = room) {

    var temperature: Double = 0.0

    fun updateTemperature(newTemperature: Double) {
        temperature = newTemperature
        println("$name temperature updated to $temperature°C")
        //En realidad la temperatura no se pasaría por parámetro sino por MQTT
    }

    override fun handleIncomingMessage(message: String) {
        val temp = message.toDoubleOrNull()
        if (temp != null) {
            updateTemperature(temp)
        } else {
            println("Mensaje inválido para TemperatureSensor '$name': $message")
        }
    }

    override fun getAttribute(attribute: String): String {
        return when (attribute) {
            "temperature" -> temperature.toString()
            else -> throw IllegalArgumentException("Atributo no soportado para $type: '$attribute'")
        }
    }

    override fun executeAction(actionType: String, parameters: String) {
        throw UnsupportedOperationException("$type no soporta acciones.")
    }
}
