package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class OpeningSensor(
                    name: String,
                    room: Room? = null
) : Device(name = name, type = DeviceType.OPENING_SENSOR, room = room) {

    var isOpen= false

    fun updateStatus(newStatus: String) {
        try {
            isOpen = newStatus.toBoolean()
        }
        catch (e:Exception){

        }
        println("$name ahora está ${if (isOpen) "abierto" else "cerrado"}")
    }

    override fun handleAttributeUpdate(attribute: String, value: String): Boolean {
        return when(attribute.lowercase()) {
            "status" -> {
                updateStatus(value)
                true
            }
            else -> super.handleAttributeUpdate(attribute, value)
        }
    }

    override fun executeAction(actionType: ActionType, parameters: String) {
        throw UnsupportedOperationException("$type no soporta acciones.")
    }

    override fun getAttributeValue(attribute: Attribute): Boolean {
        when (attribute) {
            Attribute.IS_OPEN -> return isOpen
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun setAttributeValue(attribute: Attribute, value: String) {
        when (attribute) {
            Attribute.IS_OPEN -> updateStatus(value)
            else -> throw IllegalArgumentException("Atributo no soportado por ${name}")
        }
    }

    override fun toDTO(): DeviceDTO {
        return super.toDTO().copy(status = isOpen)
    }
}