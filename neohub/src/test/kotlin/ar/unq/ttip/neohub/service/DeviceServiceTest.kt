package ar.unq.ttip.neohub.service
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.repository.DeviceRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class DeviceServiceTest {
    private val mqttService = mock(MqttService::class.java)
    private val repository :DeviceRepository = mock(DeviceRepository::class.java)
    private val deviceService = DeviceService(mqttService, repository )

    @Test
    fun `registrar un dispositivo debería delegar al MqttService`() {
        val device = SmartOutlet(name = "Lamp")

        deviceService.registerDevice(device)

        verify(mqttService).registerDevice(device)
    }

    @Test
    fun `desregistrar un dispositivo debería delegar al MqttService`() {
        val device = SmartOutlet(name = "Lamp")

        deviceService.unregisterDevice(device)

        verify(mqttService).unregisterDevice(device)
    }

    @Test
    fun `publicar un mensaje a un dispositivo debería delegar al MqttService y configurar correctamente el tópico`() {
        // Arrange
        val device = SmartOutlet(name = "Lamp")
        val room = Room(name = "LivingRoom")
        room.deviceList.add(device) // Agregamos el dispositivo al cuarto
        device.room = room
        device.configureTopic() // Configuramos el tópico basado en el cuarto y el dispositivo

        val expectedTopic = "neohub/LivingRoom/smartOutlet/Lamp"
        val message = "Turn On"

        // Act
        deviceService.publishToDevice(device, message)

        // Assert
        assert(device.topic == expectedTopic) // Verificamos que el tópico sea el esperado
        verify(mqttService).publish(expectedTopic, message) // Verificamos que se publique al tópico correcto
    }

}
