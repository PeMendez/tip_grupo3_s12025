package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.service.DeviceService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/devices")
class DeviceController(private val deviceService: DeviceService) {

    // Endpoint para crear un nuevo dispositivo
    @PostMapping
    fun createDevice(@RequestBody device: Device): Device {
        return deviceService.saveDevice(device)
    }

    // Endpoint para obtener un dispositivo por ID
    @GetMapping("/{deviceId}")
    fun getDevice(@PathVariable deviceId: Long): Device? {
        return deviceService.getDeviceById(deviceId)
    }

    // Endpoint para obtener dispositivos desconfigurados
    @GetMapping("/unconfigured")
    fun getUnconfiguredDevices(): List<Device> {
        return deviceService.getUnconfiguredDevices()
    }

    // Endpoint para eliminar un dispositivo
    @DeleteMapping("/{deviceId}")
    fun deleteDevice(@PathVariable deviceId: Long) {
        deviceService.deleteDevice(deviceId)
    }
}
