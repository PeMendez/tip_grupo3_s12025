package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RoomServiceTest {
    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var deviceService: DeviceService
    @Autowired
    private lateinit var homeService: HomeService

    @Autowired
    lateinit var roomService: RoomService

    private var user = User(21,"carlos","sdasdada")
    var home: Home = Home(user=user)

    @BeforeEach
    fun init(){
        authService.register(
            RegisterRequest(user.username
            ,user.password
            ,user.password)
        )
        home = homeService.newHome(Home(user=user))
    }
    @Transactional
    @Test
    fun `add device to room, remove it and check it no longer exists`() {
        val room =roomService.addNewRoom(Room(home = home, name = "Living Room"))
        val devicePre = SmartOutlet(name = "Smart Outlet")
        // Verificar que el dispositivo no está en ninguna sala inicialmente
        assertNull(devicePre.room)

        // Agregar el dispositivo a la sala
        roomService.addDeviceToRoom(room.id,devicePre.toDTO())
        val device = room.deviceList.first() //de nuevo no se como obtenerlo sino, o no me anda

        // Verificar que el dispositivo está en la sala
        assertTrue(room.deviceList.contains(device))
        assertEquals(room, device.room)

        // Eliminar el dispositivo de la sala
        roomService.removeDeviceFromRoom(device,room)
        // Verificar que el dispositivo ya no está en la sala
        assertFalse(room.deviceList.contains(device))
        // Verificar que el dispositivo no está asociado a ninguna sala
        assertNull(device.room)
    }

}