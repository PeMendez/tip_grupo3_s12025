package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.RoomDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.RoomRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val deviceRepository: DeviceRepository,
    private val homeRepository: HomeRepository,
    private val mqttService: MqttService,
    private val deviceService: DeviceService,
) {
    private val ackTimeoutMillis: Long = 5000

    fun getRoomDetails(roomId: Long): Room {
        return roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Habitación no encontrada") }
    }

    fun sendAckToDevices(roomId: Long): Map<Long, Boolean> {
        val room = getRoomDetails(roomId)
        val devices = room.deviceList

        if (devices.isEmpty()) {
            throw RuntimeException("No hay dispositivos asociados a la habitación con ID: $roomId")
        }

        val deviceStatus = mutableMapOf<Long, Boolean>()
        val futures = mutableMapOf<Long, CompletableFuture<Boolean>>()

        devices.forEach { device ->
            val future = CompletableFuture<Boolean>()
            futures[device.id] = future

             // Enviar el comando de ACK al dispositivo
            try {
                deviceService.sendCommand(device.id, "ack")
            } catch (e: Exception) {
                future.complete(false) // Si el envío falla, marcar como no respondido
            }
        }

        // Esperar los ACKs con timeout
        futures.forEach { (deviceId, future) ->
            try {
                deviceStatus[deviceId] = future.get(ackTimeoutMillis, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                deviceStatus[deviceId] = false // Timeout o error
            }
        }

        return deviceStatus
    }

    @Transactional
    fun addDeviceToRoom(roomId: Long, deviceId: Long): Room {
        val room = roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
        val device = deviceRepository.findById(deviceId).orElseThrow { RuntimeException("Device not found") }

        room.addDevice(device)

        mqttService.publishConfiguration(device)

        deviceRepository.save(device)
        roomRepository.save(room)
        deviceService.registerDeviceOnMqtt(device)
        return room
    }

    @Transactional
    fun removeDeviceFromRoom(deviceId: Long, roomId: Long) : Room {
        val targetRoom = roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Room not found") }
        val targetDevice = deviceRepository.findById(deviceId)
            .orElseThrow { RuntimeException("Device not found") }

        // Eliminar el dispositivo de la lista del cuarto

        mqttService.publishConfiguration(targetDevice, unconfigure = true)
        mqttService.unregisterDevice(targetDevice)

        targetRoom.removeDevice(targetDevice)
        // Desregistrar el dispositivo
        deviceRepository.save(targetDevice)
        return roomRepository.save(targetRoom)
    }

    @Transactional
    fun addNewRoom(homeId: Long, roomDTO: RoomDTO): RoomDTO {
        val home = homeRepository.findById(homeId).orElseThrow { RuntimeException("Home not found") }
        val room = roomDTO.toEntity()
        home.addRoom(room)
        val newRoom = roomRepository.save(room)
        homeRepository.save(home)
        return newRoom.toDTO()
    }
}
