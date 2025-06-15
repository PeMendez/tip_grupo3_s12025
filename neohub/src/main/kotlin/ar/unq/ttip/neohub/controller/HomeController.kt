package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.Room
import ar.unq.ttip.neohub.service.HomeService
import ar.unq.ttip.neohub.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/home")
class HomeController(
    private val homeService: HomeService,
    private val userService: UserService
) {

    @GetMapping
    fun getHome(@AuthenticationPrincipal userDetails: UserDetails): HomeDTO? {
        val user = userService.getUserByUsername(userDetails.username)
        return homeService.getHomeForUser(user.id).toDTO()
    }

    @GetMapping("/rooms")
    fun getRooms(@AuthenticationPrincipal userDetails: UserDetails): List<RoomDTO> {
        val user = userService.getUserByUsername(userDetails.username)
        return homeService.getRooms(user).map { it.toDTO() }
    }

    @PostMapping("/rooms")
    fun addRoom(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody roomRequest: RoomRequest
    ): Room {
        val user = userService.getUserByUsername(userDetails.username)
        return homeService.addRoomToHome(user, roomRequest.name)
    }

    /*@DeleteMapping("/rooms/{roomId}")
    fun deleteRoom(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long
    ) {
        val user = userService.getUserByUsername(userDetails.username)
        homeService.removeRoomFromHome(user, roomId)
    }*/

    @PostMapping("/rooms/{roomId}")
    fun removeRoomFromHome(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable roomId: Long
    ) {
        val user = userService.getUserByUsername(userDetails.username)
        homeService.removeRoomFromHome(user, roomId)
    }

    @GetMapping("/{homeId}/members")
    fun getMembers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable homeId: Long
    ): List<UserHomeDTO> {
        return homeService.getAllMembers(homeId);
    }

    @PutMapping("/{homeId}/members/{userId}")
    fun deleteMemberFromHome(
        @PathVariable homeId: Long,
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        homeService.deleteMember(homeId, userId)
        return ResponseEntity.ok().build()
    }

}
