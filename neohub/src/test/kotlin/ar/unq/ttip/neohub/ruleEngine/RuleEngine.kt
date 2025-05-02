package ar.unq.ttip.neohub.ruleEngine

import ar.unq.ttip.neohub.model.Device
import ar.unq.ttip.neohub.model.devices.SmartOutlet
import ar.unq.ttip.neohub.model.devices.TemperatureSensor
import ar.unq.ttip.neohub.model.ruleEngine.Action
import ar.unq.ttip.neohub.model.ruleEngine.Condition
import ar.unq.ttip.neohub.model.ruleEngine.Rule

class RuleEngine {
    fun evaluate(rule: Rule): Boolean {
        val conditionsMet = rule.conditions.all { condition ->
            evaluateCondition(condition)
        }

        if(conditionsMet) {
            rule.actions.forEach { action ->
                executeAction(action)
            }
        }
        return conditionsMet
    }

    private fun evaluateCondition(condition: Condition): Boolean {
        val device = condition.device
        val attributeValue = getAttributeValue(device, condition.attribute)

        return when (condition.operator){
            ">" -> attributeValue.toDouble() > condition.value.toDouble()
            "<" -> attributeValue.toDouble() < condition.value.toDouble()
            "==" -> attributeValue == condition.value
            else -> throw IllegalArgumentException("Operador no soportado: ${condition.operator}")
        }
    }
    private fun getAttributeValue(device: Device, attribute: String): String {
        return when(device){
            is TemperatureSensor -> when(attribute){
                "temperature" -> device.temperature.toString()
                else -> throw IllegalArgumentException("TemperatureSensor no tiene: '$attribute'")
            }
            else -> throw IllegalArgumentException("Dispositivo no soportado: ${device::class.simpleName}")
        }
    }

    private fun executeAction(action: Action) {
        val device = action.device

        when (action.actionType){
            "turn_on" -> {
                when(device) {
                    is SmartOutlet -> {
                        device.isOn = true //esto no va a poder quedar asi claramente
                        println("Se ejecutó la acción")
                    }
                    else -> {
                        throw IllegalArgumentException("Este dispositivo no se puede 'turn_on' ")
                    }
                }
            }
        else -> throw IllegalArgumentException("Acción no soportada.")
        }
    }
}
