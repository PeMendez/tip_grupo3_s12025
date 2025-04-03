package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.handler.MqttWebSocketHandler
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.stereotype.Service
import java.util.*

@Service
class MqttService(private val webSocketHandler: MqttWebSocketHandler) {
    private val brokerUrl = "tcp://test.mosquitto.org:1883"
    private val topic = "unq-button"
    private val clientId = "NeoHub-API-" + UUID.randomUUID().toString().substring(0, 8)
    private val mqttClient: MqttClient = MqttClient(brokerUrl, clientId, null)

    init {
        try {
            //mqttClient = MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId())
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                isAutomaticReconnect = true
            }
            mqttClient.connect(options)
            println("Conectado exitosamente al broker MQTT.")
            subscribeTopic(topic)
        } catch (e: MqttException) {
            throw RuntimeException("Error al conectar con el broker.", e)
        }
    }

    fun publish(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray()).apply { qos = 1 }
            mqttClient.publish(topic, mqttMessage)
            println("Mensaje publicado: $message en el tópico: $topic")
        } catch (e: MqttException) {
            println("Error al publicar mensaje: ${e.message}")
        }
    }

    private fun subscribeTopic(topic: String) {
        try {
                println("DEBUG: Intentando suscribirse al tópico: $topic")

                mqttClient.subscribe(topic) { receivedTopic, message ->
                    val payload = String(message.payload)
                    println("DEBUG: Mensaje recibido en '$receivedTopic': $payload")

                    when {
                        payload.equals("wasPressed", ignoreCase = true) -> {
                            println("Botón físico detectado - Activando alarma")
                            webSocketHandler.handlePhysicalButtonPress()
                        }

                        payload.startsWith("LED_") || payload == "toggle" -> {
                            println("Comando para LED recibido: $payload")
                            webSocketHandler.sendMessage(
                                if (payload == "toggle") "LED_TOGGLE" else payload
                            )
                        }
                        else -> {
                            println("Mensaje no reconocido: '$payload'")
                        }

                    }
                }
                println("DEBUG: Suscripción exitosa al tópico: $topic")
        } catch (e: MqttException) {
                println("ERROR: No se pudo suscribir a MQTT - ${e.message}")
                e.printStackTrace()
        }
    }
}
