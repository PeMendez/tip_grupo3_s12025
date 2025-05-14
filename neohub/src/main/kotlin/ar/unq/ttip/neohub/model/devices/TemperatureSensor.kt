package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Atributo
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class TemperatureSensor(
    name: String,
    room: Room? = null
) : Device(name = name, type = DeviceType.TEMPERATURE_SENSOR, room = room) {

    var temperature: Double = 0.0

    fun updateTemperature(newTemperature: Double) {
        temperature = newTemperature
        println("$name temperature updated to $temperature°C")
        //En realidad la temperatura no se pasaría por parámetro sino por MQTT
    }

    override fun handleIncomingMessage(message: String) {
        /*val temp = message.toDoubleOrNull()
        if (temp != null) {
            updateTemperature(temp)
        } else {
            println("Mensaje inválido para TemperatureSensor '$name': $message")
        }*/

        val temp = message.isBlank()
        if (!temp) {
            setValorAtributo(message)
        } else {
            println("Mensaje inválido para TemperatureSensor '$name': $message")
        }
    }



    override fun executeAction(actionType: String, parameters: String) {
        throw UnsupportedOperationException("$type no soporta acciones.")
    }

    override fun getAtributo(): Atributo {
        return Atributo.TEMPERATURA
    }

    override fun getValorAtributo(atributo: Atributo): Any {
        return temperature
    }

    override fun setValorAtributo(valor: String) {
        temperature = valor.toDouble()
        println("$name temperature updated to $temperature°C")
    }


    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(temperature = temperature)
    }
}
