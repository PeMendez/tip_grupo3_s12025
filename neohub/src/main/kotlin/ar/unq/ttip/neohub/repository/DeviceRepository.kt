package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Device
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceRepository : JpaRepository<Device, Long> {
    fun findByRoomIsNull(): List<Device> //Listar dispositivos desconfigurados
}