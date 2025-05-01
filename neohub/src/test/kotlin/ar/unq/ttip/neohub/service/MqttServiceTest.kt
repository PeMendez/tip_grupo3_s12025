package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MqttServiceTest {

    @Test
    fun `SmartOutlet processes MQTT messages correctly`() {
        // Crear un cuarto y un SmartOutlet
        val room = Room(name = "LivingRoom")
        val smartOutlet = SmartOutlet(name = "Lamp", room = room)

        // Configurar el topic del SmartOutlet
        smartOutlet.configureTopic()
        assertEquals("neohub/LivingRoom/smartOutlet/Lamp", smartOutlet.topic)

        // Simular mensajes MQTT
        smartOutlet.handleIncomingMessage("turn_on")
        assertEquals(true, smartOutlet.isOn)

        smartOutlet.handleIncomingMessage("turn_off")
        assertEquals(false, smartOutlet.isOn)

        smartOutlet.handleIncomingMessage("toggle")
        assertEquals(true, smartOutlet.isOn)

        smartOutlet.handleIncomingMessage("invalid_command")
        assertEquals(true, smartOutlet.isOn, "Estado no debería cambiar con comandos inválidos")
    }

    @Test
    fun `TemperatureSensor processes MQTT messages correctly`() {
        // Crear un cuarto y un TemperatureSensor
        val room = Room(name = "Bedroom")
        val tempSensor = TemperatureSensor(name = "Thermometer", room = room)

        // Configurar el topic del TemperatureSensor
        tempSensor.configureTopic()
        assertEquals("neohub/Bedroom/temperatureSensor/Thermometer", tempSensor.topic)

        // Simular mensajes MQTT
        tempSensor.handleIncomingMessage("25.5")
        assertEquals(25.5, tempSensor.temperature)

        tempSensor.handleIncomingMessage("18.3")
        assertEquals(18.3, tempSensor.temperature)

        tempSensor.handleIncomingMessage("invalid")
        assertEquals(18.3, tempSensor.temperature, "Temperatura no debería cambiar con mensajes inválidos")
    }

    @Test
    fun `Device without room remains in unconfigured topic`() {
        // Crear un dispositivo sin cuarto
        val tempSensor = TemperatureSensor(name = "Thermometer")

        // Comprobar el tópico inicial
        assertEquals("neohub/unconfigured", tempSensor.topic)

        // Configurar el tópico nuevamente sin cuarto asignado
        tempSensor.configureTopic()
        assertEquals("neohub/unconfigured", tempSensor.topic)

        // Asignar un cuarto y actualizar el tópico
        val room = Room(name = "Kitchen")
        tempSensor.room = room
        tempSensor.configureTopic()
        assertEquals("neohub/Kitchen/temperatureSensor/Thermometer", tempSensor.topic)
    }
}
