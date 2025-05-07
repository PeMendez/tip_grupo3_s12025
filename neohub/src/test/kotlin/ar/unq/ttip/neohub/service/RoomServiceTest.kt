package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class RoomServiceTest {
    @Autowired
    private lateinit var authService: AuthService
    @Autowired
    private lateinit var deviceFactory: DeviceFactory

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
        // Crear un nuevo room asociado al home
        val newHome = homeService.newHome(home)

        val room = roomService.addNewRoom(home.id, Room(home = newHome, name = "LivingRoom").toDTO())

        // Crear un dispositivo inicial
        val deviceDTO = DeviceDTO(
            id = 27,
            name = "Lamp",
            type = "smartOutlet",
            roomId = null,
            topic = "neohub/unconfigured"
        )
        val newDevice = deviceService.saveDevice(deviceDTO.toEntity(deviceFactory))
        // Verificar que el dispositivo no está asociado a ninguna sala inicialmente
        assertNull(newDevice.roomId)

        // Agregar el dispositivo a la sala
        roomService.addDeviceToRoom(room.id, newDevice.id)
        val updatedRoom = roomService.getRoomDetails(room.id)

        // Verificar que el dispositivo está en la sala
        val addedDevice = updatedRoom.deviceList.first { it.name == newDevice.name }
        assertTrue(updatedRoom.deviceList.contains(addedDevice))
        assertEquals(room.id, addedDevice.room!!.id)

        // Eliminar el dispositivo de la sala
        roomService.removeDeviceFromRoom(addedDevice.id, room.id)
        val updatedRoomAfterRemoval = roomService.getRoomDetails(room.id)

        // Verificar que el dispositivo ya no está en la sala
        assertFalse(updatedRoomAfterRemoval.deviceList.any { it.id == addedDevice.id })

        // Verificar que el dispositivo no está asociado a ninguna sala
        val detachedDevice = deviceService.getDeviceById(addedDevice.id)
        assertNull(detachedDevice.roomId)
    }
}