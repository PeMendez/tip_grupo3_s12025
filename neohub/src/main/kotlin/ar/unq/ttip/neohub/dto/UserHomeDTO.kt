package ar.unq.ttip.neohub.dto

data class UserHomeDTO(
    val userId :Long,
    val homeDTO: HomeDTO,
    val role: String,
)

