package ar.unq.ttip.neohub.service
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

    //tengo que inyectar yo esto poerque sino no anda.. vale la pena testear esto ?
    private val user = User(21,"carlos","sdasdada")
    private val home = Home(1,user)
    private var deviceService = DeviceService(mqttServiceMock,repositoryMock, factoryMock)

    @Test
    fun `registrar un dispositivo debería delegar al MqttService`() {
        // Arrange
        val device = SmartOutlet(name = "Lamp")

        // Configura el mock para que repository.save(device) devuelva el dispositivo
        `when`(repositoryMock.save(device)).thenReturn(device)

        // Act
        val result = deviceService.registerDevice(device)

        // Assert
        verify(mqttServiceMock).registerDevice(device) // Verifica que se llamó a mqttService
        verify(repositoryMock).save(device) // Verifica que se guardó en el repositorio
        assertEquals(device, result) // Asegúrate de que se devolvió el dispositivo esperado
    }


    @Test
    fun `desregistrar un dispositivo debería delegar al MqttService`() {
        val device = SmartOutlet(name = "Lamp")

        deviceService.unregisterDevice(device)

        verify(mqttServiceMock).unregisterDevice(device)
    }

    @Test
    fun `publicar un mensaje a un dispositivo debería delegar al MqttService y configurar correctamente el tópico`() {
        // Arrange
        val device = SmartOutlet(name = "Lamp")
        val room = Room(home=home, name = "LivingRoom")
        room.deviceList.add(device) // Agregamos el dispositivo al cuarto
        device.room = room
        device.configureTopic() // Configuramos el tópico basado en el cuarto y el dispositivo

        val expectedTopic = "neohub/LivingRoom/smartOutlet/Lamp"
        val message = "Turn On"

        // Act
        deviceService.publishToDevice(device, message)

        // Assert
        assert(device.topic == expectedTopic) // Verificamos que el tópico sea el esperado
        verify(mqttServiceMock).publish(expectedTopic, message) // Verificamos que se publique al tópico correcto
    }

}
