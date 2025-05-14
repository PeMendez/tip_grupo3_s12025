package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.*

data class DeviceDTO(
    val id: Long,
    val name: String,
    val type: String,
    val topic : String,
    val roomId : Long?,
    val temperature: Double? = null,
    val status: Boolean? = null,
    val brightness: Int? = null
)

fun DeviceDTO.toEntity(factory: DeviceFactory): Device {
    val device = factory.createDevice(name= this.name, type= DeviceType.fromString(type))
    device.topic= this.topic
    return device
}