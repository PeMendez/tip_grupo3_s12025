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
        val alertMessage = buildJsonMessage(
            mapOf(
                "type" to "ALARM_TRIGGERED",
                "message" to "¡Alerta activada!",
                "timestamp" to System.currentTimeMillis()
            )
        )
        broadcastJson(alertMessage)
    }


    fun sendTemperatureUpdate(temperature: String) {
        val tempMessage = buildJsonMessage(
            mapOf(
                "type" to "TEMP_UPDATE",
                "temp" to temperature
            )
        )
        broadcastJson(tempMessage)
    }

    fun sendLedStatusUpdate(status: String) {
        val ledMessage = buildJsonMessage(
            mapOf(
                "type" to "LED",
                "status" to status
            )
        )
        broadcastJson(ledMessage)

        println("Enviando alerta a ${sessions.size} clientes conectados")
        val alertMessage = """
        {
            "type": "ALARM_TRIGGERED",
            "message": "Se abrió la puerta sin autorización",
            "timestamp": ${System.currentTimeMillis()}
        """
    }

    fun buildJsonMessage(data: Map<String, Any>): String {
        val jsonBuilder = StringBuilder("{")

        data.entries.forEachIndexed { index, entry ->
            val key = entry.key
            val value = entry.value

            jsonBuilder.append("\"$key\": ")
            jsonBuilder.append(
                when (value) {
                    is Number, is Boolean -> value.toString()
                    else -> "\"${value.toString()}\""
                }
            )

            if (index < data.size - 1) jsonBuilder.append(", ")
        }

        jsonBuilder.append("}")
        return jsonBuilder.toString()
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

}
