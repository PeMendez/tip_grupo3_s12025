package ar.unq.ttip.neohub.model

enum class Operator(private val operation: (String, String) -> Boolean) {
    GREATER_THAN({ attr, value -> attr.toDouble() > value.toDouble() }),
    LESS_THAN({ attr, value -> attr.toDouble() < value.toDouble() }),
    EQUALS({ attr, value -> attr == value });

    fun apply(attributeValue: String, value: String): Boolean {
        return operation(attributeValue, value)
    }

    companion object {
        fun fromString(value: String): Operator {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Operador desconocido: $value")
        }
    }
}
