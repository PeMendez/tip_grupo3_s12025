package ar.unq.ttip.neohub.model

enum class ActionType {
    TURN_ON, TURN_OFF, NOTIFY, SET_BRIGHTNESS;

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