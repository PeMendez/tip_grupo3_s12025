package ar.unq.ttip.neohub.model


enum class Attribute {
    TEMPERATURE, IS_ON, IS_OPEN, BRIGHTNESS, TIME; //ver si hace falta descripcion a cada tipo de atributo

    companion object {
        fun fromString(value: String): Attribute {
            return Attribute.entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown Attribute: $value")
        }

        private val supportedOperators = mapOf(
            TEMPERATURE to listOf(Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUALS),
            IS_ON to listOf(Operator.EQUALS),
            IS_OPEN to listOf(Operator.EQUALS),
            TIME to listOf(Operator.EQUALS),
            BRIGHTNESS to listOf(Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUALS)
        )

        fun getSupportedOperators(attribute: Attribute): List<Operator> {
            return supportedOperators[attribute] ?: emptyList()
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}