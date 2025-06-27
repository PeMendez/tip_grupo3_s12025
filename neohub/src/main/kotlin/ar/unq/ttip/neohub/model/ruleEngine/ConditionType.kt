package ar.unq.ttip.neohub.model.ruleEngine

enum class ConditionType () {
    TIME,
    DEVICE;

    companion object {
        fun fromString(s: String): ConditionType {
            return when (s) {
                "TIME" -> TIME
                "DEVICE" -> DEVICE
                else -> throw IllegalArgumentException("Unknown Condition Type: $s")
            }
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}

