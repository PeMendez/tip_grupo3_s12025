package ar.unq.ttip.neohub.handler

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArrayList


@Component
class MqttWebSocketHandler : TextWebSocketHandler() {

    private val sessions = CopyOnWriteArrayList<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        println("Cliente WebSocket conectado: ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        println("Cliente WebSocket desconectado: ${session.id}")
    }

    fun sendMessage(message: String) {
        println("Enviando mensaje simple a ${sessions.size} clientes: $message")
        sessions.forEach { session ->
            if (session.isOpen) {
                try {
                    session.sendMessage(TextMessage(message))
                } catch (e: Exception) {
                    println("Error enviando mensaje a ${session.id}: ${e.message}")
                }
            }
        }
    }

    fun handlePhysicalButtonPress() {
        println("Botón físico presionado. Enviando alerta a ${sessions.size} clientes WebSocket.")
        val alertMessage = buildJsonMessage(
            type = "ALARM_TRIGGERED",
            message = "¡Alerta activada!"
        )
        broadcastJson(alertMessage)
    }

    fun sendTemperatureUpdate(temperature: String) {
        val tempMessage = """
        {
            "type": "TEMP_UPDATE",
            "temp": $temperature
        }
    """.trimIndent()

        broadcastJson(tempMessage)
    }


    fun sendLedStatusUpdate(status: String) {
        val ledMessage = buildJsonMessage(
            type = "LED_STATUS",
            message = "Estado del LED: $status"
        )
        broadcastJson(ledMessage)
    }

    private fun broadcastJson(json: String) {
        sessions.forEach { session ->
            if (session.isOpen) {
                try {
                    session.sendMessage(TextMessage(json))
                } catch (e: Exception) {
                    println("Error enviando JSON a ${session.id}: ${e.message}")
                }
            }
        }
    }

    private fun buildJsonMessage(type: String, message: String): String {
        return """
        {
            "type": "$type",
            "message": "$message",
            "timestamp": ${System.currentTimeMillis()}
        }
        """.trimIndent()
    }
}
