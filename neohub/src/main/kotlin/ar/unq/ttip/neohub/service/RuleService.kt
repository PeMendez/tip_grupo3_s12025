package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.*
import ar.unq.ttip.neohub.model.ActionType
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
            val attribute = Attribute.fromString(it.attribute)
            val operator = Operator.fromString(it.operator)

            Condition(
                rule = rule,
                device = device,
                attribute = attribute,
                operator = operator,
                value = it.value
            )
        }
        conditions.forEach { it.validate() }
        println("Mapeadas las condiciones")
        val actions = request.actions.map {
            val device = deviceMap[it.deviceId]!!
            println("mapeando para ${device.name}")
            Action(
                rule = rule,
                device = device,
                actionType = ActionType.fromString(it.actionType),
                parameters = it.parameters
            )
        }
        actions.forEach { it.validate() }
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

    fun getEnableRulesForDevice(deviceId: Long): List<RuleDTO> {
        val rules = ruleRepository.findRulesByDeviceId(deviceId).filter { it.isEnabled }
        return rules.map { it.toDTO() }
    }

    @Transactional
    fun deleteAllRulesForDevice(deviceId: Long) {
        val rules = ruleRepository.findRulesByDeviceId(deviceId)
        ruleRepository.deleteAll(rules)
    }

    @Transactional
    fun disableRulesForDevice(deviceId: Long) {
        val rules = ruleRepository.findRulesByDeviceId(deviceId)
        rules.forEach { it.isEnabled = false }
        ruleRepository.saveAll(rules)
    }

    @Transactional
    fun enableRulesForDevice(deviceId: Long) {
        val rules = ruleRepository.findRulesByDeviceId(deviceId)

        rules.forEach { rule ->
            if (allDevicesAreAvailable(rule, deviceId)) {
                rule.isEnabled = true
                ruleRepository.save(rule)
            }
        }
    }

    fun allDevicesAreAvailable(rule: Rule, deviceId: Long): Boolean {
        val allDeviceIds = (rule.actions.map { it.device.id } + rule.conditions.map { it.device.id }).toSet()
        val otherDeviceIds = allDeviceIds.filter { it != deviceId }

        return otherDeviceIds.all { id ->
            deviceRepository.findById(id)
                .map { it.room != null } //usé el id de room porque en los device no saben responder si están ok
                .orElse(false)
        }
    }
}