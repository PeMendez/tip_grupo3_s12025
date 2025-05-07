package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.RoomDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.service.RoomService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/room")
class RoomController(private val roomService: RoomService) {

    @GetMapping("/{roomId}")
    fun getRoomDetails(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long): ResponseEntity<RoomDTO> {
        val room = roomService.getRoomDetails(roomId)
        return ResponseEntity.ok(room.toDTO())
    }

    @PostMapping("/{roomId}/addDevice/{deviceId}")
    fun addDeviceToRoom(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long,
        @PathVariable deviceId: Long // PARA AGREGARLO A UN ROOM, YA TIENE QUE EXISTIR.
    ): ResponseEntity<RoomDTO> {
        val room = roomService.addDeviceToRoom(roomId, deviceId)
        return ResponseEntity.ok(room.toDTO())
    }
}
