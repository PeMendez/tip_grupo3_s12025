package ar.unq.ttip.neohub.service

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

    fun addDeviceToRoom(roomId: Long, device: Device): Room {
        val room = roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
        val newDevice = device.copy(room = room)
        deviceRepository.save(newDevice)
        return roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
    }
}
