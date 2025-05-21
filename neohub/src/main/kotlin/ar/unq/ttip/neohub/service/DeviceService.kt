package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceCommand
import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.DeviceData
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.DeviceType
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
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {

    @EventListener
    fun onUnconfiguredDeviceEvent(event: UnconfiguredDeviceEvent) {
        handleNewDevice(event.message)
    }

    fun handleNewDevice(message: String) {
        val deviceData : DeviceData = objectMapper.readValue(message, DeviceData::class.java)
        val type = DeviceType.fromString(deviceData.type)
        val name = deviceData.name

        val newDevice = factory.createDevice(name, type)
        newDevice.macAddress=(deviceData.mac_address)

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

    fun publishToDevice(deviceId: Long, commandString: String) {
        val device = repository.findById(deviceId).orElseThrow {
            IllegalArgumentException("Device with ID $deviceId not found.")
        }
        // Validar que el dispositivo tiene la información necesaria
        val currentDeviceTopic = device.topic ?: throw IllegalStateException("Device with ID $deviceId has no assigned topic for commands.")
        if (currentDeviceTopic.isBlank()) {
            throw IllegalStateException("Device with ID $deviceId has an empty or blank topic assigned.")
        }
        val deviceMacAddress = device.macAddress ?: throw IllegalStateException("Device with ID $deviceId has no MAC address.")

        // Crear el payload del comando
        val commandPayload = DeviceCommand(
            mac_address = deviceMacAddress,
            command = commandString
        )

        // Convertir el payload a JSON
        val jsonCommandMessage = objectMapper.writeValueAsString(commandPayload)

        println("publishToDevice: publicando comando JSON al device ID: $deviceId, Tópico: $currentDeviceTopic, Mensaje: $jsonCommandMessage")

        // Usar tu mqttService para publicar el mensaje JSON
        mqttService.publish(currentDeviceTopic, jsonCommandMessage)
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

    fun getAllDevices(): List<DeviceDTO> {
        val devices = repository.findAll()
        return devices.map { it.toDTO() }
    }

    fun getUnconfiguredDevices(): List<DeviceDTO> {
        return repository.findByRoomIsNull().map { it.toDTO() }
    }
}

