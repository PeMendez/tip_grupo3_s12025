package ar.unq.ttip.neohub.dto

data class RegisterRequest (
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)