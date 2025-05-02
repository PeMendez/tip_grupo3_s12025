package ar.unq.ttip.neohub
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.service.*
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@SpringBootTest
class MqttIntegrationTest @Autowired constructor(
    private val roomService: RoomService,
    private val deviceService: DeviceService,
    private val homeService: HomeService,
    private val mqttService: MqttService
) {
    @Autowired
    private lateinit var deviceFactory: DeviceFactory
    private val applicationEventPublisher = mock(ApplicationEventPublisher::class.java)
    private val user = User(21,"carlos","sdasdada")
    private val home = Home(user=user)

    @Test
    fun `when handleUnconfiguredDevice is called, should publish UnconfiguredDeviceEvent`() {
        val mqttServiceMock = MqttService(applicationEventPublisher)
        val testMessage = "test_device_message"
        //tengo que inyectarle el mock a mano porque sino usa el bean real.
        //pero veia por consola que andaba, asi que yo ya sabia que estaba andando
        mqttServiceMock.handleUnconfiguredDevice(testMessage)

        val eventCaptor = ArgumentCaptor.forClass(UnconfiguredDeviceEvent::class.java)
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture())
        val publishedEvent = eventCaptor.value
        assertEquals(publishedEvent.message, testMessage)
    }

    @Transactional
    @Test
    fun `smart outlet maneja correctamente mensajes MQTT reales`() {
        // Arrange
        val newHome = homeService.newHome(home)
        val smartOutlet = SmartOutlet(name = "Lamp")
        val room = roomService.addNewRoom(Room(home= newHome, name = "LivingRoom"))
        //val deviceA = deviceService.registerDevice(smartOutlet) as SmartOutlet // Registra el dispositivo en MQTT
        roomService.addDeviceToRoom(room.id, smartOutlet.toDTO())
        val device = room.deviceList.first() as SmartOutlet //no se como sacarlo sino


        val validMessages = mapOf(
            "turn_on" to true,
            "turn_off" to false
        )

        val invalidMessage = "INVALID_COMMAND"

        // Act & Assert
        validMessages.forEach { (message, expectedState) ->
            deviceService.publishToDevice(device, message)
            Thread.sleep(500) // Esperar un momento para que se procese el mensaje

            assertEquals(expectedState, device.isOn, "El estado del SmartOutlet no coincide con el mensaje enviado: $message")
        }

        // Probar un mensaje inválido
        deviceService.publishToDevice(device, invalidMessage)
        Thread.sleep(500) // Esperar un momento para que se procese el mensaje

        // Verificar que el estado del SmartOutlet no cambió
        assertFalse(device.isOn, "El SmartOutlet no debería cambiar de estado con un mensaje inválido.")
    }

}
