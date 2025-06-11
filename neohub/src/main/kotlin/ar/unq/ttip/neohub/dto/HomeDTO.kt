package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.Home

data class HomeDTO(
    val id: Long,
    val rooms: List<RoomDTO>
)
fun Home.toDTO(): HomeDTO {
    return HomeDTO(
        id = this.id,
        rooms = this.rooms.map { it.toDTO() }
    )
}
