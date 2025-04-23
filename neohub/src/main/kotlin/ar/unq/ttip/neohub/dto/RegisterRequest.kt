package ar.unq.ttip.neohub.dto

data class RegisterRequest (
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)