package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface HomeRepository : JpaRepository<Home, Long> {
    fun findByName(name: String): Home?

    @Query("""
        SELECT h 
        FROM Home h 
        JOIN UserHome uh ON uh.home.id = h.id 
        WHERE uh.user.id = :userId
    """)
    fun findByUserId(@Param("userId") userId: Long): List<Home>

    @Query("""
        SELECT h 
        FROM Home h 
        JOIN UserHome uh ON uh.home.id = h.id 
        WHERE uh.user.id = :userId AND uh.role = 'admin'
    """)
    fun findAdminHomeByUserId(@Param("userId") userId: Long): List<Home>

    @Query("""
    SELECT d
    FROM Device d
    JOIN d.room r
    JOIN r.home h
    WHERE h.id = :homeId AND d.type IN :deviceTypes
""")
    fun findDevicesByHomeIdAndType(
        @Param("homeId") homeId: Long,
        @Param("deviceTypes") deviceTypes: List<DeviceType>
    ): List<Device>

    @Query("""
    SELECT DISTINCT r
    FROM Rule r
    JOIN r.conditions rc
    JOIN rc.device d
    JOIN d.room ro
    JOIN ro.home h
    WHERE h.id = :homeId AND r.isEnabled = true
""")
    fun findRulesByHomeId(@Param("homeId") homeId: Long): List<Rule>

}