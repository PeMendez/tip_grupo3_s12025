package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.handler.MqttWebSocketHandler
import ar.unq.ttip.neohub.model.Device
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.stereotype.Service
import java.util.*

@Service
class MqttService(private val webSocketHandler: MqttWebSocketHandler) {
    private val brokerUrl = "tcp://test.mosquitto.org:1883"
    private val clientId = "NeoHub-API-" + UUID.randomUUID().toString().substring(0, 8)
    private val mqttClient: MqttClient = MqttClient(brokerUrl, clientId, null)
    private val subscribedTopics = mutableSetOf<String>()
    private val topicDeviceMap = mutableMapOf<String, Device>() //necesito dado el topic encontrar el dispositivo
    private val unconfiguredTopic = "neohub/unconfigured"
    init {
        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                isAutomaticReconnect = true
            }

            mqttClient.connect(options)
            println("Conectado exitosamente al broker MQTT.")

        } catch (e: MqttException) {
            throw RuntimeException("Error al conectar con el broker.", e)
        }
    }

    fun registerDevice(device: Device) {
        // Agrega al mapa y suscribe al tópico
        //topicDeviceMap[device.topic] = device
        topicDeviceMap.putIfAbsent(device.topic, device)
        subscribe(device.topic)
        //updateDeviceTopic(device)
    }

    fun unregisterDevice(device: Device) {
        unsubscribe(device.topic)
        topicDeviceMap.remove(device.topic)
        //updateDeviceTopic(device)
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

    fun subscribe(topic: String, device: Device? = null) {
        if (!subscribedTopics.contains(topic)) {
            try {
                println("Intentando suscribirse al tópico: $topic")
                mqttClient.subscribe(topic) { receivedTopic, message ->
                    val payload = String(message.payload)
                    handleMqttMessage(receivedTopic,payload)
                }
                println("Suscripción exitosa al tópico: $topic")
                subscribedTopics.add(topic)

            } catch (e: MqttException) {
                println("ERROR: No se pudo suscribir a MQTT - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun unsubscribe(topic: String) {
        if (subscribedTopics.contains(topic)) {
            try {
                mqttClient.unsubscribe(topic)
                println("Desuscripción exitosa al tópico: $topic")
                subscribedTopics.remove(topic)
                topicDeviceMap.remove(topic)
            } catch (e: MqttException) {
                println("ERROR: No se pudo desuscribir de MQTT - ${e.message}")
                e.printStackTrace()
            }

        }
    }

    fun updateDeviceTopic(device: Device) {
        // Desuscribir del tópico actual si no es el por defecto
        if (device.topic != unconfiguredTopic) {
            unsubscribe(device.topic)
        }
        device.configureTopic() // Configurar el nuevo tópico
        subscribe(device.topic) // Suscribirse al nuevo tópico
    }

    fun handleMqttMessage(topic: String, message: String) {
        println("Received message on topic $topic: $message")
        if(topic.startsWith(unconfiguredTopic)) {
            handleUnconfiguredDevice(message)
        }else {
            topicDeviceMap[topic]?.handleIncomingMessage(message)
                ?: println("ERROR: No se encontró ningún dispositivo para el tópico: $topic")
        }
    }

    private fun handleUnconfiguredDevice(message: String) {
        println("Received unconfigured device: $message")
        //Acá tendría que crearse un dispositivo nuevo si es que no existia ya.
    }


}
