package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import org.springframework.stereotype.Component

@Component
class DeviceFactory {
    fun createDevice(name: String, type: String, room: Room? =null): Device {
        println("Creating $type: $name")
        return when (type) {
            "smartOutlet" -> SmartOutlet(name=name, room=room)
            "temperatureSensor" -> TemperatureSensor(name=name, room=room)
            "openingSensor" -> OpeningSensor(name=name, room=room)
            "dimmer" -> Dimmer(name=name, room=room)
            // ... otros tipos...
            else -> throw IllegalArgumentException("Tipo de dispositivo desconocido: $type")
        }
    }
}
