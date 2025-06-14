package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.dto.DeviceMessageRequest
import ar.unq.ttip.neohub.dto.DeviceUpdateDTO
import ar.unq.ttip.neohub.service.DeviceService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/devices")
class DeviceController(
    private val deviceService: DeviceService
) {

    // Endpoint para obtener un dispositivo por ID
    @GetMapping("/{deviceId}")
    fun getDevice(@PathVariable deviceId: Long): DeviceDTO? {
        return deviceService.getDeviceById(deviceId)
    }

    @GetMapping("/devices")
    fun getAllDevices(): List<DeviceDTO> {
        return deviceService.getAllDevices()
    }

    @GetMapping("/user_devices")
    fun getAllUserDevices(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): List<DeviceDTO> {
        return emptyList()
    }

    // Endpoint para obtener dispositivos desconfigurados
    @GetMapping("/unconfigured")
    fun getUnconfiguredDevices(): List<DeviceDTO> {
        return deviceService.getUnconfiguredDevices()
    }

    @GetMapping("/configured")
    fun getConfiguredDevices(): List<DeviceDTO> {
        return deviceService.getConfiguredDevices()
    }

    @GetMapping("/configuredCount")
    fun getConfiguredDevicesCount(): Long {
        return deviceService.countConfiguredDevices()
    }

    // Endpoint para eliminar un dispositivo
    @DeleteMapping("/{deviceId}")
    fun deleteDevice(@PathVariable deviceId: Long) {
        deviceService.deleteDevice(deviceId)
    }

    @PostMapping("/manual")
    fun createManualDevice(@RequestBody message: String): DeviceDTO {
        return deviceService.handleNewDevice(message)
    }

    @PostMapping("/message/{deviceId}")
    fun messageToDevice(
        @PathVariable deviceId: Long,
        @RequestBody messageRequest: DeviceMessageRequest
    ): ResponseEntity<Void>{
        //editar la clase DeviceMessageRequest si necesitamos que mande mas cosas
        deviceService.sendCommand(deviceId, messageRequest.message, messageRequest.parameter)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{deviceId}")
    fun updateDevice(
        @PathVariable deviceId: Long,
        @RequestBody update: DeviceUpdateDTO,
        @AuthenticationPrincipal userDetails: UserDetails
    ): DeviceDTO {
        val username = userDetails.username
        return deviceService.updateDevice(deviceId, update, username)
    }
}
