package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import jakarta.persistence.Entity

@Entity
class OpeningSensor(
                    name: String,
                    room: Room? = null
) : Device(name = name, type = "openingSensor", room = room) {

    var isOpen= false

    fun updateStatus(newStatus: Boolean) {
        isOpen = newStatus
        println("$name ahora está ${if (isOpen) "abierto" else "cerrado"}")
    }

    override fun handleIncomingMessage(message: String) {
        if (message != ""){
            updateStatus(message=="open")
        } else {
            println("Mensaje no válido para $name")
        }
    }
}