package ar.unq.ttip.neohub.runner

import ar.unq.ttip.neohub.repository.RuleRepository
import ar.unq.ttip.neohub.service.RuleTriggeredByTimeEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class TimeConditionChecker(
    private val ruleRepository: RuleRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Scheduled(cron = "0 * * * * *")
    fun checkTimeConditions() {
        val currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString()
        val rules = ruleRepository.findRulesWithTimeConditions(currentTime)
        println("Encontradas ${rules.size} reglas...")
        rules.forEach { rule ->
            println("Disparando regla: ${rule.name}")
            applicationEventPublisher.publishEvent(RuleTriggeredByTimeEvent(rule))
        }
    }
}