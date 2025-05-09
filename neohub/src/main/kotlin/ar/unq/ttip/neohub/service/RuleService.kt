package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.CreateRuleRequest
import ar.unq.ttip.neohub.dto.RuleDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.RuleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RuleService(
    private val ruleRepository: RuleRepository,
    private val deviceRepository: DeviceRepository
) {
    @Transactional
    fun createRule(request: CreateRuleRequest): RuleDTO {
        //Crear objeto regla
        val rule = Rule(name= request.name)
        //Crear las condiciones por cada condicion con su device
        val conditions = request.conditions.map {
            val device = deviceRepository.findById(it.deviceId).orElseThrow{
                IllegalArgumentException("Device with id ${it.deviceId} not found")
            }
            Condition(
                rule = rule,
                device = device,
                attribute = it.attribute,
                operator = it.operator,
                value = it.value
            )
        }
        //Crear las acciones
        val actions = request.actions.map {
            val device = deviceRepository.findById(it.deviceId).orElseThrow{
                IllegalArgumentException("Device with id ${it.deviceId} not found")
            }
            Action(
                rule = rule,
                device = device,
                actionType = it.actionType,
                parameters = it.parameters
            )
        }
        //guardar la regla con sus acciones y condiciones
        rule.conditions.addAll(conditions)
        rule.actions.addAll(actions)
        val savedRule = ruleRepository.save(rule)
        return savedRule.toDTO()
    }

    fun getRulesForDevice(deviceId: Long): List<RuleDTO> {
        val rules = ruleRepository.findRulesByDeviceId(deviceId)
        return rules.map { it.toDTO() }
    }
}