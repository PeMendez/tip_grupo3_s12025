package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.model.devices.DeviceFactory
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
    private val deviceFactory: DeviceFactory,
    private val deviceService: DeviceService,
) {
    fun getRoomDetails(roomId: Long): Room {
        return roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Habitación no encontrada") }
    }

    @Transactional
    fun addDeviceToRoom(roomId: Long, deviceId: Long): Room {
        val room = roomRepository.findById(roomId).orElseThrow { RuntimeException("Room not found") }
        //no se puede instanciar devices, para eso esta el factory
        //val newDevice = deviceFactory.createDevice(deviceId.name, deviceId.type)
        val newDevice = deviceRepository.findById(deviceId).orElseThrow { RuntimeException("Device not found") }
        //newDevice.room=room
        room.addDevice(newDevice)

        //debe registrarlo
        deviceService.registerDevice(newDevice.toDTO())
        deviceRepository.save(newDevice)
        roomRepository.save(room)
        return room
    }

    @Transactional
    fun removeDeviceFromRoom(deviceId: Long, roomId: Long) {
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
        roomRepository.save(targetRoom)
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
