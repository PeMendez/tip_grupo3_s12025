package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.Home
import ar.unq.ttip.neohub.model.UserHome
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
    JOIN FETCH r.conditions rc
    LEFT JOIN FETCH rc.device dc
    WHERE r.isEnabled = true
    AND (
        rc.type = 'TIME' OR 
        :isAdmin = true OR 
        rc.id IN (
            SELECT c.id
            FROM Condition c
            JOIN c.device d
            WHERE d.visible = true OR d.owner.username = :username
        )
    )
""")
    fun findRulesWithConditions(
        @Param("username") username: String,
        @Param("isAdmin") isAdmin: Boolean
    ): List<Rule>

    @Query("""
    SELECT DISTINCT r
    FROM Rule r
    JOIN FETCH r.actions a
    JOIN FETCH a.device da
    WHERE (:isAdmin = true OR (da.visible = true OR da.owner.username = :username))
    AND r.isEnabled = true
    AND r IN :rules
""")
    fun fetchActionsForRules(
        @Param("rules") rules: List<Rule>,
        @Param("username") username: String,
        @Param("isAdmin") isAdmin: Boolean
    ): List<Rule>

    @Query("""
        SELECT DISTINCT u
        FROM UserHome u 
        WHERE u.home.id = :homeId
    """)
    fun findAllMembersByHome(@Param("homeId") homeId: Long): List<UserHome>
}