package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoomServiceTest {
    @Autowired
    private lateinit var userService: UserService
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

    @BeforeAll
    fun setup(){
        authService.register(
            RegisterRequest("admin"
            ,"clave"
            ,"clave"
            ,"CREATE"
            ,"homeName"
            ,"key123"
        ))
    }

    /*@Transactional
    @Test
    fun `add device to room, remove it and check it no longer exists`() {
        // Crear un nuevo room asociado al home
        val user = userService.getUserByUsername("admin")
        val newHome = homeService.getAdminHomeForUser(user.id)
        val room = roomService.addNewRoom(newHome.id, Room(home = newHome, name = "LivingRoom").toDTO())

        // Crear un dispositivo inicial
        val deviceDTO = DeviceDTO(
            id = 27,
            name = "Lamp",
            type = "smart_outlet",
            roomId = null,
            macAddress = "ABC123",
            ownerId = user.id,
            topic = "neohub/unconfigured"
        )
        val newDevice = deviceDTO.toEntity(deviceFactory)
        newDevice.macAddress= "ABC123"
        val newDeviceDTO = deviceService.saveDevice(newDevice)
        // Verificar que el dispositivo no está asociado a ninguna sala inicialmente
        assertNull(newDeviceDTO.roomId)

        // Agregar el dispositivo a la sala
        roomService.addDeviceToRoom(room.id, newDeviceDTO.id)
        val updatedRoom = roomService.getRoomDetails(room.id)

        // Verificar que el dispositivo está en la sala
        val addedDevice = updatedRoom.deviceList.first { it.name == newDeviceDTO.name }
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
    }*/
}