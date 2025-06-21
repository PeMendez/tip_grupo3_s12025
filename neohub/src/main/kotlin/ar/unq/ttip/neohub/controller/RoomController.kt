package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.AckResponse
import ar.unq.ttip.neohub.dto.RoomDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.service.DeviceService
import ar.unq.ttip.neohub.service.RoomService
import ar.unq.ttip.neohub.service.RuleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/room")
class RoomController(
    private val roomService: RoomService,
    private val deviceService: DeviceService,
    private val ruleService: RuleService
) {

    @GetMapping("/{roomId}")
    fun getRoomDetails(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long): ResponseEntity<RoomDTO> {
        val room = roomService.getRoomDetails(roomId)
        return ResponseEntity.ok(room.toDTO())
    }

    @GetMapping("/{roomId}/ack")
    fun getDevicesAck(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long): ResponseEntity<AckResponse> {
        return try {
            val ackStatus = roomService.sendAckToDevices(roomId)
            ResponseEntity.ok(AckResponse(success = true, data = ackStatus))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AckResponse(success = false, error = e.message ?: "Error procesando los dispositivos")
            )
        }
    }

    @PostMapping("/{roomId}/addDevice/{deviceId}")
    fun addDeviceToRoom(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long,
        @PathVariable deviceId: Long // PARA AGREGARLO A UN ROOM, YA TIENE QUE EXISTIR.
    ): ResponseEntity<RoomDTO> {
        val room = roomService.addDeviceToRoom(roomId, deviceId)
        ruleService.enableRulesForDevice(deviceId)
        return ResponseEntity.ok(room.toDTO())
    }

    @PostMapping("/{roomId}/removeDevice/{deviceId}")
    fun removeDeviceFromRoom(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long,
        @PathVariable deviceId: Long
    ): ResponseEntity<RoomDTO> {
        ruleService.disableRulesForDevice(deviceId)
        val room = roomService.removeDeviceFromRoom(deviceId, roomId)
        return ResponseEntity.ok(room.toDTO())
    }

    @DeleteMapping ("/{roomId}/resetDevice/{deviceId}")
    fun factoryResetDevice(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long,
        @PathVariable deviceId: Long
    ): ResponseEntity<RoomDTO> {
        ruleService.deleteAllRulesForDevice(deviceId)
        val room = roomService.removeDeviceFromRoom(deviceId, roomId)
        deviceService.deleteDevice(deviceId)
        return ResponseEntity.ok(room.toDTO())
    }


}


