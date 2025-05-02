package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.LoginRequest
import ar.unq.ttip.neohub.dto.LoginResponse
import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.service.AuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse {
        return authService.login(request)
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): LoginResponse {
        return authService.register(request)
    }
}
