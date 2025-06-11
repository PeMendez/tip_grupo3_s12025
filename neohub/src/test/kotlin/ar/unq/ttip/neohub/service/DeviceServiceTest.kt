package ar.unq.ttip.neohub.service
import ar.unq.ttip.neohub.dto.DeviceCommand
import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.repository.DeviceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
@SpringBootTest
class DeviceServiceTest {
    private val mqttServiceMock = mock(MqttService::class.java)
    private val repositoryMock = mock(DeviceRepository::class.java)
    private val factoryMock = mock(DeviceFactory::class.java)

    @Autowired
    lateinit var deviceFactory: DeviceFactory
    @Autowired
    lateinit var objectMapper: ObjectMapper
    private val user = User(21, "carlos", "sdasdada")
    private val home = Home(1, "myHome", accessKey = "123")
    private var deviceService = DeviceService(mqttServiceMock, repositoryMock, factoryMock)

    @Test
    fun `desregistrar un dispositivo deberia delegar al MqttService`() {
        // Arrange
        val deviceDTO = DeviceDTO(id = 1, name = "Lamp", type = DeviceType.SMART_OUTLET.toString(), roomId = null, topic = "neohub/unconfigured")
        val device = SmartOutlet(name = "Lamp")

        `when`(repositoryMock.findById(deviceDTO.id)).thenReturn(Optional.of(device))

        // Act
        deviceService.unregisterDevice(deviceDTO.id)

        // Assert
        verify(mqttServiceMock).unregisterDevice(device)
    }

    @Test
    fun `publicar un mensaje a un dispositivo deberia delegar al MqttService y configurar correctamente el topico`() {
        // Arrange
        val deviceDTO = DeviceDTO(id = 1, name = "Lamp", type = DeviceType.SMART_OUTLET.toString(), roomId = 2, macAddress = "ABC123", topic = "neohub/unconfigured")
        val room = Room(home = home, name = "LivingRoom")
        val device = deviceDTO.toEntity(deviceFactory)
        room.deviceList.add(device)
        device.room = room
        device.macAddress = "ABC123"
        device.configureTopic()

        val expectedTopic = "neohub/LivingRoom/" + DeviceType.SMART_OUTLET.toString() +  "/Lamp/command"

        val message = "TURN_ON"
        val command = objectMapper.writeValueAsString(
        DeviceCommand(
            mac_address = "ABC123",
            command = message,
            parameters = "",
        ))

        `when`(repositoryMock.findById(deviceDTO.id)).thenReturn(Optional.of(device))

        // Act
        deviceService.sendCommand(deviceDTO.id, message)

        val commandTopic = device.topic+"/command"
        // Assert
        assertEquals(expectedTopic, commandTopic)
        verify(mqttServiceMock).publish(expectedTopic, command)
    }
}

