package ar.unq.ttip.neohub.model.devices

enum class DeviceType {
    SMART_OUTLET,
    TEMPERATURE_SENSOR,
    OPENING_SENSOR,
    DIMMER;

    companion object {
        fun fromString(value: String): DeviceType {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown DeviceType: $value")
        }
    }

    override fun toString(): String {
        return name.lowercase() // Si necesitas el nombre en minúsculas
    }
}