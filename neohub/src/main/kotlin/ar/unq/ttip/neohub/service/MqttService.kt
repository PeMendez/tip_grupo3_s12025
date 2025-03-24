package ar.unq.ttip.neohub.service

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.stereotype.Service

@Service
class MqttService {

    private val brokerUrl = "tcp://broker.hivemq.com:1883" // Cambia esto si usas otro broker
    private val clientId = "spring-boot-mqtt-client"
    private val client = MqttClient(brokerUrl, clientId)

    init {
        val options = MqttConnectOptions()
        options.isCleanSession = true
        client.connect(options)

        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                println("Connection lost: ${cause.message}")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                println("Message received on topic $topic: ${String(message.payload)}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                println("Message delivered!")
            }
        })
    }

    fun publishMessage(topic: String, payload: String) {
        val message = MqttMessage(payload.toByteArray())
        client.publish(topic, message)
    }
}
