package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.dto.ActionRequest
import ar.unq.ttip.neohub.dto.ConditionRequest
import ar.unq.ttip.neohub.dto.CreateRuleRequest
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Operator
import ar.unq.ttip.neohub.model.devices.DeviceType
import ar.unq.ttip.neohub.model.devices.OpeningSensor
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import ar.unq.ttip.neohub.model.ruleEngine.ConditionType
import ar.unq.ttip.neohub.model.ruleEngine.TimeCondition
import ar.unq.ttip.neohub.repository.DeviceRepository
import ar.unq.ttip.neohub.repository.RuleRepository
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RuleServiceIntegrationTest @Autowired constructor(
    private val ruleService: RuleService,
    private val deviceRepository: DeviceRepository,
    private val ruleRepository: RuleRepository
) {
    @Test
    fun `crear regla valida`() {
        // Crear y guardar dispositivos
        val temperatureSensor = deviceRepository.save(TemperatureSensor(name = "Temperature Sensor"))
        val fan = deviceRepository.save(SmartOutlet(name = "Fan"))

        // Crear la solicitud de regla
        val request = CreateRuleRequest(
            name = "Temperature Control",
            conditions = listOf(
                ConditionRequest(
                    type = ConditionType.DEVICE.toString(),
                    deviceId = temperatureSensor.id,
                    attribute = "TEMPERATURE",
                    operator = "GREATER_THAN",
                    value = "25"
                )
            ),
            actions = listOf(
                ActionRequest(
                    deviceId = fan.id,
                    actionType = "TURN_ON",
                    parameters = ""
                )
            )
        )

        // Ejecutar el servicio
        val result = ruleService.createRule(request)

        // Verificar los resultados
        assertEquals("Temperature Control", result.name)
        assertEquals(1, result.conditions.size)
        assertEquals(1, result.actions.size)

        val savedRule = ruleRepository.findById(result.id).orElseThrow()
        assertEquals("Temperature Control", savedRule.name)
    }

    @Test
    fun `crear regla valida con condicion de hora`() {
        // Crear y guardar dispositivos
        val fan = deviceRepository.save(SmartOutlet(name = "Fan"))

        // Crear la solicitud de regla
        val request = CreateRuleRequest(
            name = "Morning Routine",
            conditions = listOf(
                ConditionRequest(
                    type = "TIME", // Tipo de condición para TimeCondition
                    deviceId = null, // No aplica para TimeCondition
                    attribute = Attribute.TIME.toString(),
                    operator = "EQUALS",
                    value = "08:00" // Hora en formato HH:mm
                )
            ),
            actions = listOf(
                ActionRequest(
                    deviceId = fan.id,
                    actionType = "TURN_ON",
                    parameters = ""
                )
            )
        )

        // Ejecutar el servicio
        val result = ruleService.createRule(request)

        // Verificar los resultados
        assertEquals("Morning Routine", result.name)
        assertEquals(1, result.conditions.size)
        assertEquals(1, result.actions.size)

        val savedRule = ruleRepository.findById(result.id).orElseThrow()
        assertEquals("Morning Routine", savedRule.name)

        // Verificar que la condición es de tipo TimeCondition
        val timeCondition = savedRule.conditions.first() as TimeCondition
        assertEquals("08:00", timeCondition.value)
        assertEquals(Operator.EQUALS, timeCondition.operator)

        // Verificar que la acción esté asociada al dispositivo correcto
        val action = savedRule.actions.first()
        assertEquals(fan.id, action.device.id)
        assertEquals(ActionType.TURN_ON, action.actionType)
    }


    @Test
    fun `no se puede crear regla con atributo invalido`() {
        // Crear y guardar dispositivo
        val temperatureSensor = deviceRepository.save(TemperatureSensor(name = "Temperature Sensor"))

        // Crear la solicitud con un atributo inválido
        val request = CreateRuleRequest(
            name = "Invalid Attribute Rule",
            conditions = listOf(
                ConditionRequest(
                    type = ConditionType.DEVICE.toString(),
                    deviceId = temperatureSensor.id,
                    attribute = "INVALID_ATTRIBUTE", // Atributo no válido
                    operator = "GREATER_THAN",
                    value = "25"
                )
            ),
            actions = listOf()
        )

        // Ejecutar el servicio y capturar la excepción
        val exception = assertThrows<IllegalArgumentException> {
            ruleService.createRule(request)
        }

        // Verificar el mensaje de la excepción
        assertEquals(
            "Unknown Attribute: INVALID_ATTRIBUTE",
            exception.message
        )
    }
    @Test
    fun `no se puede crear regla con operador invalido`() {
        // Crear y guardar dispositivo
        val temperatureSensor = deviceRepository.save(TemperatureSensor(name = "Temperature Sensor"))

        // Crear la solicitud con un operador inválido
        val request = CreateRuleRequest(
            name = "Invalid Operator Rule",
            conditions = listOf(
                ConditionRequest(
                    type = ConditionType.DEVICE.toString(),
                    deviceId = temperatureSensor.id,
                    attribute = "TEMPERATURE",
                    operator = "INVALID_OPERATOR", // Operador no válido
                    value = "25"
                )
            ),
            actions = listOf()
        )

        // Ejecutar el servicio y capturar la excepción
        val exception = assertThrows<IllegalArgumentException> {
            ruleService.createRule(request)
        }

        // Verificar el mensaje de la excepción
        assertEquals(
            "Operador desconocido: INVALID_OPERATOR",
            exception.message
        )
    }
    @Test
    fun `no se puede crear regla con atributo no soportado por el dispositivo`() {
        // Crear y guardar dispositivo
        val openingSensor = deviceRepository.save(OpeningSensor(name = "Opening Sensor"))

        // Crear la solicitud con un atributo no soportado
        val request = CreateRuleRequest(
            name = "Unsupported Attribute Rule",
            conditions = listOf(
                ConditionRequest(
                    type = ConditionType.DEVICE.toString(),
                    deviceId = openingSensor.id,
                    attribute = "TEMPERATURE", // Atributo no soportado por OpeningSensor
                    operator = "GREATER_THAN",
                    value = "25"
                )
            ),
            actions = listOf()
        )

        // Ejecutar el servicio y capturar la excepción
        val exception = assertThrows<IllegalArgumentException> {
            ruleService.createRule(request)
        }

        // Verificar el mensaje de la excepción
        assertEquals(
            "El atributo ${Attribute.TEMPERATURE} no es soportado por dispositivos del tipo ${DeviceType.OPENING_SENSOR}",
            exception.message
        )
    }
    @Test
    fun `no se puede crear regla con accion no soportada por el dispositivo`() {
        // Crear y guardar dispositivo
        val temperatureSensor = deviceRepository.save(TemperatureSensor(name = "Temperature Sensor"))

        // Crear la solicitud con una acción no soportada por el dispositivo
        val request = CreateRuleRequest(
            name = "Unsupported Action Rule",
            conditions = listOf(
                ConditionRequest(
                    type = ConditionType.DEVICE.toString(),
                    deviceId = temperatureSensor.id,
                    attribute = "TEMPERATURE",
                    operator = "GREATER_THAN",
                    value = "25"
                )
            ),
            actions = listOf(
                ActionRequest(
                    deviceId = temperatureSensor.id,
                    actionType = "TURN_ON", // Acción válida pero no soportada por TemperatureSensor
                    parameters = ""
                )
            )
        )

        // Ejecutar el servicio y capturar la excepción
        val exception = assertThrows<IllegalArgumentException> {
            ruleService.createRule(request)
        }

        // Verificar el mensaje de la excepción
        assertEquals(
            "Acción ${ActionType.TURN_ON} no soportada por dispositivos del tipo ${DeviceType.TEMPERATURE_SENSOR}",
            exception.message
        )
    }
}