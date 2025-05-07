package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.RoomRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val deviceRepository: DeviceRepository,
    private val homeRepository: HomeRepository,
    private val mqttService: MqttService,
    private val deviceService: DeviceService
) {
    fun getRoomDetails(roomId: Long): Room {
        return roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Habitación no encontrada") }
    }

    @Transactional
    fun addDeviceToRoom(roomId: Long, deviceId: Long): Room {
        val room = roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
        val newDevice = deviceRepository.findById(deviceId).orElseThrow { RuntimeException("Device not found") }

        room.addDevice(newDevice)

        deviceRepository.save(newDevice)
        roomRepository.save(room)

        deviceService.registerDeviceOnMqtt(newDevice)

        return room
    }

    @Transactional
    fun removeDeviceFromRoom(deviceId: Long, roomId: Long) : Room {
        val targetRoom = roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Room not found") }
        val targetDevice = deviceRepository.findById(deviceId)
            .orElseThrow { RuntimeException("Device not found") }

        // Eliminar el dispositivo de la lista del cuarto
        targetRoom.deviceList.remove(targetDevice)

        // Desregistrar el dispositivo
        mqttService.unregisterDevice(targetDevice)

        // Resetear el cuarto y el tópico
        targetDevice.room = null
        targetDevice.configureTopic()

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
