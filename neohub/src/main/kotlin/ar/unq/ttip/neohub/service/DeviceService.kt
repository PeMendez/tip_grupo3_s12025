package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.repository.DeviceRepository
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DeviceService(
    private val mqttService: MqttService,
    private val repository: DeviceRepository,
    private val factory: DeviceFactory) {

    @EventListener
    fun onUnconfiguredDeviceEvent(event: UnconfiguredDeviceEvent) {
        handleNewDevice(event.message)
    }

    fun handleNewDevice(message: String) {
        val newDevice = factory.createDevice("test1", "smartOutlet")
        saveDevice(newDevice)
    }

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

    @Transactional
    fun saveDevice(device: Device): Device {
        return repository.save(device)
    }

    @Transactional
    fun deleteDevice(deviceId: Long) {
        repository.deleteById(deviceId)
    }

    fun getDeviceById(id: Long): Device? {
        return repository.findById(id).orElse(null)
    }

    fun getUnconfiguredDevices(): List<Device> {
        return repository.findByRoomIsNull()
    }
}
