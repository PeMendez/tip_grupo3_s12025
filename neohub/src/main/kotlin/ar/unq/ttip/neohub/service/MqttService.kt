package ar.unq.ttip.neohub.service

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.stereotype.Service

@Service
class MqttService(/*private val webSocketHandler: MqttWebSocketHandler*/) {
    private val brokerUrl = "tcp://test.mosquitto.org:1883"
    private val clientId = "APIListener"
    private val topic = "unq-button"

    private val mqttClient: MqttClient = MqttClient(brokerUrl, clientId, null)

    init {
        try {
            //mqttClient = MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId())
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
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
        try{
            mqttClient.subscribe(topic){ receivedTopic, message -> //funcion callback para cuando se recibe un msj
                val payload = String(message.payload)
                println("Mensaje recibido en '$receivedTopic': $payload")

                //aqui tambien lo tiene que reenviar a websocket para que lo vea el front
                //webSocketHandler.sendMessage("MQTT $topic: $payload")
            }
            println("Suscrito exitosamente al tópico: $topic")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}
