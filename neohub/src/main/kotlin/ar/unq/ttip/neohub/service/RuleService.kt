package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.repository.RuleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RuleService(
    private val ruleRepository: RuleRepository,
) {
    @Transactional
    fun addRule(rule: Rule) {

    }
}