package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceFactory

data class DeviceDTO(
    val id: Long,
    val name: String,
    val type: String,
    val topic : String,
    val roomId : Long?
)

fun Device.toDTO(): DeviceDTO {
    return DeviceDTO(
        id = this.id,
        name = this.name,
        type = this.type,
        topic = this.topic,
        roomId = this.room?.id
    )
}
fun DeviceDTO.toEntity(factory: DeviceFactory): Device {
    val device = factory.createDevice(name= this.name, type=this.type)
    device.topic= this.topic
    return device
}