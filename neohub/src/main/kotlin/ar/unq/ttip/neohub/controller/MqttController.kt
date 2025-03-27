package ar.unq.ttip.neohub.controller
import ar.unq.ttip.neohub.service.MqttService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MqttController(private val mqttService: MqttService) {

    @PostMapping("/api/mqtt/send")
    fun sendMessage(
        @RequestParam topic: String,
        @RequestParam message: String
    ): ResponseEntity<String> {
        return try {
            mqttService.publish(topic, message)
            ResponseEntity.ok("Mensaje enviado al topic '$topic'")
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Error al enviar mensaje: ${e.message}")
        }
    }
}
