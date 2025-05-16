package ar.unq.ttip.neohub.model

enum class ActionType {
    ENCENDER, APAGAR, NOTIFICAR, SET_INTENSIDAD;

    companion object {
        fun fromString(value: String): ActionType {
            return ActionType.entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown Accion: $value")
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }

}