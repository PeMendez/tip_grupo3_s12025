package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import org.springframework.stereotype.Component

@Component
class DeviceFactory {

    fun createDevice(name: String, type: String): Device {
        println("Creating $type: $name")
        return when (type) {
            "smartOutlet" -> SmartOutlet(name)
            "temperatureSensor" -> TemperatureSensor(name)
            // ... otros tipos...
            else -> throw IllegalArgumentException("Tipo de dispositivo desconocido: $type")
        }
    }
}
