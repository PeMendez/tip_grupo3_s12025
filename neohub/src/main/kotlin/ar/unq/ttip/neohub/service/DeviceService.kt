package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.repository.DeviceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DeviceService(
    private val mqttService: MqttService,
    private val repository: DeviceRepository,
    private val factory: DeviceFactory,
) {

    @EventListener
    fun onUnconfiguredDeviceEvent(event: UnconfiguredDeviceEvent) {
        handleNewDevice(event.message)
    }

    fun handleNewDevice(message: String) {
        //hay que cambiar esto para que el nombre y tipo los parsee del mensaje
        val mapper = jacksonObjectMapper()

        val deviceData : DeviceData = mapper.readValue<DeviceData>(message, DeviceData::class.java)
        val type = deviceData.type
        val name = deviceData.name
        val newDevice = factory.createDevice(name, type)
        saveDevice(newDevice)
    }

    fun registerDevice(device: Device): Device {
        device.configureTopic() // Configura el topic basado en la room.
        mqttService.registerDevice(device) // Delega el registro al MqttService.
        val savedDevice= repository.save(device)
        return savedDevice
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

data class DeviceData(
    val name: String,
    val type: String
)