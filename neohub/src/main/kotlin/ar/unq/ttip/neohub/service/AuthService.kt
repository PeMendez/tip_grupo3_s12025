package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.LoginRequest
import ar.unq.ttip.neohub.dto.LoginResponse
import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val homeRepository: HomeRepository
) {

    fun login(request: LoginRequest): LoginResponse {
        val authToken = UsernamePasswordAuthenticationToken(request.username, request.password)
        authenticationManager.authenticate(authToken)
        val token = jwtService.generateToken(request.username)
        return LoginResponse(token)
    }

    fun register(request: RegisterRequest): LoginResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("El usuario ya existe")
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val newUser = User(username = request.username, password = encodedPassword)
        userRepository.save(newUser)

        val newHome = Home(user = newUser)
        homeRepository.save(newHome)

        val token = jwtService.generateToken(newUser.username)
        return LoginResponse(token)
    }

}