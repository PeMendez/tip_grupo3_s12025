package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Room

data class RoomDTO(val id: Long,
                       val name: String,
                        val devices: List<DeviceDTO>)

fun Room.toDTO(): RoomDTO {
    return RoomDTO(
        id = this.id,
        name = this.name,
        devices = this.deviceList.map { it.toDTO() }
    )
}