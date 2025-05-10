package ar.unq.ttip.neohub.dto

import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.repository.DeviceRepository

data class RuleDTO(
    val id: Long,
    val name: String,
    val conditions: List<ConditionDTO>,
    val actions: List<ActionDTO>
)

fun Rule.toDTO(): RuleDTO {
    return RuleDTO(
        id,
        name,
        conditions.map { it.toDTO() },
        actions.map { it.toDTO() }
    )
}

fun RuleDTO.toEntity(deviceRepository: DeviceRepository): Rule {
    // Crear la entidad Rule
    val rule = Rule(
        id = this.id,
        name = this.name,
        conditions = mutableListOf(), // Inicialmente vacías
        actions = mutableListOf()    // Inicialmente vacías
    )

    // Resolver dispositivos necesarios para condiciones y acciones
    val deviceMap = (this.conditions.map { it.deviceId } + this.actions.map { it.deviceId })
        .distinct()
        .associateWith { id ->
            deviceRepository.findById(id).orElseThrow {
                IllegalArgumentException("No se encontró un dispositivo con ID: $id")
            }
        }

    // Crear condiciones y acciones utilizando los métodos `toEntity`
    val conditionEntities = this.conditions.map { conditionDTO ->
        val device = deviceMap[conditionDTO.deviceId]!!
        conditionDTO.toEntity(rule, device) // Pasamos la regla recién creada
    }

    val actionEntities = this.actions.map { actionDTO ->
        val device = deviceMap[actionDTO.deviceId]!!
        actionDTO.toEntity(rule, device) // Pasamos la regla recién creada
    }

    // Asociar condiciones y acciones a la regla
    rule.conditions.addAll(conditionEntities)
    rule.actions.addAll(actionEntities)

    return rule
}

