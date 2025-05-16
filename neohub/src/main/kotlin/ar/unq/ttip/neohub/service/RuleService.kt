package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.CreateRuleRequest
import ar.unq.ttip.neohub.dto.RuleDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Operator
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.RuleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RuleService(
    private val ruleRepository: RuleRepository,
    private val deviceRepository: DeviceRepository
) {
    @Transactional
    fun createRule(request: CreateRuleRequest): RuleDTO {
        //Crear objeto regla
        val rule = Rule(name= request.name)
        println("Creado el objeto regla")

        // Obtener todos los IDs de dispositivos (de condiciones y acciones)
        val allDeviceIds = (request.conditions.map { it.deviceId } + request.actions.map { it.deviceId })
            .distinct() // Eliminamos duplicados

        // Crear un mapa de dispositivos
        val deviceMap = allDeviceIds.associateWith { id ->
            deviceRepository.findById(id).orElseThrow {
                IllegalArgumentException("No se encontró un dispositivo con ID: $id")
            }
        }

        println("Obtenidos los dispositivos")
        val conditions = request.conditions.map {
            val device = deviceMap[it.deviceId]!!
            Condition(
                rule = rule,
                device = device,
                attribute = Attribute.fromString(it.attribute),
                operator = Operator.fromString(it.operator),
                value = it.value
            )
        }
        println("Mapeadas las condiciones")
        val actions = request.actions.map {
            val device = deviceMap[it.deviceId]!!
            println("mapeando para ${device.name}")
            Action(
                rule = rule,
                device = device,
                actionType = it.actionType,
                parameters = it.parameters
            )

        }
        println("Mapeadas las acciones")
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