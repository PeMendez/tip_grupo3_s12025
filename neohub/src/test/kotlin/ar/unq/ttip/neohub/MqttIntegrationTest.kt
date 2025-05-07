package ar.unq.ttip.neohub
import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.handler.MqttWebSocketHandler
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.service.*
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ActiveProfiles("test")
@SpringBootTest
class MqttIntegrationTest @Autowired constructor(
    private val roomService: RoomService,
    private val deviceService: DeviceService,
    private val homeService: HomeService,
    private val mqttWebSocketHandler: MqttWebSocketHandler
) {
    @Autowired
    private lateinit var deviceFactory: DeviceFactory
    private val applicationEventPublisher = mock(ApplicationEventPublisher::class.java)
    private val user = User(21,"carlos","sdasdada")
    private val home = Home(user=user)

    @Test
    fun `when handleUnconfiguredDevice is called, should publish UnconfiguredDeviceEvent`() {
        val mqttServiceMock = MqttService(applicationEventPublisher, mqttWebSocketHandler)
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

        val roomDTO = roomService.addNewRoom(home.id, Room(home = newHome, name = "LivingRoom").toDTO())

        val smartOutletDTO = DeviceDTO(
            id = 27,
            name = "Lamp",
            type = "smartOutlet",
            roomId = null,
            topic = "neohub/unconfigured"
        )

        // Registrar el dispositivo en MQTT
        //val registeredDeviceDTO = deviceService.registerDevice(smartOutletDTO)
        val newDevice = deviceService.saveDevice(smartOutletDTO.toEntity(deviceFactory))
        // Agregar el dispositivo al cuarto
        val room = roomService.addDeviceToRoom(roomDTO.id, newDevice.id)

        // Recuperar el dispositivo registrado desde el cuarto
        val roomWithDevice = roomService.getRoomDetails(roomDTO.id)
        val device = roomWithDevice.deviceList.first { it.name == "Lamp" }
        val smartOutlet = device as SmartOutlet

        val validMessages = mapOf(
            "turn_on" to true,
            "turn_off" to false
        )

        val invalidMessage = "INVALID_COMMAND"

        val deviceDTO = device.toDTO()
        // Act & Assert
        validMessages.forEach { (message, expectedState) ->
            deviceService.publishToDevice(deviceDTO.id, message)
            Thread.sleep(500) // Esperar un momento para que se procese el mensaje

            assertEquals(expectedState, smartOutlet.isOn, "El estado del SmartOutlet no coincide con el mensaje enviado: $message")
        }

        // Probar un mensaje inválido
        deviceService.publishToDevice(deviceDTO.id, invalidMessage)
        Thread.sleep(500) // Esperar un momento para que se procese el mensaje

        // Verificar que el estado del SmartOutlet no cambió
        assertFalse(smartOutlet.isOn, "El SmartOutlet no debería cambiar de estado con un mensaje inválido.")
    }



}
