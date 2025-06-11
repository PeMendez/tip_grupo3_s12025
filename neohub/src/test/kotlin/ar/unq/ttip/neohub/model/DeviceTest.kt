package ar.unq.ttip.neohub.model

import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class DeviceTest {
    private val home = Home(1,"myHome", accessKey = "123")

    @Test
    fun `create device without room and add it later`() {
        val device = SmartOutlet(name = "Smart Outlet")
        val room = Room(home = home, name = "Kitchen")

        // Dispositivo inicialmente no asociado a ninguna sala
        assert(device.room == null)

        // Asocia el dispositivo a una sala
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
        kotlin.test.assertEquals("neohub/LivingRoom/smart_outlet/Lamp", smartOutlet.topic)

        // Simular mensajes MQTT
        smartOutlet.turnOn()
        kotlin.test.assertEquals(true, smartOutlet.isOn)

        smartOutlet.turnOff()
        kotlin.test.assertEquals(false, smartOutlet.isOn)

        smartOutlet.toggle()
        kotlin.test.assertEquals(true, smartOutlet.isOn)

        smartOutlet.handleAttributeUpdate("status", "invalidValue")
        kotlin.test.assertEquals(true, smartOutlet.isOn, "Estado no debería cambiar con comandos inválidos")
    }

    @Test
    fun `TemperatureSensor processes MQTT messages correctly`() {
        // Crear un cuarto y un TemperatureSensor
        val room = Room(home = home, name = "Bedroom")
        val tempSensor = TemperatureSensor(name = "Thermometer", room = room)

        // Configurar el topic del TemperatureSensor
        tempSensor.configureTopic()
        kotlin.test.assertEquals("neohub/Bedroom/temperature_sensor/Thermometer", tempSensor.topic)

        // Simular mensajes MQTT
        tempSensor.updateTemperature("25.5")
        kotlin.test.assertEquals(25.5, tempSensor.temperature)

        tempSensor.updateTemperature("18.3")
        kotlin.test.assertEquals(18.3, tempSensor.temperature)

        tempSensor.updateTemperature("invalid")
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
        kotlin.test.assertEquals("neohub/Kitchen/temperature_sensor/Thermometer", tempSensor.topic)
    }
}