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
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Service
class MqttService(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val webSocketHandler: MqttWebSocketHandler,
    private val deviceRepository: DeviceRepository,
    private val notificationService: PushNotificationService,
    private val objectMapper: ObjectMapper=jacksonObjectMapper(),
    @Value("\${mqtt.broker.url}") private val brokerUrl: String,
) {
    /*@Value("\${mqtt.broker.url}")
    private var brokerUrl: String = System.getenv("MQTT_BROKER_URL")*/
    //private val brokerUrl =  environment.getProperty("MQTT_BROKER_URL")// = System.getenv("MQTT_BROKER_URL")
    private val clientId = "NeoHub-API-" + UUID.randomUUID().toString().substring(0, 8)
    private val mqttClient: MqttClient = MqttClient(brokerUrl, clientId, null)
    private val subscribedTopics = mutableSetOf<String>()
    private val topicDeviceMap = mutableMapOf<String, Device>() //necesito dado el topic encontrar el dispositivo
    private val unconfiguredTopic = "neohub/unconfigured"
    // Mapa para almacenar futuros de acks pendientes
    private val ackFutures = ConcurrentHashMap<Long, CompletableFuture<Boolean>>()

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
        val topicToPublish = if (!unconfigure) unconfiguredTopic else (device.topic + "/command")
        // Crear la configuración como un objeto
        val config = DeviceConfiguration(
            name = device.name,
            new_topic = topicToConfigure,
            mac_address = device.macAddress!!,
        )

        // Convertir el objeto a JSON
        val jsonMessage = objectMapper.writeValueAsString(config)
        println("Publicando configuracion para  ${device.name}...")
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
        println("Dispositivo desconfigurado.")
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
                println("ERROR: Mensaje no es JSON válido o no es un objeto JSON.")
                return
            }

            val macAddress = jsonNode["mac_address"]?.asText()
            // Es crucial que los mensajes de estado del dispositivo siempre incluyan 'mac_address'.
            // Los scripts de Python que hemos desarrollado lo hacen.
            if (macAddress == null || macAddress != device.macAddress) {
                println("ERROR: La MAC del mensaje (${macAddress ?: "N/A"}) no coincide con la del dispositivo ${device.macAddress} o falta.")
                return
            }

            // Manejo de mensajes de tipo ACK
            val responseToCommand = jsonNode["response_to_command"]?.asText()
            if (responseToCommand == "ack") {
                println("Recibido ACK del dispositivo ${device.name} (MAC: ${device.macAddress}).")
                ackFutures[device.id]?.complete(true) // Completar el futuro pendiente
                ackFutures.remove(device.id) // Eliminar el futuro del mapa
                return
            }

            var attributesUpdated = false
            val metadataFields = setOf("mac_address", "device_id", "timestamp") // Campos a ignorar para la actualización de atributos

            // Iterar sobre los campos en el JSON
            jsonNode.fields().forEach { (key, valueNode) ->
                if (key !in metadataFields) {
                    // Llamar a un mét,odo en la entidad 'Device' para que maneje la actualización de este atributo.
                    // Este mét,odo se encargará de la lógica específica del tipo de dispositivo.
                    val valueAsString = valueNode.asText() // Pasamos el valor como String, la entidad lo parseará.
                    val updated = device.handleAttributeUpdate(key, valueAsString) // El mét,odo 'handle attribute update' debería devolver true si el atributo fue reconocido y procesado.
                    if (updated) {
                        attributesUpdated = true
                    }
                }
            }

            if (attributesUpdated) {
                // Guardar cambios en el dispositivo (ya que sus atributos internos fueron modificados)
                deviceRepository.save(device)
                println("Se actualizó correctamente el estado de '${device.name}' (MAC: ${device.macAddress}). Atributos procesados del mensaje: $message")

                //Evaluar reglas si el estado cambió
                applicationEventPublisher.publishEvent(RuleTriggeredEvent(device))
                handleDeviceUpdate(device)
            } else {
                println("No se procesaron atributos actualizables para '${device.name}' (MAC: ${device.macAddress}) del mensaje: $message")
            }

        } catch (e: com.fasterxml.jackson.core.JsonProcessingException) {
            println("Error al parsear el mensaje JSON: ${e.message}")
        } catch (e: Exception) {
            // Es buena idea loguear la traza completa para errores inesperados
            println("Error procesando el mensaje del dispositivo: ${e.message}")
            e.printStackTrace()
        }
    }

    fun registerAckFuture(deviceId: Long): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        ackFutures[deviceId] = future
        return future
    }

    fun handleUnconfiguredDevice(message: String) {
        println("Received unconfigured device: $message")
        applicationEventPublisher.publishEvent(UnconfiguredDeviceEvent(message))
    }

    fun handleDeviceUpdate(device: Device) {
        println("Se recibió una actualización para el dispositivo de tipo ${device.type}...")
        when (device.type) {
            DeviceType.OPENING_SENSOR -> {
                //Ver si toda esta logica conviene extraerla a otro lado.
                val isOpen = device.getAttributeValue(Attribute.IS_OPEN)
                webSocketHandler.sendOpeningUpdate(isOpen, device.id)
                val isOpenBoolean = device.getAttributeValue(Attribute.IS_OPEN) as? Boolean
                if (isOpenBoolean != null) {
                    val message = if (isOpenBoolean) {
                        "Sensor ${device.name} está ahora abierto!"
                    } else {
                        "Sensor ${device.name} está ahora cerrado!"
                    }
                    notificationService.sendPushNotification("Sensor ${device.name}", message)
                } else {
                    println("El atributo IS_OPEN no es un booleano.")
                }

            }
            DeviceType.TEMPERATURE_SENSOR -> webSocketHandler.sendTemperatureUpdate(device.getAttributeValue(Attribute.TEMPERATURE), device.id)
            DeviceType.SMART_OUTLET -> webSocketHandler.sendSmartOutletUpdate(device.getAttributeValue(Attribute.IS_ON), device.id)
            DeviceType.DIMMER -> webSocketHandler.sendDimmerUpdate(device.getAttributeValue(Attribute.BRIGHTNESS), device.id)
        }
    }

}

