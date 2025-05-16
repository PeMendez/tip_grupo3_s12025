package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.dto.DeviceDTO
import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface HomeRepository : JpaRepository<Home, Long> {
    fun findByUser(user: User): Home?

    @Query("SELECT d FROM Device d WHERE d.room.home.user.id = :userId AND d.type IN :deviceTypes")
    fun findByUserIdAndTypeIn(
        @Param("userId") userId: Long,
        @Param("deviceTypes") deviceTypes: List<DeviceType>
    ): List<Device>

}