package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.DeviceFactory
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.RoomRepository
import org.springframework.stereotype.Service

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val deviceRepository: DeviceRepository,
    private val mqttService: MqttService,
    private val deviceFactory: DeviceFactory,
    private val deviceService: DeviceService,
) {
    fun getRoomDetails(roomId: Long): Room {
        return roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Habitación no encontrada") }
    }

    fun addDeviceToRoom(roomId: Long, deviceDto: DeviceDTO): Room {
        val room = roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
        //no se puede instanciar devices, para eso esta el factory
        val newDevice = deviceFactory.createDevice(deviceDto.name, deviceDto.type)
        //val newDevice = deviceRepository.findById(deviceDto.id).orElseThrow { RuntimeException("Device not found") }
        newDevice.room=room
        room.addDevice(newDevice)

        //debe registrarlo
        deviceService.registerDevice(newDevice)
        deviceRepository.save(newDevice)
        roomRepository.save(room)
        return room
    }

    fun removeDeviceFromRoom(device: Device, room: Room) {
        val targetRoom = roomRepository.findById(room.id).orElseThrow { RuntimeException("Room not found") }
        val targetDevice = deviceRepository.findById(device.id).orElseThrow { RuntimeException("Device not found") }
        // aca habria que traerlo del dto
        targetRoom.deviceList.remove(targetDevice)
        //des registrar
        mqttService.unregisterDevice(targetDevice)

        // Resetear el cuarto y el tópico
        targetDevice.room = null
        targetDevice.configureTopic()
        deviceRepository.save(targetDevice)
        roomRepository.save(room)
    }

    fun addNewRoom(room: Room): Room{
        val newRoom = roomRepository.save(room)
        return newRoom
    }
}
