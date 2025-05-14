package ar.unq.ttip.neohub.model.devices

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import org.springframework.stereotype.Component

@Component
class DeviceFactory {
    fun createDevice(name: String, type: DeviceType, room: Room? = null): Device {
        println("Creating ${type.name.lowercase()}: $name")
        return when (type) {
            DeviceType.SMART_OUTLET -> SmartOutlet(name = name, room = room)
            DeviceType.TEMPERATURE_SENSOR -> TemperatureSensor(name = name, room = room)
            DeviceType.OPENING_SENSOR -> OpeningSensor(name = name, room = room)
            DeviceType.DIMMER -> Dimmer(name = name, room = room)
        }
    }
}

