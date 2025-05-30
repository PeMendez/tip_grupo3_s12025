package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class TemperatureSensor(
    name: String,
    room: Room? = null
) : Device(name = name, type = DeviceType.TEMPERATURE_SENSOR, room = room) {

    var temperature: Double = 0.0

    fun updateTemperature(newTemperature: String) {
        try {
            this.temperature = newTemperature.toDouble()
            println("INFO: Temperatura del Sensor '${this.name}' actualizada a: ${this.temperature}")
        } catch (e: NumberFormatException) {
            println("ERROR: Valor de temperatura '$newTemperature' no es un número válido para Sensor '${this.name}'.")
        }
    }

    override fun handleAttributeUpdate(attribute: String, value: String): Boolean {
        return when(attribute.lowercase()) {
            "temperature" -> {
                updateTemperature(value)
                true
            }
            else -> super.handleAttributeUpdate(attribute, value)
        }
    }

    override fun executeAction(actionType: ActionType, parameters: String) {
        throw UnsupportedOperationException("$type no soporta acciones.")
    }

    override fun getAttributeValue(attribute: Attribute): Any {
        when (attribute){
            Attribute.TEMPERATURE -> {return temperature}
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun setAttributeValue(attribute: Attribute, value: String) {
        when (attribute){
            Attribute.TEMPERATURE -> updateTemperature(value)
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(temperature = temperature)
    }
}
