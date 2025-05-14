package ar.unq.ttip.neohub.model


enum class Atributo {
    TEMPERATURA, IS_ON, IS_OPEN, INTENSIDAD;

    companion object {
        fun fromString(value: String): Atributo {
            return Atributo.entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown Atributo: $value")
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}