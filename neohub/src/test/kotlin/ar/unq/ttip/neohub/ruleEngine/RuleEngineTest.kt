package ar.unq.ttip.neohub.ruleEngine
import ar.unq.ttip.neohub.model.ActionType
import ar.unq.ttip.neohub.model.Attribute
import ar.unq.ttip.neohub.model.Operator
import ar.unq.ttip.neohub.model.devices.Dimmer
import ar.unq.ttip.neohub.model.devices.OpeningSensor
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
@SpringBootTest
@ActiveProfiles("test")
class RuleEngineTest {
    @Test
    fun `regla creada exitosamente con atributos y operadores validos`() {
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val condition = Condition(
            id = 1,
            rule = null,
            device = temperatureSensor,
            attribute = Attribute.TEMPERATURE,
            operator = Operator.GREATER_THAN,
            value = "25"
        )

        val action = Action(
            id = 1,
            rule = null,
            device = temperatureSensor,
            actionType = ActionType.TURN_ON,
            parameters = ""
        )

        val rule = Rule(
            id = 1,
            name = "Valid Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        assertDoesNotThrow { rule.validateConditions() }
    }

    @Test
    fun `regla falla por atributo invalido`() {
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val invalidAttribute = Attribute.IS_OPEN // Atributo no soportado por un TemperatureSensor

        val condition = Condition(
            id = 1,
            rule = null,
            device = temperatureSensor,
            attribute = invalidAttribute,
            operator = Operator.EQUALS,
            value = "true"
        )

        val rule = Rule(
            id = 1,
            name = "Invalid Attribute Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf()
        )

        val exception = assertThrows<IllegalArgumentException> { rule.validateConditions() }
        assertEquals(
            "El atributo $invalidAttribute no es soportado por dispositivos del tipo ${temperatureSensor.type}",
            exception.message
        )
    }

    @Test
    fun `regla falla por operador invalido`() {
        val window = OpeningSensor(name = "Window")
        val condition = Condition(
            id = 1,
            rule = null,
            device = window,
            attribute = Attribute.IS_OPEN,
            operator = Operator.GREATER_THAN, // Operator válido pero no soportado para IS_OPEN
            value = "25"
        )

        val rule = Rule(
            id = 1,
            name = "Invalid Operator Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf()
        )

        val exception = assertThrows<IllegalArgumentException> { rule.validateConditions() }
        assertEquals(
            "El operador ${condition.operator} no es soportado por el atributo ${condition.attribute} del dispositivo ${window.type}",
            exception.message
        )
    }

    @Test
    fun `regla falla porque el dispositivo no soporta el atributo`() {
        val smartOutlet = SmartOutlet(name = "Smart Outlet")
        val unsupportedAttribute = Attribute.TEMPERATURE // Atributo no soportado por un SmartOutlet

        val condition = Condition(
            id = 1,
            rule = null,
            device = smartOutlet,
            attribute = unsupportedAttribute,
            operator = Operator.GREATER_THAN,
            value = "30"
        )

        val rule = Rule(
            id = 1,
            name = "Unsupported Attribute Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf()
        )

        val exception = assertThrows<IllegalArgumentException> { rule.validateConditions() }
        assertEquals(
            "El atributo $unsupportedAttribute no es soportado por dispositivos del tipo ${smartOutlet.type}",
            exception.message
        )
    }
    @Test
    fun `regla creada exitosamente con temperature sensor y smart outlet`() {
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val smartOutlet = SmartOutlet(name = "Smart Outlet")

        // Configurar condición
        val condition = Condition(
            id = 1,
            rule = null,
            device = temperatureSensor,
            attribute = Attribute.TEMPERATURE,
            operator = Operator.GREATER_THAN,
            value = "25"
        )

        // Configurar acción
        val action = Action(
            id = 1,
            rule = null,
            device = smartOutlet,
            actionType = ActionType.TURN_ON,
            parameters = ""
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Temperature and Outlet Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        assertDoesNotThrow { rule.validateActions() }
    }

    @Test
    fun `regla creada exitosamente con opening sensor y dimmer`() {
        val openingSensor = OpeningSensor(name = "Opening Sensor")
        val dimmer = Dimmer(name = "Dimmer")

        // Configurar condición
        val condition = Condition(
            id = 1,
            rule = null,
            device = openingSensor,
            attribute = Attribute.IS_OPEN,
            operator = Operator.EQUALS,
            value = "true"
        )

        // Configurar acción
        val action = Action(
            id = 1,
            rule = null,
            device = dimmer,
            actionType = ActionType.SET_BRIGHTNESS,
            parameters = "50"
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Opening and Dimmer Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        assertDoesNotThrow { rule.validateActions() }
    }

    @Test
    fun `regla falla porque dispositivo no soporta la accion con temperature sensor y smart outlet`() {
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val smartOutlet = SmartOutlet(name = "Smart Outlet")

        // Configurar condición válida
        val condition = Condition(
            id = 1,
            rule = null,
            device = temperatureSensor,
            attribute = Attribute.TEMPERATURE,
            operator = Operator.GREATER_THAN,
            value = "25"
        )

        // Configurar acción no soportada
        val action = Action(
            id = 1,
            rule = null,
            device = smartOutlet,
            actionType = ActionType.SET_BRIGHTNESS, // Acción no soportada por SmartOutlet
            parameters = "50"
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Invalid Action Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        val exception = assertThrows<IllegalArgumentException> { rule.validateActions() }
        assertEquals(
            "Acción ${action.actionType} no soportada por dispositivos del tipo ${smartOutlet.type}",
            exception.message
        )
    }

    @Test
    fun `regla falla porque dispositivo no soporta la accion con opening sensor y dimmer`() {
        val openingSensor = OpeningSensor(name = "Opening Sensor")
        val dimmer = Dimmer(name = "Dimmer")

        // Configurar condición válida
        val condition = Condition(
            id = 1,
            rule = null,
            device = openingSensor,
            attribute = Attribute.IS_OPEN,
            operator = Operator.EQUALS,
            value = "true"
        )

        // Configurar acción no soportada
        val action = Action(
            id = 1,
            rule = null,
            device = dimmer,
            actionType = ActionType.TURN_ON, // Acción no soportada por Dimmer
            parameters = ""
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Invalid Dimmer Action Rule",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        val exception = assertThrows<IllegalArgumentException> { rule.validateActions() }
        assertEquals(
            "Acción ${action.actionType} no soportada por dispositivos del tipo ${dimmer.type}",
            exception.message
        )
    }


    /*@Test
    fun `cuando la temperatura excede el umbral de la regla, la regla da verdadero y se ejecuta la accion`() {
        // Configurar dispositivos
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val fan = SmartOutlet(name = "Ventilador")

        // Inicializar estado inicial
        temperatureSensor.updateTemperature(30.0) // Usamos el método para actualizar la temperatura
        fan.turnOff()

        // Configurar condición asociada al sensor
        val condition = Condition(
            id = 1,
            rule = null, // Asociado a la regla después
            device = temperatureSensor,
            attribute = Attribute.TEMPERATURE,
            operator =  Operator.GREATER_THAN, // ">"
            value = "25"
        )

        // Configurar acción asociada al aire acondicionado
        val action = Action(
            id = 1,
            rule = null, // Asociado a la regla después
            device = fan,
            actionType = ActionType.TURN_ON,
            parameters = ""
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Temperature Control",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        // Simular evaluación de regla
        val result = rule.evaluateAndExecute()

        // Verificar que la regla se evalúa y la acción se ejecuta
        assertTrue(result, "La regla debería evaluarse como verdadera.")
        assertTrue(fan.isOn, "El aire acondicionado debería estar encendido.")
    }


    @Test
    fun `la temperatura no excede el umbral, asi que la regla no se activa`() {
        // Configurar dispositivos
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val fan = SmartOutlet(name = "Ventilador")

        temperatureSensor.updateTemperature(24.0)
        fan.turnOff()

        // Configurar condición asociada al sensor
        val condition = Condition(
            id = 1,
            rule = null, // Asociado a la regla después
            device = temperatureSensor,
            attribute = Attribute.TEMPERATURE,
            operator = Operator.GREATER_THAN,//">"
            value = "25"
        )

        // Configurar acción asociada al aire acondicionado
        val action = Action(
            id = 1,
            rule = null, // Asociado a la regla después
            device = fan,
            actionType = ActionType.TURN_ON,
            parameters = ""
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Temperature Control",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        // Simular evaluación de regla
        val result = rule.evaluateAndExecute()

        // Verificar que la regla se evalúa y la acción se ejecuta
        assertFalse(result, "La regla debería evaluarse como falsa.")
        assertFalse(fan.isOn, "El aire acondicionado debería estar encendido.") // Esto depende de implementar un estado en `SmartOutlet`
    }

    @Test
    fun `cuando la puerta se abre, el dimmer cambia su brillo al 100 por una regla válida`() {
        // Configurar dispositivos
        val openingSensor = OpeningSensor(name = "Opening Sensor")
        val dimmer = Dimmer(name = "Dimmer")

        // Inicializar estado inicial
        openingSensor.updateStatus(false) // Inicialmente la puerta está cerrada
        dimmer.updateBrightness(50) // El brillo inicial es 50%

        // Configurar condición asociada al sensor
        val condition = Condition(
            id = 1,
            rule = null, // Asociado a la regla después
            device = openingSensor,
            attribute = Attribute.IS_OPEN,
            operator = Operator.EQUALS,
            value = "true" // Se evalúa si la puerta está abierta
        )

        // Configurar acción asociada al dimmer
        val action = Action(
            id = 1,
            rule = null, // Asociado a la regla después
            device = dimmer,
            actionType = ActionType.SET_BRIGHTNESS,
            parameters = "100" // Cambiar el brillo al 100%
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Door Open Brightness Control",
            conditions = mutableListOf(condition),
            actions = mutableListOf(action)
        )
        condition.rule = rule
        action.rule = rule

        // Actualizar estado: Abrir la puerta
        openingSensor.updateStatus(true)

        // Simular evaluación de la regla
        val result = rule.evaluateAndExecute()

        // Verificar que la regla se evalúa y la acción se ejecuta
        assertTrue(result, "La regla debería evaluarse como verdadera.")
        assertEquals(100, dimmer.brightness, "El brillo del dimmer debería haber cambiado a 100%.")
    }*/


}
