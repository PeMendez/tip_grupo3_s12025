package ar.unq.ttip.neohub.handler

import org.springframework.stereotype.Component
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

    override fun afterConnectionClosed(session: WebSocketSession, status: org.springframework.web.socket.CloseStatus) {
        sessions.remove(session)
        println("Cliente WebSocket desconectado: ${session.id}")
    }

    fun sendMessage(message: String) {
        for (session in sessions) {
            if (session.isOpen) {
                session.sendMessage(TextMessage(message))
            }
        }
    }

    fun handlePhysicalButtonPress() {
        println("Enviando alerta a ${sessions.size} clientes conectados")
        val alertMessage = """
    {
        "type": "ALARM_TRIGGERED",
        "message": "¡Alerta activada!",
        "timestamp": ${System.currentTimeMillis()}
    }
    """.trimIndent()

        sessions.forEach { session ->
            println("Enviando a sesión ${session.id} - Abierta: ${session.isOpen}")
            if (session.isOpen) {
                try {
                    session.sendMessage(TextMessage(alertMessage))
                } catch (e: Exception) {
                    println("Error enviando mensaje a ${session.id}: ${e.message}")
                }
            }
        }
    }
}
