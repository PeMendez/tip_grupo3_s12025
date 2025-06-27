package ar.unq.ttip.neohub.model.ruleEngine

import ar.unq.ttip.neohub.model.Operator
import jakarta.persistence.*
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Entity
@Table(name = "time_condition")
class TimeCondition(
    rule: Rule?,
    @Enumerated(EnumType.STRING)
    val operator: Operator, // Ejemplo: "==", ">", "<"

    @Column(name = "time_value") // Nuevo campo para el tiempo
    val value: String // Ejemplo: "12:00", "18:30", formato HH:mm
) : Condition(rule = rule, type = ConditionType.TIME) {
    override fun evaluate(): Boolean {
        val currentTime = LocalTime.now() // Hora actual
        val conditionTime = LocalTime.parse(value) // Convertir valor a hora
        return operator.apply(currentTime.toString(), conditionTime.toString())
    }

    override fun validate() {
        // Validar que el valor sea un formato de hora válido
        try {
            LocalTime.parse(value)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("El valor $value no es una hora válida (formato esperado: HH:mm)")
        }

        // Validar que el operador es compatible con el atributo TIME
        val supportedOperators = listOf(Operator.EQUALS, Operator.GREATER_THAN, Operator.LESS_THAN)
        if (!supportedOperators.contains(operator)) {
            throw IllegalArgumentException("El operador $operator no es soportado para TIME")
        }
    }
}
