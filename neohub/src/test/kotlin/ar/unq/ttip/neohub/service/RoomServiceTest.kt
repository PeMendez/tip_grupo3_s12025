package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.RegisterRequest
import ar.unq.ttip.neohub.dto.RoomDTO
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.User
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
        val roomDTO = RoomDTO(id = 11, homeId = home.id, name = "Living Room", deviceList = emptyList())
        val savedRoom = roomService.addNewRoom(home.id, roomDTO)

        // Crear un dispositivo inicial
        val deviceDTO = DeviceDTO(id = 22, name = "Smart Outlet", type = "smartOutlet", roomId = null, topic = "neohub/unconfigured")

        // Verificar que el dispositivo no está asociado a ninguna sala inicialmente
        assertNull(deviceDTO.roomId)

        // Agregar el dispositivo a la sala
        roomService.addDeviceToRoom(savedRoom.id, deviceDTO.id)
        val updatedRoom = roomService.getRoomDetails(savedRoom.id)

        // Verificar que el dispositivo está en la sala
        val addedDevice = updatedRoom.deviceList.first { it.name == deviceDTO.name }
        assertTrue(updatedRoom.deviceList.contains(addedDevice))
        assertEquals(savedRoom.id, addedDevice.room!!.id)

        // Eliminar el dispositivo de la sala
        roomService.removeDeviceFromRoom(addedDevice.id, savedRoom.id)
        val updatedRoomAfterRemoval = roomService.getRoomDetails(savedRoom.id)

        // Verificar que el dispositivo ya no está en la sala
        assertFalse(updatedRoomAfterRemoval.deviceList.any { it.id == addedDevice.id })

        // Verificar que el dispositivo no está asociado a ninguna sala
        val detachedDevice = deviceService.getDeviceById(addedDevice.id)
        assertNull(detachedDevice.roomId)
    }
}