package ar.unq.ttip.neohub.service
import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.repository.DeviceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
class DeviceServiceTest {
    private val mqttServiceMock = mock(MqttService::class.java)
    private val repositoryMock = mock(DeviceRepository::class.java)
    private val factoryMock = mock(DeviceFactory::class.java)

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    lateinit var deviceFactory: DeviceFactory

    private val user = User(21, "carlos", "sdasdada")
    private val home = Home(1, user)
    private var deviceService = DeviceService(mqttServiceMock, repositoryMock, factoryMock)

    @Test
    fun `registrar un dispositivo debería delegar al MqttService`() {
        // Arrange
        val deviceDTO = DeviceDTO(id = 12, name = "Lamp", type = "smartOutlet", roomId = 3, topic = "neohub/unconfigured")
        val device = SmartOutlet(name = "Lamp")

        `when`(factoryMock.createDevice(deviceDTO.name, deviceDTO.type)).thenReturn(device)
        `when`(repositoryMock.save(device)).thenReturn(device)

        // Act
        val result = deviceService.registerDeviceOnMqtt(deviceDTO.toEntity(deviceFactory))

        // Assert
        verify(mqttServiceMock).registerDevice(device)
        // No entiendo por qué falla este verify, cuando miro el diff son IGUALES SALVO UN ENTER AL FINAL.
        verify(repositoryMock).save(device)
        assertEquals(device.toDTO(), result)
    }

    @Test
    fun `desregistrar un dispositivo debería delegar al MqttService`() {
        // Arrange
        val deviceDTO = DeviceDTO(id = 1, name = "Lamp", type = "smartOutlet", roomId = null, topic = "neohub/unconfigured")
        val device = SmartOutlet(name = "Lamp")

        `when`(repositoryMock.findById(deviceDTO.id)).thenReturn(Optional.of(device))

        // Act
        deviceService.unregisterDevice(deviceDTO.id)

        // Assert
        verify(mqttServiceMock).unregisterDevice(device)
    }

    @Test
    fun `publicar un mensaje a un dispositivo debería delegar al MqttService y configurar correctamente el tópico`() {
        // Arrange
        val deviceDTO = DeviceDTO(id = 1, name = "Lamp", type = "smartOutlet", roomId = 2, topic = "neohub/unconfigured")
        val room = Room(home = home, name = "LivingRoom")
        val device = SmartOutlet(name = "Lamp")
        room.deviceList.add(device)
        device.room = room
        device.configureTopic()

        val expectedTopic = "neohub/LivingRoom/smartOutlet/Lamp"
        val message = "Turn On"

        `when`(repositoryMock.findById(deviceDTO.id)).thenReturn(Optional.of(device))

        // Act
        deviceService.publishToDevice(deviceDTO.id, message)

        // Assert
        assertEquals(expectedTopic, device.topic)
        verify(mqttServiceMock).publish(expectedTopic, message)
    }
}

