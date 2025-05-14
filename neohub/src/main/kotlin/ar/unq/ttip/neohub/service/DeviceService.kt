package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.repository.DeviceRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DeviceService(
    private val mqttService: MqttService,
    private val repository: DeviceRepository,
    private val factory: DeviceFactory
) {

    @EventListener
    fun onUnconfiguredDeviceEvent(event: UnconfiguredDeviceEvent) {
        handleNewDevice(event.message)
    }

    fun handleNewDevice(message: String) {
        //hay que cambiar esto para que el nombre y tipo los parsee del mensaje
        val mapper = jacksonObjectMapper()

        val deviceData : DeviceData = mapper.readValue(message, DeviceData::class.java)
        val type = DeviceType.fromString(deviceData.type)
        val name = deviceData.name
        val newDevice = factory.createDevice(name, type)
        repository.save(newDevice)
    }

    fun registerDeviceOnMqtt(device: Device) {
        mqttService.registerDevice(device) // Delega el registro al MqttService.
    }

    fun unregisterDevice(deviceId: Long) {
        val device = repository.findById(deviceId).orElseThrow {
            IllegalArgumentException("Device with ID $deviceId not found.")
        }
        mqttService.unregisterDevice(device)
    }

    fun publishToDevice(deviceId: Long, message: String) {
        val device = repository.findById(deviceId).orElseThrow {
            IllegalArgumentException("Device with ID $deviceId not found.")
        }
        println("publishToDevice: publicando al device ID: $deviceId, mensaje: $message")
        mqttService.publish(device.topic, message)
    }

    @Transactional
    fun saveDevice(device: Device): DeviceDTO {
        val savedDevice = repository.save(device)
        return savedDevice.toDTO()
    }

    @Transactional
    fun deleteDevice(deviceId: Long) {
        repository.deleteById(deviceId)
    }

    fun getDeviceById(id: Long): DeviceDTO {
        val device = repository.findById(id).orElseThrow {
            IllegalArgumentException("Device with ID $id not found.")
        }
        return device.toDTO()
    }

    fun getUnconfiguredDevices(): List<DeviceDTO> {
        return repository.findByRoomIsNull().map { it.toDTO() }
    }
}

data class DeviceData(
    val name: String,
    val type: String
)
data class DeviceMessageRequest(
    val message: String
)