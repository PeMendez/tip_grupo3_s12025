package ar.unq.ttip.neohub.ruleEngine
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.Rule
import ar.unq.ttip.neohub.model.ruleEngine.RuleEngine
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
@SpringBootTest
@ActiveProfiles("test")
class RuleEngineTest {
    @Test
    fun `cuando la temperatura excede el umbral de la regla, la regla da verdadero y se ejecuta la accion`() {
        // Configurar dispositivos
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val airConditioner = SmartOutlet(name = "Air Conditioner")

        temperatureSensor.temperature=30.0
        airConditioner.isOn=false

        // Configurar condición asociada al sensor
        val condition = Condition(
            id = 1,
            rule = null, // Asociado a la regla después
            device = temperatureSensor,
            attribute = "temperature",
            operator = ">",
            value = "25"
        )

        // Configurar acción asociada al aire acondicionado
        val action = Action(
            id = 1,
            rule = null, // Asociado a la regla después
            device = airConditioner,
            actionType = "turn_on",
            parameters = ""
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Temperature Control",
            conditions = listOf(condition),
            actions = listOf(action)
        )
        condition.rule = rule
        action.rule = rule

        // Simular evaluación de regla
        val ruleEngine = RuleEngine() // Clase que evaluará las reglas
        val result = ruleEngine.evaluate(rule)

        // Verificar que la regla se evalúa y la acción se ejecuta
        assertTrue(result, "La regla debería evaluarse como verdadera.")
        assertTrue(airConditioner.isOn, "El aire acondicionado debería estar encendido.") // Esto depende de implementar un estado en `SmartOutlet`
    }

    @Test
    fun `la temperatura no excede el umbral, así que la regla no se activa`() {
        // Configurar dispositivos
        val temperatureSensor = TemperatureSensor(name = "Temperature Sensor")
        val airConditioner = SmartOutlet(name = "Air Conditioner")

        temperatureSensor.temperature=24.0
        airConditioner.isOn=false

        // Configurar condición asociada al sensor
        val condition = Condition(
            id = 1,
            rule = null, // Asociado a la regla después
            device = temperatureSensor,
            attribute = "temperature",
            operator = ">",
            value = "25"
        )

        // Configurar acción asociada al aire acondicionado
        val action = Action(
            id = 1,
            rule = null, // Asociado a la regla después
            device = airConditioner,
            actionType = "turn_on",
            parameters = ""
        )

        // Configurar regla
        val rule = Rule(
            id = 1,
            name = "Temperature Control",
            conditions = listOf(condition),
            actions = listOf(action)
        )
        condition.rule = rule
        action.rule = rule

        // Simular evaluación de regla
        val ruleEngine = RuleEngine() // Clase que evaluará las reglas (a implementar)
        val result = ruleEngine.evaluate(rule)

        // Verificar que la regla se evalúa y la acción se ejecuta
        assertFalse(result, "La regla debería evaluarse como falsa.")
        assertFalse(airConditioner.isOn, "El aire acondicionado debería estar encendido.") // Esto depende de implementar un estado en `SmartOutlet`
    }
}
