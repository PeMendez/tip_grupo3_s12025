package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.handler.MqttWebSocketHandler
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.OpeningSensor
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*

@Service
class MqttService(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val webSocketHandler: MqttWebSocketHandler
) {
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
            this.subscribe(unconfiguredTopic)

        } catch (e: MqttException) {
            throw RuntimeException("Error al conectar con el broker.", e)
        }
    }

    fun registerDevice(device: Device) {
        // Agrega al mapa y suscribe al tópico
        topicDeviceMap.putIfAbsent(device.topic, device)
        subscribe(device.topic)
    }

    fun unregisterDevice(device: Device) {
        unsubscribe(device.topic)
        topicDeviceMap.remove(device.topic)
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

    fun handleMqttMessage(topic: String, message: String) {
        println("Received message on topic $topic: $message")
        if(topic.startsWith(unconfiguredTopic)) {
            handleUnconfiguredDevice(message)
        }else {
            val device = topicDeviceMap[topic]
            if(device != null) {
                device.handleIncomingMessage(message)
                when (device){
                    is TemperatureSensor -> {handleTemperatureUpdate(device, message)}
                    is OpeningSensor -> {handleOpeningUpdate(device,message)}
                }
                //aca falta algo
            }else {
                println("ERROR: No se encontró ningún dispositivo para el tópico: $topic")
            }
        }
    }

    fun handleUnconfiguredDevice(message: String) {
        println("Received unconfigured device: $message")
        applicationEventPublisher.publishEvent(UnconfiguredDeviceEvent(message))
    }

    fun handleTemperatureUpdate(sensor: TemperatureSensor, newTemp: String) {
        println("Enviando update de temperatura...")
        webSocketHandler.sendTemperatureUpdate(newTemp,sensor.id)
    }

    fun handleOpeningUpdate(sensor: OpeningSensor, newStatus: String) {
        println("Enviando update de apertura de ${sensor.name}")
        webSocketHandler.sendOpeningUpdate(newStatus, sensor.id)
    }
}

class UnconfiguredDeviceEvent(val message: String): ApplicationEvent(message)