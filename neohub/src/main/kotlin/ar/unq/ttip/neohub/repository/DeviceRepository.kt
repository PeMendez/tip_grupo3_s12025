package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Device
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository : JpaRepository<Device, Long>{
    fun findByRoomIsNull(): List<Device>

    fun findByRoomIsNotNull(): List<Device>
    @Query("""SELECT COUNT (d) FROM Device d WHERE d.room IS NOT NULL""")
    fun countConfiguredDevices(): Long

    fun findByOwnerId(ownerId: Long): List<Device>

    fun findByMacAddress(macAddress: String): Device?
}