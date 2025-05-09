package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Room

data class RoomDTO(
    val id: Long,
    val name: String,
    val homeId: Long?,
    val deviceList: List<DeviceDTO>
)

fun Room.toDTO(): RoomDTO {
    return RoomDTO(
        id = this.id,
        name = this.name,
        homeId = this.home?.id,
        deviceList = this.deviceList.map { it.toDTO() }
    )
}

fun RoomDTO.toEntity(): Room {
    return Room(
        id = this.id,
        name = this.name,
        home = null, // Como asigno la home? home.addRoom
        deviceList = mutableListOf() // Inicializar con lista vacía
    )
}