package ar.unq.ttip.neohub.dto

data class UserHomeDTO(
    val user : UserDTO,
    val homeDTO: HomeDTO,
    val role: String,
)

