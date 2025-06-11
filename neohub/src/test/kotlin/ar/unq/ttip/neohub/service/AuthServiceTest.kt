package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Role
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.UserHome
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.UserHomeRepository
import ar.unq.ttip.neohub.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val homeRepository: HomeRepository,
    private val userHomeRepository: UserHomeRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @MockitoBean
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var authService: AuthService

    @Test
    fun `register should create a user and home`() {
        // Mock del token
        `when`(jwtService.generateToken(anyString())).thenReturn("mocked-token")

        // Preparar datos de entrada
        val request = RegisterRequest(
            "testuser",
            "password",
            "password",
            "CREATE",
            "my home",
            "key123"
        )

        // Ejecutar el mét_odo
        val response = authService.register(request)

        // Verificar resultados
        assertNotNull(response.token)
        assertEquals("mocked-token", response.token)

        val user = userRepository.findByUsername("testuser")
        assertNotNull(user)
        assertTrue(passwordEncoder.matches("password", user!!.password))
    }

    @Test @Transactional
    fun `register should create a user and assign them as admin in the new home`() {
        // Mock del token
        `when`(jwtService.generateToken(anyString())).thenReturn("mocked-token")

        // Preparar datos de entrada
        val request = RegisterRequest(
            "testuser5",
            "password",
            "password",
            "CREATE",
            "New Home",
            "key123"
        )

        // Ejecutar el método
        val response = authService.register(request)

        // Verificar resultados
        assertNotNull(response.token)
        assertEquals("mocked-token", response.token)

        val user = userRepository.findByUsername("testuser5")
        assertNotNull(user)
        assertTrue(passwordEncoder.matches("password", user!!.password))

        val home = homeRepository.findByName("New Home")
        assertNotNull(home)

        // Verificar que el creador es administrador
        val admins = home!!.getAdmins()
        assertEquals(1, admins.size)
        assertTrue(admins.contains(user))
    }

    @Test @Transactional
    fun `register should allow a user to join an existing home and assign the role USER`() {
        // Crear una home existente
        val owner = userRepository.save(User(
            username = "existingAdmin",
            password = passwordEncoder.encode("password")))
        val home = homeRepository.save(Home(name = "Existing Home", accessKey = "homekey123"))
        home.addUserHome(UserHome(user = owner, role = Role.ADMIN))

        // Mock del token
        `when`(jwtService.generateToken(anyString())).thenReturn("mocked-token")

        // Preparar datos de entrada
        val request = RegisterRequest(
            "testuser6",
            "password",
            "password",
            "JOIN",
            "Existing Home",
            "homekey123"
        )

        // Ejecutar el método
        val response = authService.register(request)

        // Verificar resultados
        assertNotNull(response.token)
        assertEquals("mocked-token", response.token)

        val user = userRepository.findByUsername("testuser6")
        assertNotNull(user)
        assertTrue(passwordEncoder.matches("password", user!!.password))

        val joinedHome = homeRepository.findByName("Existing Home")
        assertNotNull(joinedHome)

        // Verificar que el nuevo usuario está en la lista de usuarios y no es admin
        val users = joinedHome!!.getUsers()
        assertTrue(users.contains(user))

        val admins = joinedHome.getAdmins()
        assertEquals(1, admins.size)
        assertTrue(admins.contains(owner))
    }

    @Test @Transactional
    fun `register should fail to join an existing home with incorrect secretKey`() {
        // Crear una home existente
        val owner = userRepository.save(User(username = "existingAdmin", password = passwordEncoder.encode("password")))
        homeRepository.save(Home(name = "Existing Home", accessKey = "homekey123").apply {
            addUserHome(UserHome(user = owner, role = Role.ADMIN))
        })

        // Preparar datos de entrada
        val request = RegisterRequest(
            "testuser7",
            "password",
            "password",
            "JOIN",
            "Existing Home",
            "wrongkey"
        )

        // Verificar que lanza excepción
        val exception = assertThrows<IllegalArgumentException> {
            authService.register(request)
        }
        assertEquals("Clave de acceso incorrecta", exception.message)

        // Verificar que el usuario no fue agregado a la home
        val user = userRepository.findByUsername("testuser7")
        val userHomes = userHomeRepository.findByUser(user!!)
        assertTrue(userHomes.isEmpty())
    }


}
