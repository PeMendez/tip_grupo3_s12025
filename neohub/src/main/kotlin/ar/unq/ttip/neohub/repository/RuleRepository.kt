package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.ruleEngine.Rule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RuleRepository : JpaRepository<Rule, Long> {
    @Query("""
        SELECT DISTINCT r
        FROM Rule r
        LEFT JOIN r.conditions c
        WHERE c.device.id = :deviceId
    """)
    fun findRulesByDeviceId(deviceId: Long): List<Rule>

    @Query("""
    SELECT DISTINCT r
    FROM Rule r
    JOIN r.conditions c
    JOIN TimeCondition tc ON c.id = tc.id
    WHERE tc.value = :currentTime 
    AND r.isEnabled = true
""")
    fun findRulesWithTimeConditions(@Param("currentTime") currentTime: String): List<Rule>

}
