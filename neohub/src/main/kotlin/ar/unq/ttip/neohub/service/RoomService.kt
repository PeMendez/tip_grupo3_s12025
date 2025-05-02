package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.repository.RoomRepository
import ar.unq.ttip.neohub.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val deviceRepository: DeviceRepository
) {
    fun getRoomDetails(roomId: Long): Room {
        return roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Habitación no encontrada") }
    }

    fun addDeviceToRoom(roomId: Long, deviceDto: DeviceDTO): Room {
        val room = roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
        val newDevice = Device(
            name = deviceDto.name,
            type = deviceDto.type,
            room = room
        )
        deviceRepository.save(newDevice)
        return room
    }
}
