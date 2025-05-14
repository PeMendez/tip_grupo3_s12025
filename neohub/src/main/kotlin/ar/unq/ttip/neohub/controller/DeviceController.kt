package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.service.DeviceMessageRequest
import ar.unq.ttip.neohub.service.DeviceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/devices")
class DeviceController(private val deviceService: DeviceService) {

    // Endpoint para obtener un dispositivo por ID
    @GetMapping("/{deviceId}")
    fun getDevice(@PathVariable deviceId: Long): DeviceDTO? {
        return deviceService.getDeviceById(deviceId)
    }

    // Endpoint para obtener dispositivos desconfigurados
    @GetMapping("/unconfigured")
    fun getUnconfiguredDevices(): List<DeviceDTO> {
        return deviceService.getUnconfiguredDevices()
    }

    // Endpoint para eliminar un dispositivo
    @DeleteMapping("/{deviceId}")
    fun deleteDevice(@PathVariable deviceId: Long) {
        deviceService.deleteDevice(deviceId)
    }

    @PostMapping("/manual")
    fun createManualDevice(@RequestBody message: String) {
        return deviceService.handleNewDevice(message)
    }

    @PostMapping("/message/{deviceId}")
    fun messageToDevice(
        @PathVariable deviceId: Long,
        @RequestBody messageRequest: DeviceMessageRequest
    ): ResponseEntity<Void>{
        //editar la clase DeviceMessageRequest si necesitamos que mande mas cosas
        deviceService.publishToDevice(deviceId, messageRequest.message)
        println("Respondiendo la request")
        return ResponseEntity.ok().build()
    }
}
