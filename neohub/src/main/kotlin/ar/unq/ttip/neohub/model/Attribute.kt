package ar.unq.ttip.neohub.model


enum class Attribute {
    TEMPERATURA, IS_ON, IS_OPEN, INTENSIDAD; //ver si hace falta descripcion a cada tipo de atributo

    companion object {
        fun fromString(value: String): Attribute {
            return Attribute.entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown Attribute: $value")
        }

        private val supportedOperators = mapOf(
            TEMPERATURA to listOf(Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUALS),
            IS_ON to listOf(Operator.EQUALS),
            IS_OPEN to listOf(Operator.EQUALS),
            INTENSIDAD to listOf(Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUALS)
        )

        fun getSupportedOperators(attribute: Attribute): List<Operator> {
            return supportedOperators[attribute] ?: emptyList()
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}