package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceConfiguration
import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.handler.MqttWebSocketHandler
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.repository.DeviceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*

@Service
class MqttService(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val webSocketHandler: MqttWebSocketHandler,
    private val ruleService: RuleService,
    private val deviceRepository: DeviceRepository,
    private val objectMapper: ObjectMapper=jacksonObjectMapper()
) {
    private val brokerUrl = "tcp://broker.hivemq.com:1883" // "tcp://test.mosquitto.org:1883"
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

    fun publishConfiguration(device: Device, unconfigure: Boolean = false) {
        val topicToConfigure = if (unconfigure) unconfiguredTopic else device.topic
        val topicToPublish = if (!unconfigure) unconfiguredTopic else device.topic
        // Crear la configuración como un objeto
        val config = DeviceConfiguration(
            name = device.name,
            new_topic = topicToConfigure,
            mac_address = device.macAddress!!,
        )

        // Convertir el objeto a JSON
        val jsonMessage = objectMapper.writeValueAsString(config)

        // Publicar el mensaje
        publish(topicToPublish, jsonMessage)
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
            handleUnconfiguredMessage(message)
        } else {
            val device = topicDeviceMap[topic]
            if (device != null) {
                processDeviceMessage(device, message)
            } else {
                println("ERROR: No se encontró ningún dispositivo para el tópico: $topic")
            }
        }
    }

    fun handleUnconfiguredMessage(message: String) {
        try {
            val jsonNode = objectMapper.readTree(message) as? ObjectNode
            if (jsonNode != null && jsonNode.has("new_topic")) {
                val newTopic = jsonNode["new_topic"].asText()
                if (newTopic.isNotEmpty()) {
                    println("Message contains 'new_topic': $newTopic. Ignoring device registration.")
                }
            } else {
                handleUnconfiguredDevice(message)
            }
        } catch (e: Exception) {
            println("Error processing unconfigured message: ${e.message}")
        }
    }

    fun processDeviceMessage(device: Device, message: String) {
        try {
            val jsonNode = objectMapper.readTree(message) as? ObjectNode
            if (jsonNode == null) {
                println("ERROR: Mensaje no es JSON válido.")
                return
            }

            val macAddress = jsonNode["mac_address"]?.asText() ?: ""
            if (macAddress != device.macAddress) {
                println("ERROR: La MAC del mensaje no coincide con el dispositivo.")
                return
            }

            // Procesar comandos o actualizaciones
            val command = jsonNode["command"]?.asText()
            val parameters = jsonNode["parameters"]?.asText()
            val updateHandlers = mapOf(
                "temperature" to { value: String -> device.setAttributeValue(value) }, //termometro
                "status" to { value: String -> device.setAttributeValue(value) }, //puerta
                "brightness" to { value: String -> device.setAttributeValue(value) },
            )
             if (command != null) {
                 if(command.lowercase() == "set_brightness" && parameters != null) {
                    device.handleIncomingMessage(parameters)
                 } else
                    device.handleIncomingMessage(command)
            } else {
                // Procesar actualizaciones
                for ((key, handler) in updateHandlers) {
                    if (jsonNode.has(key)) {
                        handler(jsonNode[key].asText())
                    }
                }
            }

            // Guardar cambios y evaluar reglas
            handleDeviceUpdate(device)
            deviceRepository.save(device)
            println("Se actualizó correctamente ${device.name}")
            evaluateRulesForDevice(device)
        } catch (e: Exception) {
            println("Error procesando el mensaje JSON: ${e.message}")
        }
    }

    private fun evaluateRulesForDevice(device: Device) {
        // Obtener las reglas asociadas al dispositivo
        val rulesDTOs = ruleService.getRulesForDevice(device.id) // Debes llamar al service, no al repo directamente
        val rules = rulesDTOs.map { it.toEntity(deviceRepository) } // Conversión de DTO a entidad

        // Evaluar cada regla
        rules.forEach { rule ->
            val modifiedDevices = rule.evaluateAndExecute()
            if (modifiedDevices.isNotEmpty()) {
                println("Rule '${rule.name}' triggered and actions executed.")

                modifiedDevices.forEach { modifiedDevice ->
                    try {
                        deviceRepository.save(modifiedDevice)
                        println("Estado actualizado guardado para ${modifiedDevice.name}")
                        handleDeviceUpdate(modifiedDevice)
                    } catch (e: Exception) {
                        println("ERROR al guardar ${modifiedDevice.name}: ${e.message}")
                    }
                }
            }
        }
    }

    fun handleUnconfiguredDevice(message: String) {
        println("Received unconfigured device: $message")
        applicationEventPublisher.publishEvent(UnconfiguredDeviceEvent(message))
    }

    private fun handleDeviceUpdate(device: Device) {
        println("Se recibió una actualización para el dispositivo de tipo ${device.type}...")
        when (device.type) {
            DeviceType.TEMPERATURE_SENSOR -> webSocketHandler.sendTemperatureUpdate(device.getAttributeValue(Attribute.TEMPERATURE), device.id)
            DeviceType.OPENING_SENSOR -> webSocketHandler.sendOpeningUpdate(device.getAttributeValue(Attribute.IS_OPEN), device.id)
            DeviceType.SMART_OUTLET -> webSocketHandler.sendSmartOutletUpdate(device.getAttributeValue(Attribute.IS_ON), device.id)
            DeviceType.DIMMER -> webSocketHandler.sendDimmerUpdate(device.getAttributeValue(Attribute.BRIGHTNESS), device.id)
            else -> println("Tipo de dispositivo no manejado: ${device.type}")
        }
    }

}

