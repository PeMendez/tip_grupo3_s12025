package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.handler.MqttWebSocketHandler
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.stereotype.Service
import java.util.*

//@Service
class MqttServiceOld(private val webSocketHandler: MqttWebSocketHandler) {
    private val brokerUrl = "tcp://test.mosquitto.org:1883"
    private val clientId = "NeoHub-API-" + UUID.randomUUID().toString().substring(0, 8)
    private val mqttClient: MqttClient = MqttClient(brokerUrl, clientId, null)

    // Lista de tópicos a suscribirse
    private val topicsToSubscribe = listOf(
        "unq-button",
        "LEDctrl",
        "unq-temperature"
        //Agregar más si hace falta
    )

    /*init {
        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                isAutomaticReconnect = true
            }

            mqttClient.connect(options)
            println("Conectado exitosamente al broker MQTT.")

            topicsToSubscribe.forEach { topic ->
                subscribeTopic(topic)
            }

        } catch (e: MqttException) {
            throw RuntimeException("Error al conectar con el broker.", e)
        }
    }
*/
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
            println("Intentando suscribirse al tópico: $topic")

            mqttClient.subscribe(topic) { receivedTopic, message ->
                val payload = String(message.payload)
                println("Mensaje recibido en '$receivedTopic': $payload")
                handleIncomingMessage(receivedTopic, payload)
            }

            println("Suscripción exitosa al tópico: $topic")

        } catch (e: MqttException) {
            println("ERROR: No se pudo suscribir a MQTT - ${e.message}")
            e.printStackTrace()
        }
    }

    // Manejo modular de cada mensaje según el tópico
    private fun handleIncomingMessage(topic: String, payload: String) {
        when (topic) {
            "unq-button" -> {
                if (payload.equals("wasPressed", ignoreCase = true)) {
                    println("Apertura de puerta detectada - Activando alarma")
                    webSocketHandler.handlePhysicalButtonPress()
                }
            }

            "LEDctrl" -> {
                if (payload == "toggle") {
                    println("Comando toggle para LED recibido")
                    webSocketHandler.sendMessage("LED_TOGGLE")
                    //webSocketHandler.sendLedStatusUpdate()
                } else {
                    println("Comando no reconocido en LEDctrl: $payload")
                }
            }

            "unq-temperature" -> {
                println("Temperatura recibida: $payload")
                //webSocketHandler.sendMessage("TEMP: $payload")
                webSocketHandler.sendTemperatureUpdate(payload)
            }

            else -> {
                println("Tópico no manejado: $topic, mensaje: $payload")
            }
        }
    }
}
