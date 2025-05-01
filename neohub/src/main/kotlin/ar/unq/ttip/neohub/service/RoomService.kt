package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import org.springframework.stereotype.Service

@Service
class RoomService(private val mqttService: MqttService) {

    fun addDeviceToRoom(device: Device, room: Room) {
        device.room = room
        device.configureTopic()
        room.deviceList.add(device)

        // Registrar el dispositivo en el servicio MQTT
        mqttService.registerDevice(device)
    }

    fun removeDeviceFromRoom(device: Device, room: Room) {
        room.deviceList.remove(device)
        mqttService.unregisterDevice(device)

        // Resetear el cuarto y el tópico
        device.room = null
        device.configureTopic()
    }
}
