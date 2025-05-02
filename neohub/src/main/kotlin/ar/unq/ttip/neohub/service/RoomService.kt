package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.RoomRepository
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val deviceRepository: DeviceRepository,
    private val mqttService: MqttService
) {
    fun getRoomDetails(roomId: Long): Room {
        return roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Habitación no encontrada") }
    }

    fun addDeviceToRoom(roomId: Long, deviceDto: DeviceDTO): Room {

        val room = roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
        /*val newDevice = (instanciar con el factory)
        deviceRepository.save(newDevice)*/

        //debe registrarlo
        //mqttService.registerDevice()
        return room
    }

    fun removeDeviceFromRoom(device: Device, room: Room) {
        room.deviceList.remove(device)
        //des registrar
        //mqttService.unregisterDevice(device)

        // Resetear el cuarto y el tópico
        //device.room = null
        //device.configureTopic()
    }
}
