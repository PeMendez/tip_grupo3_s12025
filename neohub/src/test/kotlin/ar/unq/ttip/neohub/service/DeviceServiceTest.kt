package ar.unq.ttip.neohub.service
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.repository.DeviceRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher

@SpringBootTest
class DeviceServiceTest {
    private val mqttService = mock(MqttService::class.java)
    private val repository = mock(DeviceRepository::class.java)
    private val factory = mock(DeviceFactory::class.java)
    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher
    //tengo que inyectar yo esto poerque sino no anda.. vale la pena testear esto ?
    private var deviceService = DeviceService(mqttService,repository, factory)

    @Test
    fun `when UnconfiguredDeviceEvent is published, should call handleNewDevice`() {
        val testMessage = "test_device_message"
        val mockDevice = mock(Device::class.java)

        `when`(factory.createDevice(anyString(), anyString())).thenReturn(mockDevice)
        `when`(repository.save(any(Device::class.java))).thenReturn(mockDevice)

        applicationEventPublisher.publishEvent(UnconfiguredDeviceEvent(testMessage))

        verify(factory, times(1)).createDevice(anyString(), anyString())
        verify(repository, times(1)).save(any(Device::class.java))
    }

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
