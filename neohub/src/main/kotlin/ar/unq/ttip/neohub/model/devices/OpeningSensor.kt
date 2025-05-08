package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room

class OpeningSensor(
                    name: String,
                    room: Room? = null
) : Device(name = name, type = "openingSensor", room = room) {

    var isOpen= false

    fun updateStatus(newStatus: Boolean) {
        isOpen = newStatus
        val status  = if (isOpen) "abierto" else "cerrado"
        println("$name ahora está $status")
    }

    override fun handleIncomingMessage(message: String) {
        val status = message.toBooleanStrictOrNull()
        if (status != null){
            updateStatus(status)
        } else {
            println("Mensaje no válido para $name")
        }
    }
}