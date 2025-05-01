package ar.unq.ttip.neohub.model

import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.service.RoomService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DeviceTest {
    @Autowired
    lateinit var roomService: RoomService

    @Test
    fun `create device without room and add it later`() {
        val device = SmartOutlet(name = "Smart Outlet")
        val room = Room(name = "Kitchen")

        // Dispositivo inicialmente no asociado a ninguna sala
        assert(device.room == null)

        // Asocia el dispositivo a una sala
        roomService.addDeviceToRoom(device,room)

        assert(device.room == room)
        assert(room.deviceList.contains(device))
    }

    @Test
    fun `test smart outlet turn on, turn off, and toggle`() {
        val room = Room(name = "Living Room")
        val smartOutlet = SmartOutlet(name = "Smart Outlet", room = room)

        assertFalse(smartOutlet.isOn)

        smartOutlet.turnOn()
        assertTrue(smartOutlet.isOn)

        smartOutlet.turnOff()
        assertFalse(smartOutlet.isOn)

        smartOutlet.toggle()
        assertTrue(smartOutlet.isOn)

        smartOutlet.toggle()
        assertFalse(smartOutlet.isOn)
    }
}