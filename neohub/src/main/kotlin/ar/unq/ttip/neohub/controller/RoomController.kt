package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.service.RoomService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/room")
class RoomController(private val roomService: RoomService) {

    @PostMapping("/{roomId}/devices")
    fun addDeviceToRoom(
        @PathVariable roomId: Long,
        @RequestBody device: Device
    ): Room {
        return roomService.addDeviceToRoom(roomId, device)
    }
}
