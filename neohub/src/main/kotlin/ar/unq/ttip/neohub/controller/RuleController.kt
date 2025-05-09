package ar.unq.ttip.neohub.controller

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.RuleRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rule")
class RuleController(
    private val ruleRepository: RuleRepository,
    private val deviceRepository: DeviceRepository
) {

    @GetMapping("/{deviceId}")
    fun getRulesForDevice(@PathVariable deviceId: Long): List<RuleDTO> {
        return ruleRepository.findAll()
            .filter { rule ->
                rule.conditions.any { it.device.id == deviceId } ||
                        rule.actions.any { it.device.id == deviceId }
            }
            .map { it.toDTO() }
    }

    @PostMapping
    fun createRule(@RequestBody request: CreateRuleRequest): RuleDTO {
        val rule = Rule(name = request.name)

        val conditions = request.conditions.map {
            val device = deviceRepository.findById(it.deviceId).orElseThrow()
            Condition(
                rule = rule,
                device = device,
                attribute = it.attribute,
                operator = it.operator,
                value = it.value
            )
        }
        val actions = request.actions.map {
            val device = deviceRepository.findById(it.deviceId).orElseThrow()
            Action(
                rule = rule,
                device = device,
                actionType = it.actionType,
                parameters = it.parameters
            )
        }

        val savedRule = ruleRepository.save(rule.copy(conditions = conditions, actions = actions))
        return savedRule.toDTO()
    }

    @DeleteMapping("/{ruleId}")
    fun deleteRule(@PathVariable ruleId: Long) {
        ruleRepository.deleteById(ruleId)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleIllegalArgument(ex: IllegalArgumentException): String {
        return ex.message ?: "Solicitud inválida"
    }
}