package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.RoomDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.repository.HomeRepository
import ar.unq.ttip.neohub.repository.RoomRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val homeRepository: HomeRepository,
    private val mqttService: MqttService,
    private val deviceService: DeviceService,
    private val userService: UserService,
) {
    private val ackTimeoutMillis: Long = 2400

    fun getRoomDetails(roomId: Long): Room {
        return roomRepository.findById(roomId)
            .orElseThrow { RuntimeException("Habitación no encontrada") }
    }

    @Transactional
    fun getRoomDetailsForUser(roomId: Long, username: String): Room {
        val user = userService.getUserByUsername(username)
        /*val room = roomRepository.findById(roomId)
           .orElseThrow { RuntimeException("Habitación no encontrada") } */
        val room = getRoomDetails(roomId)
        if (!room.home!!.getAdmins().contains(user)) { //si no es admin
            room.deviceList = room.deviceList.filter { device ->
                device.visible || device.owner!!.username == username
            }.toMutableList()
        }

        return room
    }


    fun sendAckToDevices(roomId: Long, username: String): Map<Long, Boolean> {
        val user = userService.getUserByUsername(username)
        val room = getRoomDetails(roomId)

        if (!room.home!!.getAdmins().contains(user)) { //si no es admin
            room.deviceList = room.deviceList.filter { device ->
                device.visible || device.owner!!.username == username
            }.toMutableList()
        }

        val futures = room.deviceList.associate { device ->
            val future = mqttService.registerAckFuture(device.id)
            // mqttService.publish("${device.topic}/command", """{"command": "ack"}""")
            deviceService.sendCommand(device.id, "ack")
            device.id to future
        }

        return futures.mapValues { (_, future) ->
            try {
                future.get(ackTimeoutMillis, TimeUnit.MILLISECONDS) // Timeout de 5 segundos
            } catch (e: TimeoutException) {
                false
            }
        }
    }


    @Transactional
    fun addDeviceToRoom(roomId: Long, deviceId: Long, username: String): Room {
        val user = userService.getUserByUsername(username)
        val room = getRoomDetails(roomId)
        val device = deviceService.getDeviceEntityById(deviceId)

        device.owner = user

        room.addDevice(device)

        mqttService.publishConfiguration(device)

        deviceService.saveDevice(device)
        roomRepository.save(room)
        deviceService.registerDeviceOnMqtt(device)
        return room
    }

    @Transactional
    fun removeDeviceFromRoom(deviceId: Long, roomId: Long) : Room {

        val targetRoom = getRoomDetails(roomId)
        val targetDevice = deviceService.getDeviceEntityById(deviceId)

        targetDevice.owner = null;

        // Eliminar el dispositivo de la lista del cuarto
        mqttService.publishConfiguration(targetDevice, unconfigure = true)
        mqttService.unregisterDevice(targetDevice)

        targetRoom.removeDevice(targetDevice)
        // Desregistrar el dispositivo
        deviceService.saveDevice(targetDevice)
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
