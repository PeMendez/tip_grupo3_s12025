package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Device

data class DeviceDTO(
    val id: Long,
    val name: String,
    val type: String
)

fun Device.toDTO(): DeviceDTO {
    return DeviceDTO(
        id = this.id,
        name = this.name,
        type = this.type
    )
}