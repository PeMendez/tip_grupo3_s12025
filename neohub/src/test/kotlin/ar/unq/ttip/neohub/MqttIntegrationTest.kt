package ar.unq.ttip.neohub
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.service.DeviceService
import ar.unq.ttip.neohub.service.RoomService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class MqttIntegrationTest @Autowired constructor(
    private val roomService: RoomService,
    private val deviceService: DeviceService,
) {

    @Test
    fun `smart outlet maneja correctamente mensajes MQTT reales`() {
        // Arrange
        val smartOutlet = SmartOutlet(name = "Lamp")
        val room = Room(name = "LivingRoom")
        roomService.addDeviceToRoom(smartOutlet, room)
        /*room.deviceList.add(smartOutlet)
        smartOutlet.room = room
        smartOutlet.configureTopic()*/

        deviceService.registerDevice(smartOutlet) // Registra el dispositivo en MQTT

        val validMessages = mapOf(
            "turn_on" to true,
            "turn_off" to false
        )

        val invalidMessage = "INVALID_COMMAND"

        // Act & Assert
        validMessages.forEach { (message, expectedState) ->
            deviceService.publishToDevice(smartOutlet, message)
            Thread.sleep(500) // Esperar un momento para que se procese el mensaje

            assertEquals(expectedState, smartOutlet.isOn, "El estado del SmartOutlet no coincide con el mensaje enviado: $message")
        }

        // Probar un mensaje inválido
        deviceService.publishToDevice(smartOutlet, invalidMessage)
        Thread.sleep(500) // Esperar un momento para que se procese el mensaje

        // Verificar que el estado del SmartOutlet no cambió
        assertEquals(false, smartOutlet.isOn, "El SmartOutlet no debería cambiar de estado con un mensaje inválido.")
    }
}
