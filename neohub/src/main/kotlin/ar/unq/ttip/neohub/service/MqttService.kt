package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.handler.MqttWebSocketHandler
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.OpeningSensor
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.repository.DeviceRepository
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
    private val webSocketHandler: MqttWebSocketHandler,
    private val ruleService: RuleService,
    private val deviceRepository: DeviceRepository
) {
    private val brokerUrl = "tcp://broker.hivemq.com" // "tcp://test.mosquitto.org:1883"
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
        if (topic.startsWith(unconfiguredTopic)) {
            handleUnconfiguredDevice(message)
        } else {
            val device = topicDeviceMap[topic]
            if (device != null) {
                device.handleIncomingMessage(message)

                // Lógica adicional basada en el tipo de dispositivo
                when (device) {
                    is TemperatureSensor -> handleTemperatureUpdate(device, message)
                    is OpeningSensor -> handleOpeningUpdate(device, message)
                    is SmartOutlet -> handleSmartOutletUpdate(device, message)
                    //Este ahora no iria, porque el mensaje al smart outlet lo publique yo, ya no quiero escucharlo y reaccionar a él
                }

                try{
                    deviceRepository.save(device)
                    println("Se actualizó correctamente ${device.name}")
                } catch (e: Exception) {
                    println("ERROR: No se pudo guardar ${device.name} en la BDD.")
                }

                // Evaluar reglas asociadas al dispositivo
                evaluateRulesForDevice(device)
            } else {
                println("ERROR: No se encontró ningún dispositivo para el tópico: $topic")
            }
        }
    }

    private fun evaluateRulesForDevice(device: Device) {
        // Obtener las reglas asociadas al dispositivo
        val rulesDTOs = ruleService.getRulesForDevice(device.id) // Debes llamar al service, no al repo directamente
        val rules = rulesDTOs.map { it.toEntity(deviceRepository) } // Conversión de DTO a entidad

        // Evaluar cada regla
        rules.forEach { rule ->
            if (rule.evaluate()) {
                println("Rule '${rule.name}' triggered and actions executed.")
            } else {
                println("Rule '${rule.name}' not triggered.")
            }
        }
    }

    fun handleUnconfiguredDevice(message: String) {
        println("Received unconfigured device: $message")
        applicationEventPublisher.publishEvent(UnconfiguredDeviceEvent(message))
    }

    private fun handleTemperatureUpdate(sensor: TemperatureSensor, newTemp: String) {
        println("Se pescó una update de temperatura...")
        webSocketHandler.sendTemperatureUpdate(newTemp,sensor.id)
    }

    private fun handleOpeningUpdate(sensor: OpeningSensor, newStatus: String) {
        println("Se pescó una update de puerta/ventana...")
        webSocketHandler.sendOpeningUpdate(newStatus, sensor.id)
    }

    private fun handleSmartOutletUpdate(boton: SmartOutlet, newStatus: String) {
        println("Se pescó una update de un botón de luz...")
        webSocketHandler.sendSmartOutletUpdate(newStatus, boton.id)
    }
}

class UnconfiguredDeviceEvent(val message: String): ApplicationEvent(message)