package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.RoomDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.model.Device
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

    @PostMapping("/{roomId}/addDevice")
    fun addDeviceToRoom(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long,
        @RequestBody deviceDto: DeviceDTO
    ): ResponseEntity<RoomDTO> {
        val room = roomService.addDeviceToRoom(roomId, deviceDto)
        return ResponseEntity.ok(room.toDTO())
    }
}
