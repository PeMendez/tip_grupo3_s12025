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

fun Device.toDTO(): DeviceDTO {
    when (this){
        is TemperatureSensor -> return DeviceDTO(
            id = this.id,
            name = this.name,
            type = this.type,
            topic = this.topic,
            roomId = this.room?.id,
            temperature = this.temperature
            )
        is OpeningSensor -> return DeviceDTO(
            id = this.id,
            name = this.name,
            type = this.type,
            topic = this.topic,
            roomId = this.room?.id,
            temperature = null,
            status = this.isOpen
        )
        is SmartOutlet -> return DeviceDTO(
            id = this.id,
            name = this.name,
            type = this.type,
            topic = this.topic,
            roomId = this.room?.id,
            temperature = null,
            status = this.isOn
        )
        is Dimmer -> return DeviceDTO(
            id = this.id,
            name = this.name,
            type = this.type,
            topic = this.topic,
            roomId = this.room?.id,
            temperature = null,
            brightness = this.brightness
        )
        else -> {
            println("Unknown device type ${this.type}")
            return DeviceDTO(
                id = this.id,
                name = this.name,
                type = this.type,
                topic = this.topic,
                roomId = this.room?.id
            )
        }
    }
}
fun DeviceDTO.toEntity(factory: DeviceFactory): Device {
    val device = factory.createDevice(name= this.name, type=this.type)
    device.topic= this.topic
    return device
}