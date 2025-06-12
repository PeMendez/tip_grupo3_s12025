package ar.unq.ttip.neohub.integration

import ar.unq.ttip.neohub.dto.DeviceData
import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.service.AuthService
import ar.unq.ttip.neohub.service.DeviceService
import ar.unq.ttip.neohub.service.HomeService
import ar.unq.ttip.neohub.service.RoomService
import ar.unq.ttip.neohub.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
class SharedHomesTest (
    @Autowired private val homeService: HomeService,
    @Autowired private val roomService: RoomService,
    @Autowired private val deviceService: DeviceService,
    @Autowired private val userService: UserService,
    @Autowired private val authService: AuthService,
){
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `test getDeviceById with proper access`() {
        // Registrar admin y crear una home
        val adminRequest = RegisterRequest(
            username = "adminUser",
            password = "password",
            confirmPassword = "password",
            action = "CREATE",
            homeName = "Admin Home",
            accessKey = "adminKey"
        )
        authService.register(adminRequest)
        val admin = userService.getUserByUsername("adminUser")
        val home = homeService.getAdminHomeForUser(admin.id)

        // Registrar user y unirse a la home del admin
        val userRequest = RegisterRequest(
            username = "normalUser",
            password = "password",
            confirmPassword = "password",
            action = "JOIN",
            homeName = "Admin Home",
            accessKey = "adminKey"
        )
        authService.register(userRequest)
        val user = userService.getUserByUsername("normalUser")

        val room = roomService.addNewRoom(home.id, Room(home = home, name = "LivingRoom").toDTO())

        // Admin conecta un dispositivo
        val adminDeviceData = DeviceData(
            name = "Admin Device",
            type = "smart_outlet",
            mac_address = "ABC123"
        )
        val adminDeviceMessage=objectMapper.writeValueAsString(adminDeviceData)
        val adminDevice = deviceService.handleNewDevice(adminDeviceMessage)
        //admin debe agregarlo al room
        roomService.addDeviceToRoom(room.id,adminDevice.id)

        // User conecta un dispositivo
        val userDeviceData = DeviceData(
            name = "User Device",
            type = "temperature_sensor",
            mac_address = "DEF456"
        )
        val userDeviceMessage=objectMapper.writeValueAsString(userDeviceData)
        val userDevice=deviceService.handleNewDevice(userDeviceMessage)
        //user debe agregarlo al room
        roomService.addDeviceToRoom(room.id,userDevice.id)

        // Verificar que el user puede ver su propio dispositivo
        val fetchedUserDevice = deviceService.getDeviceById(userDevice.id)
        assertEquals(userDevice.name, fetchedUserDevice.name)

        // Verificar que el admin puede ver el dispositivo del user
        val fetchedDeviceByAdmin = deviceService.getDeviceById(userDevice.id)
        assertEquals(userDevice.name, fetchedDeviceByAdmin.name)

        // Verificar que el user no puede ver el dispositivo del admin
        val exception = assertThrows<IllegalArgumentException> {
            deviceService.getDeviceById(adminDevice.id)
        }
        assertEquals("Access denied to device with ID ${adminDevice.id}.", exception.message)

        // Verificar que el admin puede ver su propio dispositivo
        val fetchedAdminDevice = deviceService.getDeviceById(adminDevice.id)
        assertEquals(adminDevice.name, fetchedAdminDevice.name)
    }
}