package ar.unq.ttip.neohub.handler

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArrayList


@Component
class MqttWebSocketHandler : TextWebSocketHandler()
{
    init {
        println("MqttWebSocketHandler iniciandose")
    }
    private val sessions = CopyOnWriteArrayList<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("Cliente WebSocket conectado: ${session.id}")
        sessions.forEach {
            try {
                if (it.isOpen) {
                    println("Cerrando sesión anterior: ${it.id}")
                    it.close(CloseStatus.NORMAL)
                }
            } catch (e: Exception) {
                println("Error cerrando sesión anterior: ${e.message}")
            }
        }

        sessions.clear()
        sessions.add(session)
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
                "message" to "Se abrió la puerta sin autorización",
                "timestamp" to System.currentTimeMillis()
            )
        )
        broadcastJson(alertMessage)
    }


    fun sendTemperatureUpdate(temperature: Any, deviceId: Long) {
        val tempMessage = buildJsonMessage(
            mapOf(
                "type" to "TEMP_UPDATE",
                "id" to deviceId,
                "temp" to temperature
            )
        )
        println("Enviando update de temperatura: $temperature")
        broadcastJson(tempMessage)
    }

    fun sendOpeningUpdate(status: Any, deviceId: Long) {
        val statusMessage = buildJsonMessage(
            mapOf(
                "type" to "OPENING_UPDATE",
                "id" to deviceId,
                "status" to status
            )
        )
        println("Enviando update de status: $statusMessage")
        broadcastJson(statusMessage)
    }

    fun sendSmartOutletUpdate(status: Any, deviceId: Long) {
        val statusMessage = buildJsonMessage(
            mapOf(
                "type" to "SMART_OUTLET",
                "id" to deviceId,
                "status" to status
            )
        )
        println("Enviando update de status: $statusMessage")
        broadcastJson(statusMessage)
    }

    fun sendDimmerUpdate(brightness: Any, deviceId: Long) {
        val statusMessage = buildJsonMessage(
            mapOf(
                "type" to "DIMMER_UPDATE",
                "id" to deviceId,
                "brightness" to brightness
            )
        )
        println("Enviando update de status: $statusMessage")
        broadcastJson(statusMessage)
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
        println("Cantidad de sesiones abiertas: ${sessions.size}")
        sessions.forEach { session ->
            if (session.isOpen) {
                try {
                    session.sendMessage(TextMessage(json))
                    println("Mensaje de webSocket enviado con éxito.")
                } catch (e: Exception) {
                    println("Error enviando JSON a ${session.id}: ${e.message}")
                }
            }
        }
    }

}
