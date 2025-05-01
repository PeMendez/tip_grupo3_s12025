package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class DeviceService(private val mqttService: MqttService, private val repository: DeviceRepository) {
    fun registerDevice(device: Device) {
        device.configureTopic() // Configura el topic basado en la room.
        mqttService.registerDevice(device) // Delega el registro al MqttService.
    }

    fun unregisterDevice(device: Device) {
        mqttService.unregisterDevice(device) //Delega a MqttService
    }

    fun publishToDevice(device: Device, message: String) {
        mqttService.publish(device.topic, message) // Publica un mensaje al tópico del dispositivo.
    }
}
