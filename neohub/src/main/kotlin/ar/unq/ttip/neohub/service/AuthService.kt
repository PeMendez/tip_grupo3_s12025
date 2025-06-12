package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.LoginRequest
import ar.unq.ttip.neohub.dto.LoginResponse
import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Role
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.UserHome
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.UserHomeRepository
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
    private val homeRepository: HomeRepository,
    private val userHomeRepository: UserHomeRepository
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

        when (request.action) {
            "CREATE" -> {
                // Crear una nueva home y asignar al usuario como admin
                val newHome = Home(
                    name = request.homeName ?: throw IllegalArgumentException("El nombre de la home es requerido"),
                    accessKey = request.accessKey ?: throw IllegalArgumentException("La clave de acceso es requerida")
                )

                val userHome = UserHome(user = newUser, home = newHome, role = Role.ADMIN)
                newHome.addUserHome(userHome)
                homeRepository.save(newHome)
                userHomeRepository.save(userHome)
            }

            "JOIN" -> {
                // Unirse a una home existente
                val home = homeRepository.findByName(request.homeName!!)
                    ?: throw IllegalArgumentException("La home no existe")
                if (home.accessKey != request.accessKey) {
                    throw IllegalArgumentException("Clave de acceso incorrecta")
                }

                val userHome = UserHome(user = newUser, home = home, role = Role.USER)
                home.addUserHome(userHome)
                homeRepository.save(home)
                userHomeRepository.save(userHome)
            }

            else -> throw IllegalArgumentException("Acción no válida")
        }
        val token = jwtService.generateToken(newUser.username)
        return LoginResponse(token)
    }
}