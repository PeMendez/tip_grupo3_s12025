package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Home

data class HomeDTO(
    val id: Long,
    val name: String,
    val key: String,
    val rooms: List<RoomDTO>
)
fun Home.toDTO(): HomeDTO {
    return HomeDTO(
        id = this.id,
        name = this.name,
        key = this.accessKey,
        rooms = this.rooms.map { it.toDTO() }
    )
}
