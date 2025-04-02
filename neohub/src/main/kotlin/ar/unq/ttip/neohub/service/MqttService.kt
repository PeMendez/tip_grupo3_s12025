package ar.unq.ttip.neohub.service

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.stereotype.Service

@Service
class MqttService {
    private val mqttClient: MqttClient = MqttClient("tcp://test.mosquitto.org:1883", MqttClient.generateClientId())

    init {
        try {
            //mqttClient = MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId())
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
            }
            mqttClient.connect(options)
            println("Conectado exitosamente al broker MQTT de HiveMQ")
        } catch (e: MqttException) {
            throw RuntimeException("Error al conectar con el broker HiveMQ", e)
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
}
