package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.service.UserHomeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController (
    private val userHomeService: UserHomeService
) {

    @GetMapping("/role")
    fun getUserRole(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<String> {
        val role = userHomeService.getRoleForUsername(userDetails.username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in any home")

        return ResponseEntity.ok(role)
    }

    @GetMapping("/current-role/{homeId}")
    fun getUserRoleInCurrentHome(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable homeId: Long): ResponseEntity<String> {
        return userHomeService.getRoleForUserInCurrentHome(userDetails.username, homeId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in current home")
    }
}