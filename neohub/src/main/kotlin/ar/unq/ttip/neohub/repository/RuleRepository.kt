package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.ruleEngine.Rule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
}
