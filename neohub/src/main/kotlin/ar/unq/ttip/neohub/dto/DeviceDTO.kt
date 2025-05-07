package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.TemperatureSensor

data class DeviceDTO(
    val id: Long,
    val name: String,
    val type: String,
    val topic : String,
    val roomId : Long?,
    val temperature: Double? = null
)

fun Device.toDTO(): DeviceDTO {
    return if (this is TemperatureSensor) {
        DeviceDTO(
            id = this.id,
            name = this.name,
            type = this.type,
            topic = this.topic,
            roomId = this.room?.id,
            temperature = this.temperature
        )
    } else {
        DeviceDTO(
            id = this.id,
            name = this.name,
            type = this.type,
            topic = this.topic,
            roomId = this.room?.id,
            temperature = null
        )
    }
}
fun DeviceDTO.toEntity(factory: DeviceFactory): Device {
    val device = factory.createDevice(name= this.name, type=this.type)
    device.topic= this.topic
    return device
}