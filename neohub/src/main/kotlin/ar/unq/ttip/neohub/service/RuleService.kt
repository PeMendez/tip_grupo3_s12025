package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.CreateRuleRequest
import ar.unq.ttip.neohub.dto.RuleDTO
import ar.unq.ttip.neohub.dto.toDTO
import ar.unq.ttip.neohub.dto.toEntity
import ar.unq.ttip.neohub.model.*
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.DeviceCondition
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.model.ruleEngine.TimeCondition
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.RuleRepository
import jakarta.transaction.Transactional
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class RuleService(
    private val ruleRepository: RuleRepository,
    private val deviceRepository: DeviceRepository,
    private val deviceService: DeviceService,
    private val mqttService: MqttService,
    private val roomService: RoomService
) {
    @Transactional
    fun unregisterAllDevicesForUser(user: User)  {
        deviceService.getAllDevicesForUser(user).forEach { device ->
            disableRulesForDevice(device.id)
/*            mqttService.publishConfiguration(device, unconfigure = true)
            mqttService.unregisterDevice(device)*/
            val room = device.room
            if (room != null) {
                roomService.removeDeviceFromRoom(device.id, room.id)
            }
        }
    }

    @Transactional
    fun createRule(request: CreateRuleRequest): RuleDTO {
        //Crear objeto regla
        val rule = Rule(name= request.name)
        println("Creado el objeto regla")

        // Obtener todos los IDs de dispositivos de condiciones que tienen un deviceId
        val conditionDeviceIds = request.conditions
            .mapNotNull { it.deviceId } // Ignorar condiciones que no tienen deviceId

        // Obtener todos los IDs de dispositivos de acciones
        val actionDeviceIds = request.actions
            .map { it.deviceId } // Suponemos que las acciones siempre tienen un deviceId

        // Combinar y eliminar duplicados
        val allDeviceIds = (conditionDeviceIds + actionDeviceIds).distinct()

        // Crear un mapa de dispositivos
        val deviceMap = allDeviceIds.associateWith { id ->
            deviceRepository.findById(id).orElseThrow {
                IllegalArgumentException("No se encontró un dispositivo con ID: $id")
            }
        }
        println("Obtenidos los dispositivos")

        val conditions = request.conditions.map {
            when (it.type) { // `type` indica el tipo de condición en el DTO (e.g., "DEVICE" o "TIME")
                "DEVICE" -> {
                    val device = deviceRepository.findById(it.deviceId!!).orElseThrow {
                        IllegalArgumentException("No se encontró el dispositivo con ID: ${it.deviceId}")
                    }
                    DeviceCondition(
                        rule = rule,
                        device = device,
                        attribute = Attribute.fromString(it.attribute),
                        operator = Operator.fromString(it.operator),
                        value = it.value
                    )
                }
                "TIME" -> {
                    TimeCondition(
                        rule = rule,
                        operator = Operator.fromString(it.operator),
                        value = it.value
                    )
                }
                else -> throw IllegalArgumentException("Tipo de condición no soportado: ${it.type}")
            }
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
        // Filtrar las condiciones con un dispositivo asociado
        val conditionDeviceIds = rule.conditions
            .filterIsInstance<DeviceCondition>() // Considera solo las condiciones basadas en dispositivos (CUESTIONABLE)
            .map { it.device.id }

        // Obtener los IDs de dispositivos de las acciones
        val actionDeviceIds = rule.actions
            .map { it.device.id }

        // Combinar y eliminar duplicados
        val allDeviceIds = (conditionDeviceIds + actionDeviceIds).toSet()

        // Excluir el dispositivo dado
        val otherDeviceIds = allDeviceIds.filter { it != deviceId }

        // Verificar si todos los dispositivos restantes están disponibles
        return otherDeviceIds.all { id ->
            deviceRepository.findById(id)
                .map { it.room != null } // Considerar el dispositivo como disponible si tiene un room asignado
                .orElse(false)
        }
    }

    fun executeRuleActions(rule: Rule) {
        rule.actions.forEach { action ->
            deviceService.sendCommand(action.device.id, action.actionType.name.lowercase(), action.parameters)
        }
    }

    @EventListener
    fun onRuleTriggered(event: RuleTriggeredEvent) {
        println("Se dispara una regla para el dispositivo: ${event.device.name}")
        evaluateRulesForDevice(event.device)
    }

    fun evaluateRulesForDevice(device: Device) {
        // Obtener las reglas asociadas al dispositivo
        val rulesDTOs = getEnableRulesForDevice(device.id)
        if (rulesDTOs.isEmpty()) return

        val rules = rulesDTOs.map { it.toEntity(deviceRepository) } // Conversión de DTO a entidad

        // Evaluar cada regla
        rules.forEach { rule ->
            val modifiedDevices = rule.evaluateAndExecute()
            if (modifiedDevices.isNotEmpty()) {
                executeRuleActions(rule)
                println("Rule '${rule.name}' triggered and actions executed.")
                modifiedDevices.forEach { modifiedDevice ->
                    try {
                        deviceRepository.save(modifiedDevice)
                        println("Estado actualizado guardado para ${modifiedDevice.name}")
                        mqttService.handleDeviceUpdate(modifiedDevice)
                    } catch (e: Exception) {
                        println("ERROR al guardar ${modifiedDevice.name}: ${e.message}")
                    }
                }
            }
        }
    }
}