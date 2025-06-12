package ar.unq.ttip.neohub.integration

import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import ar.unq.ttip.neohub.service.AuthService
import ar.unq.ttip.neohub.service.DeviceService
import ar.unq.ttip.neohub.service.HomeService
import ar.unq.ttip.neohub.service.RoomService
import ar.unq.ttip.neohub.service.UserService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SharedHomesTest (
    @Autowired private val homeService: HomeService,
    @Autowired private val roomService: RoomService,
    @Autowired private val deviceService: DeviceService,
    @Autowired private val userService: UserService,
    @Autowired private val authService: AuthService,
){
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

        // Admin agrega un dispositivo
        val adminDevice = SmartOutlet(
            name = "Lamp",
            room = room.toEntity()
        )
        deviceService.saveDevice(adminDevice)

        // User agrega un dispositivo
        val userDevice = TemperatureSensor(
            name = "User Device",
            room = room.toEntity(),
        )
        deviceService.saveDevice(userDevice)

        // Verificar que el user puede ver su propio dispositivo
        val fetchedUserDevice = deviceService.getDeviceById(userDevice.id)
        Assertions.assertEquals(userDevice.name, fetchedUserDevice.name)

        // Verificar que el admin puede ver el dispositivo del user
        val fetchedDeviceByAdmin = deviceService.getDeviceById(userDevice.id)
        Assertions.assertEquals(userDevice.name, fetchedDeviceByAdmin.name)

        // Verificar que el user no puede ver el dispositivo del admin
        val exception = assertThrows<IllegalArgumentException> {
            deviceService.getDeviceById(adminDevice.id)
        }
        Assertions.assertEquals("Access denied to device with ID ${adminDevice.id}.", exception.message)

        // Verificar que el admin puede ver su propio dispositivo
        val fetchedAdminDevice = deviceService.getDeviceById(adminDevice.id!!)
        Assertions.assertEquals(adminDevice.name, fetchedAdminDevice.name)
    }
}