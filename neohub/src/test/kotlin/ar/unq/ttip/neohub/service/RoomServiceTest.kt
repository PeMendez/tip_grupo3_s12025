package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RoomServiceTest {
    @Autowired
    lateinit var roomService: RoomService
    @Test
    fun `add device to room, remove it and check it no longer exists`() {
        val room = Room(name = "Living Room")
        val device = SmartOutlet(name = "Smart Outlet")

        // Verificar que el dispositivo no está en ninguna sala inicialmente
        assertNull(device.room)

        // Agregar el dispositivo a la sala
        roomService.addDeviceToRoom(device,room)

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