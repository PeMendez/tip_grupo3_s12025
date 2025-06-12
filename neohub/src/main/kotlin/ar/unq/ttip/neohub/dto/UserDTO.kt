package ar.unq.ttip.neohub.dto
import ar.unq.ttip.neohub.model.User

data class UserDTO(
    val id: Long,
    val username: String,
    val homes: List<HomeDTO>
)

fun toDTO(user: User): UserDTO{
    return UserDTO(
        id = user.id,
        username = user.username,
        homes = user.userHomes.map{ HomeDTO(
            it.home!!.id,
            it.home!!.name,
            it.home!!.rooms.map { room -> room.toDTO() },
        )}
    )
}
