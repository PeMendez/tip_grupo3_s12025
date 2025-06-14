package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceCommand
import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.DeviceData
import ar.unq.ttip.neohub.dto.DeviceUpdateDTO
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Role
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.repository.DeviceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.security.core.userdetails.UserDetails
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

    fun handleNewDevice(message: String): DeviceDTO {
        val deviceData : DeviceData = objectMapper.readValue(message, DeviceData::class.java)
        val type = DeviceType.fromString(deviceData.type)
        val name = deviceData.name

        val newDevice = factory.createDevice(name, type)
        newDevice.macAddress=(deviceData.mac_address)

        repository.save(newDevice)
        return newDevice.toDTO()
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

    fun sendCommand(deviceId: Long, commandString: String, parameter: String = "") {
        val device = repository.findById(deviceId).orElseThrow {
            IllegalArgumentException("Device with ID $deviceId not found.")
        }
        // Validar que el dispositivo tiene la información necesaria
        val currentDeviceTopic = device.topic
        if (currentDeviceTopic.isBlank()) {
            throw IllegalStateException("Device with ID $deviceId has an empty or blank topic assigned.")
        }
        val deviceMacAddress = device.macAddress ?: throw IllegalStateException("Device with ID $deviceId has no MAC address.")

        // Crear el payload del comando
        val commandPayload = DeviceCommand(
            mac_address = deviceMacAddress,
            command = commandString,
            parameters = parameter
        )

        val commandTopic = "$currentDeviceTopic/command"
        // Convertir el payload a JSON
        val jsonCommandMessage = objectMapper.writeValueAsString(commandPayload)

        println("sendCommand: publicando comando JSON al device ID: $deviceId, Tópico: $commandTopic, Mensaje: $jsonCommandMessage")

        // Usar mqttService para publicar el mensaje JSON
        mqttService.publish(commandTopic, jsonCommandMessage)
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
    fun getDeviceEntityById(id: Long): Device {
        val device = repository.findById(id).orElseThrow {
            IllegalArgumentException("Device with ID $id not found.")
        }
        return device
    }

    fun getAllDevices(): List<DeviceDTO> {
        val devices = repository.findAll()
        return devices.map { it.toDTO() }
    }

    fun getUnconfiguredDevices(): List<DeviceDTO> {
        return repository.findByRoomIsNull().map { it.toDTO() }
    }

    fun getConfiguredDevices(role: String, username: String): List<DeviceDTO> {

        if(role == "ADMIN"){
            return repository.findByRoomIsNotNull().map {it.toDTO() }
        } else {
            return repository.findByRoomIsNotNull().filter { device ->
                device.visible || device.owner!!.username == username
            }.map {it.toDTO() }
        }
    }

    fun countConfiguredDevices(): Long{
        val count = repository.countConfiguredDevices()
        println("countConfiguredDevices: $count")
        return count
    }

    fun updateDevice(deviceId: Long, update: DeviceUpdateDTO, username: String): DeviceDTO {
        val device = repository.findById(deviceId).orElseThrow {
            throw IllegalArgumentException("Dispositivo no encontrado")
        }

        if (device.owner!!.username != username) {
            throw IllegalAccessException("No sos el dueño del dispositivo")
        }

        update.name?.let { device.name = it }
        update.visible?.let { device.visible = it }

        val updated = repository.save(device)
        return updated.toDTO()
    }

}

