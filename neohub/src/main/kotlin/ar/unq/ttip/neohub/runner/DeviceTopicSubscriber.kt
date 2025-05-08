package ar.unq.ttip.neohub.runner

import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.service.MqttService
import org.springframework.stereotype.Service

@Service
class DeviceTopicSubscriber (
    private val deviceRepository: DeviceRepository,
    private val mqttService: MqttService
) {
    fun subscribeToDeviceTopics() {
        val devices = deviceRepository.findAll()
        devices.forEach { device ->
            mqttService.registerDevice(device)
        }
    }
}