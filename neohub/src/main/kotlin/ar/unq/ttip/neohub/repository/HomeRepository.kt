package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.User
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.model.ruleEngine.Rule
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

    @Query("""
    SELECT DISTINCT r
    FROM Rule r
    JOIN r.conditions rc
    JOIN rc.device d
    JOIN d.room ro
    JOIN ro.home h
    WHERE h.user.id = :userId and r.isEnabled
""")
    fun findRulesByUser(@Param("userId") userId: Long): List<Rule>
}