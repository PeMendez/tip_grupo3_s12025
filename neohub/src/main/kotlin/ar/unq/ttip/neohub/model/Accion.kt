package ar.unq.ttip.neohub.model

enum class Accion {
    ENCENDER, APAGAR, NOTIFICAR;

    companion object {
        fun fromString(value: String): Accion {
            return Accion.entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown Accion: $value")
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }

}