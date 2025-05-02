package ar.unq.ttip.neohub.model

import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import ar.unq.ttip.neohub.service.RoomService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DeviceTest {
    @Autowired
    lateinit var roomService: RoomService
    private val user = User(21,"carlos","sdasdada")
    private val home = Home(1,user)

    @Test
    fun `create device without room and add it later`() {
        val device = SmartOutlet(name = "Smart Outlet")
        val room = Room(home = home, name = "Kitchen")

        // Dispositivo inicialmente no asociado a ninguna sala
        assert(device.room == null)

        // Asocia el dispositivo a una sala
        //roomService.addDeviceToRoom(room.id,device.toDTO())
        room.addDevice(device)

        assert(device.room == room)
        assert(room.deviceList.contains(device))
    }

    @Test
    fun `test smart outlet turn on, turn off, and toggle`() {
        val room = Room(home = home, name = "Living Room")
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

    @Test
    fun `SmartOutlet processes MQTT messages correctly`() {
        // Crear un cuarto y un SmartOutlet
        val room = Room(home = home, name = "LivingRoom")
        val smartOutlet = SmartOutlet(name = "Lamp", room = room)

        // Configurar el topic del SmartOutlet
        smartOutlet.configureTopic()
        kotlin.test.assertEquals("neohub/LivingRoom/smartOutlet/Lamp", smartOutlet.topic)

        // Simular mensajes MQTT
        smartOutlet.handleIncomingMessage("turn_on")
        kotlin.test.assertEquals(true, smartOutlet.isOn)

        smartOutlet.handleIncomingMessage("turn_off")
        kotlin.test.assertEquals(false, smartOutlet.isOn)

        smartOutlet.handleIncomingMessage("toggle")
        kotlin.test.assertEquals(true, smartOutlet.isOn)

        smartOutlet.handleIncomingMessage("invalid_command")
        kotlin.test.assertEquals(true, smartOutlet.isOn, "Estado no debería cambiar con comandos inválidos")
    }

    @Test
    fun `TemperatureSensor processes MQTT messages correctly`() {
        // Crear un cuarto y un TemperatureSensor
        val room = Room(home = home, name = "Bedroom")
        val tempSensor = TemperatureSensor(name = "Thermometer", room = room)

        // Configurar el topic del TemperatureSensor
        tempSensor.configureTopic()
        kotlin.test.assertEquals("neohub/Bedroom/temperatureSensor/Thermometer", tempSensor.topic)

        // Simular mensajes MQTT
        tempSensor.handleIncomingMessage("25.5")
        kotlin.test.assertEquals(25.5, tempSensor.temperature)

        tempSensor.handleIncomingMessage("18.3")
        kotlin.test.assertEquals(18.3, tempSensor.temperature)

        tempSensor.handleIncomingMessage("invalid")
        kotlin.test.assertEquals(18.3, tempSensor.temperature, "Temperatura no debería cambiar con mensajes inválidos")
    }

    @Test
    fun `Device without room remains in unconfigured topic`() {
        // Crear un dispositivo sin cuarto
        val tempSensor = TemperatureSensor(name = "Thermometer")

        // Comprobar el tópico inicial
        kotlin.test.assertEquals("neohub/unconfigured", tempSensor.topic)

        // Configurar el tópico nuevamente sin cuarto asignado
        tempSensor.configureTopic()
        kotlin.test.assertEquals("neohub/unconfigured", tempSensor.topic)

        // Asignar un cuarto y actualizar el tópico
        val room = Room(home = home, name = "Kitchen")
        tempSensor.room = room
        tempSensor.configureTopic()
        kotlin.test.assertEquals("neohub/Kitchen/temperatureSensor/Thermometer", tempSensor.topic)
    }
}